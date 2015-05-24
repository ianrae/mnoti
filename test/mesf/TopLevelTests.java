package mesf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.CommitMgrTests.MyCmdProc;
import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.core.Commit;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.ObjectViewCache;

import org.junit.Before;
import org.junit.Test;

public class TopLevelTests extends BaseTest 
{
	public static abstract class TopLevel
	{
		protected CommandProcessor proc;
		protected CommitMgr commitMgr;
		protected ObjectViewCache objcache;
		protected ObjectManagerRegistry registry;

		public TopLevel(CommitMgr commitMgr, ObjectManagerRegistry registry, IStreamDAO streamDAO)
		{
			this.commitMgr = commitMgr;
			this.registry = registry;
			this.objcache = new ObjectViewCache(commitMgr, streamDAO, registry);
			
			createProc();
		}
		
		protected abstract void createProc();

		public void init(long oldMaxId)
		{
			List<Commit> L = commitMgr.loadAllFrom(oldMaxId + 1);
			commitMgr.observeList(L, objcache);
		}
	}
	
	public static class MyTopLevel extends TopLevel
	{

		public MyTopLevel(CommitMgr commitMgr, ObjectManagerRegistry registry, IStreamDAO streamDAO)
		{
			super(commitMgr, registry, streamDAO);
		}

		@Override
		protected void createProc() 
		{
			this.proc = new MyCmdProc(commitMgr, registry, objcache);
		}
		
	}
	
	
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr commitMgr = new CommitMgr(dao, streamDAO);
		
		ObjectManagerRegistry registry = new ObjectManagerRegistry();
		registry.register(Scooter.class, new ObjectMgr<Scooter>(Scooter.class));
		
		MyTopLevel toplevel = new MyTopLevel(commitMgr, registry, streamDAO);
		
		toplevel.init(0L);
//		assertEquals(0, 1);
		
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
