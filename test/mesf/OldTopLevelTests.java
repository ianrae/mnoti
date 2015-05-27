package mesf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.CommitMgrTests.InsertScooterCmd;
import mesf.CommitMgrTests.MyCmdProc;
import mesf.CommitMgrTests.UpdateScooterCmd;
import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.core.Commit;
import mesf.core.CommitCache;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MContext;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.ObjectRepository;
import mesf.core.StreamCache;

import org.junit.Before;
import org.junit.Test;

public class OldTopLevelTests extends BaseTest 
{
	public static abstract class TopLevel
	{
		protected CommandProcessor proc;
		protected CommitMgr commitMgr;
		protected ObjectRepository objcache;
		protected ObjectManagerRegistry registry;

		public TopLevel(CommitMgr commitMgr, ObjectManagerRegistry registry, IStreamDAO streamDAO, ObjectRepository objcache)
		{
			this.commitMgr = commitMgr;
			this.registry = registry;
			this.objcache = objcache;
			
			createProc();
		}
		
		protected abstract void createProc();

		public void init(long oldMaxId)
		{
			commitMgr.freshenMaxId(); //efficently load new ones
			
			List<Commit> L = commitMgr.loadAllFrom(oldMaxId + 1);
			commitMgr.observeList(L, objcache);
		}
	}
	
	public static class MyTopLevel extends TopLevel
	{

		public MyTopLevel(CommitMgr commitMgr, ObjectManagerRegistry registry, IStreamDAO streamDAO, ObjectRepository objcache)
		{
			super(commitMgr, registry, streamDAO, objcache);
		}

		@Override
		protected void createProc() 
		{
			MContext mtx = new MContext(commitMgr, registry, objcache, null, null);
			this.proc = new MyCmdProc(mtx);
		}

		public void process(ICommand cmd) 
		{
			proc.process(cmd);
		}

		public long getMaxId() 
		{
			return commitMgr.getMaxId();
		}
	}
	
	long maxId = 0L;
	ObjectManagerRegistry registry;
	
	@Test
	public void test() throws Exception
	{
		//create long-running objects
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitCache cache = new CommitCache(dao);
		
		registry = new ObjectManagerRegistry();
		registry.register(Scooter.class, new ObjectMgr<Scooter>(Scooter.class));
		ObjectRepository objcache = new ObjectRepository(streamDAO, registry);
		maxId = 0L;
		
		MyTopLevel toplevel = createTopLevel(dao, streamDAO, cache, objcache);
		log(String.format("1st: %d", maxId));
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		toplevel.process(cmd);
		
		toplevel = createTopLevel(dao, streamDAO, cache, objcache);
		log(String.format("2nd: %d", maxId));
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		toplevel = createTopLevel(dao, streamDAO, cache, objcache);
		log(String.format("3rd: %d", maxId));
		ucmd = new UpdateScooterCmd();
		ucmd.s = "again";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		toplevel = createTopLevel(dao, streamDAO, cache, objcache);
		log(String.format("4th: %d", maxId));
		ucmd = new UpdateScooterCmd();
		ucmd.s = "again2";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		toplevel = createTopLevel(dao, streamDAO, cache, objcache);
		log(String.format("5th: %d", maxId));
		ucmd = new UpdateScooterCmd();
		ucmd.s = "again3";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		toplevel = createTopLevel(dao, streamDAO, cache, objcache);
		log(String.format("6th: %d", maxId));
		ucmd = new UpdateScooterCmd();
		ucmd.s = "again4";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		
		objcache.dumpStats();
//		log("a");
//		commitMgr.dump();
	}

	private MyTopLevel createTopLevel(ICommitDAO dao, IStreamDAO streamDAO, CommitCache cache, ObjectRepository objcache)
	{
		CommitMgr commitMgr = new CommitMgr(dao, streamDAO, cache,  new StreamCache(streamDAO));
		
		
		MyTopLevel toplevel = new MyTopLevel(commitMgr, registry, streamDAO, objcache);
		toplevel.init(maxId);
		maxId = toplevel.getMaxId();
		return toplevel;
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
