package mesf.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mef.framework.sfx.SfxTrail;

public class ObjectViewCache implements ICommitObserver
{
	Map<Long, BaseObject> map = new HashMap<>(); //!!needs to be thread-safe
	private IStreamDAO streamDAO;
	private ObjectManagerRegistry registry;
	private long numHits;
	private long numMisses;
	private SfxTrail trail = new SfxTrail();
	
	public ObjectViewCache(IStreamDAO streamDAO, ObjectManagerRegistry registry)
	{
		this.streamDAO = streamDAO;
		this.registry = registry;
	}
	
	public synchronized void dumpStats()
	{
		System.out.println(String.format("OVC: hits:%d, misses:%d", numHits, numMisses));
		System.out.println(trail.getTrail());
	}
	
	public synchronized BaseObject loadObject(String type, Long objectId, StreamLoader sloader) throws Exception
	{
		BaseObject obj = map.get(objectId);
		if (obj != null)
		{
			numHits++;
			return obj;
		}
		
		numMisses++;
		obj = doLoadObject(type, objectId, sloader);
		return obj;
	}
	private BaseObject doLoadObject(String type, Long objectId,  StreamLoader sloader) throws Exception
	{
		Stream stream = streamDAO.findById(objectId);
		if (stream == null)
		{
			return null;
		}
		
		IObjectMgr mgr = registry.findByType(type);
		List<Commit> L = sloader.loadStream(type, objectId);
		BaseObject obj = null;
		
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
		BaseObject obj = map.get(objectId);
		
		IObjectMgr mgr = registry.findByType(stream.getType());
		try {
			obj = doObserve(objectId, commit, mgr, obj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private BaseObject doObserve(Long objectId, Commit commit, IObjectMgr mgr, BaseObject obj) throws Exception
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
		}
		return obj;
	}

	public synchronized BaseObject getIfLoaded(Long objectId) 
	{
		BaseObject obj = map.get(objectId);
		return obj;
	}
}