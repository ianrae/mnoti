package mesf.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mesf.core.ICommitObserver;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.IStreamDAO;
import mesf.persistence.Stream;

import org.mef.framework.sfx.SfxTrail;

public class EntityRepository implements ICommitObserver
{
	Map<Long, BaseEntity> map = new HashMap<>(); //!!needs to be thread-safe
	Map<Long, Long> whenMap = new HashMap<>(); 
	private IStreamDAO streamDAO;
	private EntityManagerRegistry registry;
	private long numHits;
	private long numMisses;
	private SfxTrail trail = new SfxTrail();

	public EntityRepository(IStreamDAO streamDAO, EntityManagerRegistry registry)
	{
		this.streamDAO = streamDAO;
		this.registry = registry;
	}

	public synchronized void dumpStats()
	{
		Logger.log("OVC: hits:%d, misses:%d", numHits, numMisses);
		Logger.log(trail.getTrail());
	}

	public synchronized BaseEntity loadObject(String type, Long objectId, EntityLoader oloader) throws Exception
	{
		BaseEntity obj = map.get(objectId);
		Long startId = null;
		if (obj != null)
		{
			long when = whenMap.get(objectId);
			if(when >= oloader.getMaxId())
			{
				numHits++;
				return obj;
			}
			startId = when + 1L;
		}

		numMisses++;
		obj = doLoadObject(type, objectId, oloader, startId, obj);
		return obj;
	}
	private BaseEntity doLoadObject(String type, Long objectId, EntityLoader oloader, Long startId, BaseEntity obj) throws Exception
	{
		List<Commit> L = null;
		if (startId == null)
		{
			L = oloader.loadStream(type, objectId);
		}
		else 
		{
			L = oloader.loadPartialStream(objectId, startId);
		}
		IEntityMgr mgr = registry.findByType(type);

		for(Commit commit : L)
		{
			try {
				obj = doObserve(objectId, commit, mgr, obj);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return obj;
	}

	public synchronized Object getSize() 
	{
		return map.size();
	}

	@Override
	public synchronized boolean willAccept(Stream stream, Commit commit) 
	{
		if (stream == null)
		{
			return false;
		}
		return map.containsKey(stream.getId()); //only care about object we have already in cache
	}

	@Override
	public synchronized void observe(Stream stream, Commit commit) 
	{
		Long objectId = stream.getId();
		BaseEntity obj = map.get(objectId);

		IEntityMgr mgr = registry.findByType(stream.getType());
		try {
			obj = doObserve(objectId, commit, mgr, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private BaseEntity doObserve(Long objectId, Commit commit, IEntityMgr mgr, BaseEntity obj) throws Exception
	{
		this.trail.add(commit.getId().toString()); //remove later!!

		switch(commit.getAction())
		{
		case 'I':
		case 'S':
			obj = mgr.rehydrate(commit.getJson());
			if (obj != null)
			{
				obj.setId(objectId);
				map.put(objectId, obj);
			}
			break;
		case 'U':
			mgr.mergeHydrate(obj, commit.getJson());
			break;
		case 'D':
			obj = null;
			break;
		default:
			break;
		}

		if (obj != null)
		{
			obj.clearSetList();
			whenMap.put(objectId, commit.getId()); 
		}
		return obj;
	}

	public synchronized BaseEntity getIfLoaded(Long objectId) 
	{
		BaseEntity obj = map.get(objectId);
		return obj;
	}
}