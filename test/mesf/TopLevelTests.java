package mesf;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.CommitMgrTests.InsertScooterCmd;
import mesf.CommitMgrTests.MyCmdProc;
import mesf.CommitMgrTests.UpdateScooterCmd;
import mesf.ObjManagerTests.Scooter;
import mesf.OldTopLevelTests.MyTopLevel;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.core.BaseObject;
import mesf.core.Commit;
import mesf.core.CommitCache;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.IObjectMgr;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.ObjectViewCache;
import mesf.core.Stream;

import org.junit.Before;
import org.junit.Test;

public class TopLevelTests extends BaseTest 
{
	public static abstract class Permanent
	{
		protected ICommitDAO dao;
		protected IStreamDAO streamDAO;
		protected ObjectManagerRegistry registry;
		protected ObjectViewCache objcache;

		public Permanent(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry)
		{
			this.dao = dao;
			this.streamDAO = streamDAO;
			this.registry = registry;
			ObjectViewCache objcache = new ObjectViewCache(streamDAO, registry);	
			this.objcache = objcache;
		}
		
		public void start()
		{
			List<Commit> L = dao.all();
			for(Commit commit : L)	
			{
				doObserve(commit);
			}
		}
		
		private void doObserve(Commit commit)
		{
			Long streamId = commit.getStreamId();
			Stream stream = null;
			if (streamId != null && streamId != 0L)
			{
				stream = streamDAO.findById(streamId);
			}

			objcache.observe(stream, commit);
		}

		public TopLevel createTopLevel() 
		{
			CommitCache cache = new CommitCache(dao);
			CommitMgr mgr = new CommitMgr(dao, streamDAO, cache);
			mgr.getMaxId(); //query db
			CommandProcessor proc = createProc(mgr);
			
			TopLevel toplevel = new TopLevel(proc, mgr);
			return toplevel;
		}
		
		abstract protected CommandProcessor createProc(CommitMgr mgr);
	}
	
	public static class MyPerm extends Permanent
	{

		public MyPerm(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry) 
		{
			super(dao, streamDAO, registry);
		}

		@Override
		protected CommandProcessor createProc(CommitMgr commitMgr)
		{
			return new MyCmdProc(commitMgr, registry, objcache);
		}
		
	}
	
	public static class TopLevel
	{
		CommandProcessor proc;
		private CommitMgr commitMgr;
		
		public TopLevel(CommandProcessor proc, CommitMgr mgr)
		{
			this.proc = proc;
			this.commitMgr = mgr;
		}

		public void process(ICommand cmd) 
		{
			proc.process(cmd);
		}
	}
	
	@Test
	public void test() throws Exception
	{
		//create long-running objects
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		
		ObjectManagerRegistry registry = new ObjectManagerRegistry();
		registry.register(Scooter.class, new ObjectMgr<Scooter>(Scooter.class));
		
		MyPerm perm = new MyPerm(dao, streamDAO, registry);
		perm.start();
		
		log(String.format("1st"));
		TopLevel toplevel = perm.createTopLevel();
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		toplevel.process(cmd);
		
		log(String.format("2nd"));
		toplevel = perm.createTopLevel();
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		log(String.format("2nd"));
		toplevel = perm.createTopLevel();
		ucmd = new UpdateScooterCmd();
		ucmd.s = "more2";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
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
