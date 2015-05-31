package mesf.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mesf.core.ICommitObserver;
import mesf.core.MContext;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.IStreamDAO;
import mesf.persistence.Stream;
import mesf.util.SfxTrail;


public class EntityRepository implements ICommitObserver
{
	Map<Long, Entity> map = new HashMap<>(); //!!needs to be thread-safe
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

	public synchronized Entity loadEntity(String type, Long entityId, EntityLoader oloader) throws Exception
	{
		Entity obj = map.get(entityId);
		Long startId = null;
		if (obj != null)
		{
			long when = whenMap.get(entityId);
			if(when >= oloader.getMaxId())
			{
				numHits++;
				return obj;
			}
			startId = when + 1L;
		}

		numMisses++;
		obj = doLoadEntity(type, entityId, oloader, startId, obj);
		return obj;
	}
	private Entity doLoadEntity(String type, Long entityId, EntityLoader oloader, Long startId, Entity obj) throws Exception
	{
		List<Commit> L = null;
		if (startId == null)
		{
			L = oloader.loadStream(type, entityId);
		}
		else 
		{
			L = oloader.loadPartialStream(entityId, startId);
		}
		IEntityMgr mgr = registry.findByType(type);

		for(Commit commit : L)
		{
			try {
				obj = doObserve(entityId, commit, mgr, obj);
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
		return map.containsKey(stream.getId()); //only care about entity we have already in cache
	}

	@Override
	public synchronized void observe(MContext mtx, Stream stream, Commit commit) 
	{
		Long entityId = stream.getId();
		Entity obj = map.get(entityId);

		IEntityMgr mgr = registry.findByType(stream.getType());
		try {
			obj = doObserve(entityId, commit, mgr, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private Entity doObserve(Long entityId, Commit commit, IEntityMgr mgr, Entity obj) throws Exception
	{
		this.trail.add(commit.getId().toString()); //remove later!!

		switch(commit.getAction())
		{
		case 'I':
		case 'S':
			obj = mgr.rehydrate(commit.getJson());
			if (obj != null)
			{
				obj.setId(entityId);
				map.put(entityId, obj);
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
			whenMap.put(entityId, commit.getId()); 
		}
		return obj;
	}

	public synchronized Entity getIfLoaded(Long entityId) 
	{
		Entity obj = map.get(entityId);
		return obj;
	}
}