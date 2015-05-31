package mesf.core;

import java.util.List;

import mesf.cache.CommitCache;
import mesf.cache.StreamCache;
import mesf.entity.Entity;
import mesf.entity.EntityLoader;
import mesf.entity.IEntityMgr;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.PersistenceContext;
import mesf.persistence.Stream;

//will create one of these per web request, but all will share underlying thread-safe commit cache
public class CommitMgr
{
	private ICommitDAO dao;
	private IStreamDAO streamDAO;
	private long maxId; //per current epoch
	private CommitCache cache;
	private StreamCache strcache;
	private MContext mtx;

	public CommitMgr(MContext mtx, PersistenceContext persistenceCtx, CommitCache cache, StreamCache strcache)
	{
		this.mtx = mtx;
		this.dao = persistenceCtx.getDao();
		this.streamDAO = persistenceCtx.getStreamDAO();
		this.cache = cache;
		this.strcache = strcache;
	}
	
	public long getMaxId()
	{
		if (maxId != 0L)
		{
			return maxId;
		}
		
		maxId = dao.findMaxId();
		return maxId;
	}
	public long freshenMaxId()
	{
		maxId = 0L;
		getMaxId();
		cache.clearLastSegment(maxId);
		return maxId;
	}
	
	public List<Commit> loadAll()
	{
		getMaxId();
		List<Commit> L = cache.loadRange(0, maxId);
		return L;
	}
	public List<Commit> loadAllFrom(long startId)
	{
		getMaxId();
		long startIndex = startId - 1; //no 0 id
		long n = maxId - startIndex;
		List<Commit> L = cache.loadRange(startIndex, n);
		return L;
	}
	
	public Commit loadByCommitId(Long id)
	{
		List<Commit> L = this.cache.loadRange(id - 1, 1);
		if (L.size() == 0)
		{
			return null;
		}
		return L.get(0);
//		return dao.findById(id);
	}
	
	public Commit loadSnapshotCommit(Long streamId)
	{
		Stream stream = strcache.findStream(streamId);
		if (stream == null)
		{
			return null; //!!
		}
		
		Commit commit = loadByCommitId(stream.getSnapshotId());
		return commit;
	}
//	public List<Commit> loadStream(String type, Long entityId)
//	{
//		StreamLoader loader = new StreamLoader(dao, streamDAO, maxId);
//		List<Commit> L = loader.loadStream(type, entityId);
//		return L;
//	}
	
	public void writeNoOp()
	{
		Commit commit = new Commit();
		commit.setAction('-');
		this.dao.save(commit);
	}
	
	public void dump()
	{
		for(Commit commit : loadAll())
		{
			String s = String.format("[%d] %c %d json:%s", commit.getId(), commit.getAction(), commit.getStreamId(), commit.getJson());
			Logger.log(s);
		}
	}
	
	public long insertEntity(IEntityMgr mgr, Entity obj)
	{
		Stream stream = new Stream();
		stream.setType(mgr.getTypeName());
		this.streamDAO.save(stream);
		
		Long entityid = stream.getId();
		obj.setId(entityid);
		
		Commit commit = new Commit();
		commit.setAction('I');
		commit.setStreamId(entityid);
		String json = "";
		try {
			json = mgr.renderEntity(obj);
		} catch (Exception e) {
			e.printStackTrace();  //!!handle later!!
		}
		commit.setJson(json);
		this.dao.save(commit);
		
		Long snapshotId = commit.getId();
		stream.setSnapshotId(snapshotId);
		this.streamDAO.update(stream);
		Logger.logDebug("INS [%d] %d %s - %s", snapshotId, entityid, mgr.getTypeName(), commit.getJson());
		return entityid;
	}
	
	public void updateEntity(IEntityMgr mgr, Entity obj)
	{
//		Stream stream = streamDAO.findById(obj.getId());
//		if (stream == null)
//		{
//			return; //!!
//		}
		
//		Long entityId = stream.getId();
		Long entityId = obj.getId();
		Commit commit = new Commit();
		commit.setAction('U');
		commit.setStreamId(entityId);
		String json = "";
		try {
			json = mgr.renderPartial(obj);
		} catch (Exception e) {
			e.printStackTrace();  //!!handle later!!
		}
		commit.setJson(json);
		this.dao.save(commit);
		Logger.logDebug("UPD [%d] %d %s - %s", commit.getId(), entityId, mgr.getTypeName(), commit.getJson());
	}
	public void deleteEntity(IEntityMgr mgr, Entity obj)
	{
//		Stream stream = streamDAO.findById(obj.getId());
//		if (stream == null)
//		{
//			return; //!!
//		}
		
		Long entityId = obj.getId();
		Commit commit = new Commit();
		commit.setAction('D');
		commit.setStreamId(entityId);
		String json = "";
		commit.setJson(json);
		this.dao.save(commit);
		Logger.logDebug("DEL [%d] %d %s", commit.getId(), entityId, mgr.getTypeName());
	}
	
	public void observeList(List<Commit> L, ICommitObserver observer)
	{
		for(Commit commit : L)
		{
			Long streamId = commit.getStreamId();
			Stream stream = null;
			if (streamId != null)
			{
				stream = strcache.findStream(streamId);
			}
			
			if (observer.willAccept(stream, commit))
			{
				observer.observe(mtx, stream, commit);
			}
		}
	}

	public EntityLoader createEntityLoader() 
	{
		EntityLoader oloader = new EntityLoader(dao, strcache, this.maxId);
		return oloader;
	}

	public void setMtx(MContext mtx)
	{
		this.mtx = mtx;
	}
}