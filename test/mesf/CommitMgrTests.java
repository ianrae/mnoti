package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.cmd.ObjectCommand;
import mesf.core.BaseObject;
import mesf.core.Commit;
import mesf.core.CommitCache;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.ICommitObserver;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.ObjectCache;
import mesf.core.Stream;
import mesf.core.ObjectLoader;
import mesf.core.StreamCache;
import mesf.view.ViewLoader;
import mesf.view.ViewManager;

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
	
	public static class MultiObserver implements ICommitObserver
	{
		List<ICommitObserver> L = new ArrayList<>();
		
		public MultiObserver()
		{}

		@Override
		public boolean willAccept(Stream stream, Commit commit)
		{
			return true;
		}

		@Override
		public void observe(Stream stream, Commit commit) 
		{
			for(ICommitObserver observer : L)
			{
				if (observer.willAccept(stream, commit))
				{
					observer.observe(stream, commit);
				}
			}
		}
	}
	
	
	public static class InsertScooterCmd extends ObjectCommand
	{
		public int a;
		public String s;
	}
	public static class UpdateScooterCmd extends ObjectCommand
	{
		public String s;
	}
	public static class DeleteScooterCmd extends ObjectCommand
	{
	}
	
	public static class MyCmdProc extends CommandProcessor
	{
		public MyCmdProc(CommitMgr commitMgr, ObjectManagerRegistry registry, ObjectCache objcache, ViewManager viewMgr, ViewLoader vloader)
		{
			super(commitMgr, registry, objcache, viewMgr, vloader);
		}

		@Override
		public void process(ICommand cmd) 
		{
			try {
				if (cmd instanceof InsertScooterCmd)
				{
					doInsertScooterCmd((InsertScooterCmd)cmd);
				}
				else if (cmd instanceof UpdateScooterCmd)
				{
					doUpdateScooterCmd((UpdateScooterCmd)cmd);
				}
				else if (cmd instanceof DeleteScooterCmd)
				{
					doDeleteScooterCmd((DeleteScooterCmd)cmd);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void doDeleteScooterCmd(DeleteScooterCmd cmd) throws Exception 
		{
			Scooter scooter = loadTheObject(cmd.getObjectId());
			if (scooter == null)
			{
				return; //!!
			}
			
			deleteObject(scooter);
		}

		private Scooter loadTheObject(long objectId) throws Exception 
		{
			String type = this.registry.findTypeForClass(Scooter.class);
			Scooter scooter = (Scooter) this.hydrater.loadObject(type, objectId, oloader);
			return scooter;
		}
		private void doUpdateScooterCmd(UpdateScooterCmd cmd) throws Exception 
		{
			Scooter scooter = loadTheObject(cmd.getObjectId());
			if (scooter == null)
			{
				return; //!!
			}
			
			scooter.setS(cmd.s);
			updateObject(scooter);
		}

		private void doInsertScooterCmd(InsertScooterCmd cmd) throws Exception
		{
			Scooter scooter = new Scooter();
			scooter.setA(cmd.a);
			scooter.setB(10);
			scooter.setS(cmd.s);
			
			insertObject(cmd, scooter);
		}
	}
	
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO, new CommitCache(dao), new StreamCache(streamDAO));
		
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
		CommitMgr mgr = new CommitMgr(dao, streamDAO, new CommitCache(dao), new StreamCache(streamDAO));
		
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
		mgr.freshenMaxId(); //update maxid
		L = mgr.loadAll();
		assertEquals(3, L.size());
		chkStreamSize(streamDAO, 1);
		
		CountObserver observer = new CountObserver("scooter");
		mgr.observeList(mgr.loadAll(), observer);
		assertEquals(1, observer.count);
		
		mgr.deleteObject(omgr, scooter);
		mgr.freshenMaxId(); //update maxid
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
		CommitMgr mgr = new CommitMgr(dao, streamDAO, new CommitCache(dao), new StreamCache(streamDAO));
		ObjectLoader oloader = mgr.createObjectLoader();
		
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
		mgr.freshenMaxId();
		L = mgr.loadAll();
		assertEquals(3, L.size());
		chkStreamSize(streamDAO, 1);
		oloader = mgr.createObjectLoader();
		
		mgr.dump();
		ObjectManagerRegistry registry = new ObjectManagerRegistry();
		registry.register(Scooter.class, new ObjectMgr<Scooter>(Scooter.class));
		ObjectCache objcache = new ObjectCache(streamDAO, registry);
		
		BaseObject obj = objcache.loadObject("scooter", scooter.getId(), oloader);
		assertEquals(1L, obj.getId().longValue());
		chkScooter((Scooter) obj, 444, 26, "abc");

		BaseObject obj2 = objcache.loadObject("scooter", scooter.getId(), oloader);
		assertEquals(1L, obj2.getId().longValue());
		chkScooter((Scooter) obj2, 444, 26, "abc");
		
		assertEquals(1, objcache.getSize());
		
		//commit more
		long maxId = mgr.getMaxId();
		scooter.clearSetList();
		scooter.setA(555);
		mgr.updateObject(omgr, scooter);
		mgr.dump();
		
		mgr.freshenMaxId();
		L = mgr.loadAllFrom(maxId + 1);
		assertEquals(1, L.size());
		mgr.observeList(mgr.loadAll(), objcache);
		Scooter scoot2 = (Scooter) objcache.getIfLoaded(scooter.getId());
		assertEquals(555, scoot2.getA());
		oloader = mgr.createObjectLoader();
	}

	@Test
	public void testCmd() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr commitMgr = new CommitMgr(dao, streamDAO, new CommitCache(dao), new StreamCache(streamDAO));

		ObjectManagerRegistry registry = new ObjectManagerRegistry();
		registry.register(Scooter.class, new ObjectMgr<Scooter>(Scooter.class));
		ObjectCache objcache = new ObjectCache(streamDAO, registry);
		
		MyCmdProc proc = new MyCmdProc(commitMgr, registry, objcache, null, null);
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		proc.process(cmd);
		
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.objectId = 1L;
		proc.process(ucmd);
		
		DeleteScooterCmd dcmd = new DeleteScooterCmd();
		dcmd.objectId = 1L;
		proc.process(dcmd);
		
		long oldMaxId = commitMgr.getMaxId();
		commitMgr.freshenMaxId();
		commitMgr.dump();
		
		List<Commit> L = commitMgr.loadAllFrom(oldMaxId + 1);
		CountObserver observer = new CountObserver("scooter");
		commitMgr.observeList(L, observer);
		log(String.format("n %d", observer.count));
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
