package mesf;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mef.framework.helpers.BaseTest;
import mesf.ObjManagerTests.Scooter;
import mesf.core.BaseObject;
import mesf.core.Commit;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.ICommitObserver;
import mesf.core.IObjectMgr;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectMgr;
import mesf.core.Stream;

import org.junit.Before;
import org.junit.Test;

public class CommitMgrTests extends BaseTest 
{
	public static class CountObserver implements ICommitObserver
	{
		String type;
		public int count;
		
		public CountObserver(String type)
		{
			this.type = type;
		}
		
		@Override
		public boolean willAccept(Stream stream, Commit commit) 
		{
			if (stream != null && ! stream.getType().equals(type))
			{
				return false;
			}
			
			char action = commit.getAction();
			return (action == 'I' || action == 'D');
		}

		@Override
		public void observe(Stream stream, Commit commit) 
		{
			char action = commit.getAction();
			if (action == 'I')
			{
				count++;
			}
			else if (action == 'D')
			{
				count--;
			}
		}
	}
	
	public static class ObjectManagerRegistry
	{
		Map<Class, IObjectMgr> map = new HashMap<>();
		
		public ObjectManagerRegistry()
		{
			
		}
		public void register(Class clazz, IObjectMgr mgr)
		{
			map.put(clazz, mgr);
		}
		public IObjectMgr findByType(String type) 
		{
			for(Class clazz : map.keySet())
			{
				IObjectMgr mgr = map.get(clazz);
				if (mgr.getTypeName().equals(type))
				{
					return mgr;
				}
			}
			return null;
		}
	}
	
	public static class ObjectViewCache implements ICommitObserver
	{
		Map<Long, BaseObject> map = new HashMap<>();
		private IStreamDAO streamDAO;
		private CommitMgr commitMgr;
		private ObjectManagerRegistry registry;
		
		public ObjectViewCache(CommitMgr commitMgr, IStreamDAO streamDAO, ObjectManagerRegistry registry)
		{
			this.commitMgr = commitMgr;
			this.streamDAO = streamDAO;
			this.registry = registry;
		}
		
		public BaseObject loadObject(String type, Long objectId) throws Exception
		{
			BaseObject obj = map.get(objectId);
			if (obj != null)
			{
				return obj;
			}
			obj = doLoadObject(type, objectId);
			return obj;
		}
		private BaseObject doLoadObject(String type, Long objectId) throws Exception
		{
			Stream stream = streamDAO.findById(objectId);
			if (stream == null)
			{
				return null;
			}
			
			IObjectMgr mgr = registry.findByType(type);
			List<Commit> L = commitMgr.loadStream(type, objectId);
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

		public Object getSize() 
		{
			return map.size();
		}

		@Override
		public boolean willAccept(Stream stream, Commit commit) 
		{
			if (stream == null)
			{
				return false;
			}
			return map.containsKey(stream.getId()); //only care about object we have already in cache
		}

		@Override
		public void observe(Stream stream, Commit commit) 
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

		public BaseObject getIfLoaded(Long objectId) 
		{
			BaseObject obj = map.get(objectId);
			return obj;
		}
	}
	
	
	public static class ObjectHydrater
	{
		private ObjectViewCache objcache;
		
		public ObjectHydrater(ObjectViewCache objcache)
		{
			this.objcache = objcache;
		}
		
		public BaseObject loadObject(String type, Long objectId) throws Exception
		{
			//objcache should be immutable objects, so for our commands make a copy
			BaseObject obj = objcache.loadObject(type, objectId);
			if (obj != null)
			{
				BaseObject clone = obj.clone();
				return clone;
			}
			return null;
		}
	}
	
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
		
		List<Commit> L = mgr.loadAll();
		assertEquals(0, L.size());
		
		mgr.writeNoOp();
		mgr.writeNoOp();
		L = mgr.loadAll();
		assertEquals(2, L.size());
		Commit commit = L.get(1);
		assertEquals(2L, commit.getId().longValue());
		assertEquals('-', commit.getAction());
		
		mgr.dump();
	}

	@Test
	public void testInsert() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
		
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMgr<Scooter> omgr = new ObjectMgr(Scooter.class);
		Scooter scooter = omgr.createFromJson(fix(json));		

		mgr.writeNoOp();
		mgr.insertObject(omgr, scooter);
		List<Commit> L = mgr.loadAll();
		assertEquals(2, L.size());
		
		chkStreamSize(streamDAO, 1);
		Stream stream = streamDAO.findById(1L);
		assertEquals("scooter", stream.getType());
		assertEquals(1L, stream.getId().longValue());
		assertEquals(2L, stream.getSnapshotId().longValue());
		
		scooter.clearSetList();
		scooter.setA(444);
		mgr.updateObject(omgr, scooter);
		L = mgr.loadAll();
		assertEquals(3, L.size());
		chkStreamSize(streamDAO, 1);
		
		CountObserver observer = new CountObserver("scooter");
		mgr.observeList(mgr.loadAll(), observer);
		assertEquals(1, observer.count);
		
		mgr.deleteObject(omgr, scooter);
		L = mgr.loadAll();
		assertEquals(4, L.size());
		chkStreamSize(streamDAO, 1);

		mgr.dump();
		observer = new CountObserver("scooter");
		mgr.observeList(mgr.loadAll(), observer);
		assertEquals(0, observer.count);
	}
	
	
	@Test
	public void testViewCache() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
		
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMgr<Scooter> omgr = new ObjectMgr(Scooter.class);
		Scooter scooter = omgr.createFromJson(fix(json));		

		mgr.writeNoOp();
		mgr.insertObject(omgr, scooter);
		List<Commit> L = mgr.loadAll();
		assertEquals(2, L.size());
		scooter.clearSetList();
		scooter.setA(444);
		mgr.updateObject(omgr, scooter);
		L = mgr.loadAll();
		assertEquals(3, L.size());
		chkStreamSize(streamDAO, 1);
		
		mgr.dump();
		ObjectManagerRegistry registry = new ObjectManagerRegistry();
		registry.register(Scooter.class, new ObjectMgr<Scooter>(Scooter.class));
		ObjectViewCache objcache = new ObjectViewCache(mgr, streamDAO, registry);
		
		BaseObject obj = objcache.loadObject("scooter", scooter.getId());
		assertEquals(1L, obj.getId().longValue());
		chkScooter((Scooter) obj, 444, 26, "abc");

		BaseObject obj2 = objcache.loadObject("scooter", scooter.getId());
		assertEquals(1L, obj2.getId().longValue());
		chkScooter((Scooter) obj2, 444, 26, "abc");
		
		assertEquals(1, objcache.getSize());
		
		//commit more
		long maxId = mgr.getMaxId();
		scooter.clearSetList();
		scooter.setA(555);
		mgr.updateObject(omgr, scooter);
		mgr.dump();
		
		L = mgr.loadAllFrom(maxId + 1);
		assertEquals(1, L.size());
		mgr.observeList(mgr.loadAll(), objcache);
		Scooter scoot2 = (Scooter) objcache.getIfLoaded(scooter.getId());
		assertEquals(555, scoot2.getA());
	}

	//--helpers--
	private void chkStreamSize(IStreamDAO streamDAO, int expected)
	{
		assertEquals(expected, streamDAO.size());
	}
	private void chkScooter(Scooter scooter, int expectedA, int expectedB, String expectedStr)
	{
		assertEquals(expectedA, scooter.getA());
		assertEquals(expectedB, scooter.getB());
		assertEquals(expectedStr, scooter.getS());
	}
	
	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}

	@Before
	public void init()
	{
		super.init();
	}
}
