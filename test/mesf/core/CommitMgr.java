package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.ObjManagerTests;

//will create one of these per web request, but all will share underlying thread-safe commit cache
public class CommitMgr
{
	private ICommitDAO dao;
	private IStreamDAO streamDAO;
	private long maxId; //per current epoch
	private CommitCache cache;

	public CommitMgr(ICommitDAO dao, IStreamDAO streamDAO)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
		this.cache = new CommitCache(dao);
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
		Stream stream = streamDAO.findById(streamId);
		if (stream == null)
		{
			return null; //!!
		}
		
		Commit commit = loadByCommitId(stream.getSnapshotId());
		return commit;
	}
	public List<Commit> loadStream(String type, Long id)
	{
		Stream stream = streamDAO.findById(id);
		if (stream == null)
		{
			return null; //!!
		}
		
		List<Commit> rawL = loadAllFrom(stream.getSnapshotId());
		List<Commit> L = new ArrayList<>();
		for(Commit commit : rawL)
		{
			if (commit.getStreamId().equals(id))
			{
				L.add(commit);
			}
		}
		return L;
	}
	
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
			System.out.println(s);
		}
	}
	
	public void insertObject(IObjectMgr mgr, BaseObject obj)
	{
		Stream stream = new Stream();
		stream.setType(mgr.getTypeName());
		this.streamDAO.save(stream);
		
		Long objectId = stream.getId();
		obj.setId(objectId);
		
		Commit commit = new Commit();
		commit.setAction('I');
		commit.setStreamId(objectId);
		String json = "";
		try {
			json = mgr.renderObject(obj);
		} catch (Exception e) {
			e.printStackTrace();  //!!handle later!!
		}
		commit.setJson(json);
		this.dao.save(commit);
		
		Long snapshotId = commit.getId();
		stream.setSnapshotId(snapshotId);
		this.streamDAO.update(stream);
		System.out.println(String.format("INS [%d] %d %s", snapshotId, objectId, mgr.getTypeName()));
	}
	
	public void updateObject(IObjectMgr mgr, BaseObject obj)
	{
		Stream stream = streamDAO.findById(obj.getId());
		if (stream == null)
		{
			return; //!!
		}
		
		Long objectId = stream.getId();
		Commit commit = new Commit();
		commit.setAction('U');
		commit.setStreamId(objectId);
		String json = "";
		try {
			json = mgr.renderPartialObject(obj);
		} catch (Exception e) {
			e.printStackTrace();  //!!handle later!!
		}
		commit.setJson(json);
		this.dao.save(commit);
	}
	public void deleteObject(IObjectMgr mgr, BaseObject obj)
	{
		Stream stream = streamDAO.findById(obj.getId());
		if (stream == null)
		{
			return; //!!
		}
		
		Long objectId = stream.getId();
		Commit commit = new Commit();
		commit.setAction('D');
		commit.setStreamId(objectId);
		String json = "";
		commit.setJson(json);
		this.dao.save(commit);
	}
	
	public void observeList(List<Commit> L, ICommitObserver observer)
	{
		for(Commit commit : L)
		{
			Long streamId = commit.getStreamId();
			Stream stream = null;
			if (streamId != null)
			{
				stream = streamDAO.findById(streamId);
			}
			
			if (observer.willAccept(stream, commit))
			{
				observer.observe(stream, commit);
			}
		}
	}
}