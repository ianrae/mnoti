package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mef.framework.helpers.BaseTest;
import mesf.ObjManagerTests.BaseObject;
import mesf.ObjManagerTests.IObjectMgr;
import mesf.ObjManagerTests.ObjectMgr;
import mesf.ObjManagerTests.Scooter;
import mesf.core.Commit;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.Stream;

import org.junit.Before;
import org.junit.Test;

public class CommitMgrTests extends BaseTest 
{
	public static interface ICommitObserver
	{
		boolean willAccept(Stream stream, Commit commit);
		void observe(Stream stream, Commit commit);
	}
	
	public static class CommitMgr
	{
		private ICommitDAO dao;
		private IStreamDAO streamDAO;

		public CommitMgr(ICommitDAO dao, IStreamDAO streamDAO)
		{
			this.dao = dao;
			this.streamDAO = streamDAO;
		}
		
		List<Commit> loadAll()
		{
			return dao.all();
		}
		List<Commit> loadAllFrom(long startId)
		{
			List<Commit> L = new ArrayList<>();
			for(Commit commit : loadAll())
			{
				if (commit.getId().longValue() >= startId)
				{
					L.add(commit);
				}
			}
			return L;
		}
		
		Commit loadByCommitId(Long id)
		{
			return dao.findById(id);
		}
		
		Commit loadSnapshotCommit(Long streamId)
		{
			Stream stream = streamDAO.findById(streamId);
			if (stream == null)
			{
				return null; //!!
			}
			
			Commit commit = dao.findById(stream.getSnapshotId());
			return commit;
		}
		List<Commit> loadStream(String type, Long id)
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
	
	public static class ObjectViewCache
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
				switch(commit.getAction())
				{
				case 'I':
				case 'S':
					obj = mgr.rehydrate(commit.getJson());
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
			}
			
			return obj;
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
	}

	private void chkStreamSize(IStreamDAO streamDAO, int expected)
	{
		assertEquals(expected, streamDAO.size());
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
