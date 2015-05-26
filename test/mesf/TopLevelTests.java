package mesf;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mef.framework.helpers.BaseTest;
import mesf.CommitMgrTests.InsertScooterCmd;
import mesf.CommitMgrTests.MyCmdProc;
import mesf.CommitMgrTests.UpdateScooterCmd;
import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.core.BaseObject;
import mesf.core.Commit;
import mesf.core.CommitCache;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.ObjectViewCache;
import mesf.core.Stream;
import mesf.view.BaseView;
import mesf.view.ViewLoader;
import mesf.view.ViewManager;

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
		protected ViewManager viewMgr;

		public Permanent(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry)
		{
			this.dao = dao;
			this.streamDAO = streamDAO;
			this.registry = registry;
			ObjectViewCache objcache = new ObjectViewCache(streamDAO, registry);	
			this.objcache = objcache;
			this.viewMgr = new ViewManager(streamDAO);
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
			viewMgr.observe(stream, commit);
		}

		public TopLevel createTopLevel() 
		{
			CommitCache cache = new CommitCache(dao);
			CommitMgr mgr = new CommitMgr(dao, streamDAO, cache);
			mgr.getMaxId(); //query db
			CommandProcessor proc = createProc(mgr);
			ViewLoader vloader = new ViewLoader(dao, streamDAO, mgr.getMaxId());
			
			TopLevel toplevel = new TopLevel(proc, mgr, vloader);
			return toplevel;
		}
		
		abstract protected CommandProcessor createProc(CommitMgr mgr);
		

		public BaseObject getObjectFromCache(long objectId) 
		{
			return objcache.getIfLoaded(objectId);
		}
		
		protected void registerViewObserver(BaseView view)
		{
			viewMgr.registerViewObserver(view);
		}
		public ViewManager getViewMgr()
		{
			return viewMgr;
		}
	}
	
	public static class TopLevel
	{
		CommandProcessor proc;
		private CommitMgr commitMgr;
		public ViewLoader vloader;
		
		public TopLevel(CommandProcessor proc, CommitMgr mgr, ViewLoader vloader)
		{
			this.proc = proc;
			this.commitMgr = mgr;
			this.vloader = vloader;
		}

		public void process(ICommand cmd) 
		{
			proc.process(cmd);
		}
	}
	
	public static class MyView extends BaseView
	{
		public Map<Long,Scooter> map = new HashMap<>();
		
		public int size()
		{
			return map.size();
		}

		@Override
		public boolean willAccept(Stream stream, Commit commit) 
		{
			if (stream != null && stream.getType().equals("scooter"))
			{
				return true;
			}
			return false;
		}

		@Override
		public void observe(Stream stream, Commit commit) 
		{
			switch(commit.getAction())
			{
			case 'I':
			case 'S':
				map.put(commit.getStreamId(), null);
				break;
			case 'U':
				break;
			case 'D':
				map.remove(commit.getStreamId());
				break;
			default:
				break;
			}
		}
		
	}
	
	public static class MyPerm extends Permanent
	{
		public MyView view1;
		
		public MyPerm(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry) 
		{
			super(dao, streamDAO, registry);
			
			view1 = new MyView();
			registerViewObserver(view1);
		}
		
		@Override
		protected CommandProcessor createProc(CommitMgr commitMgr)
		{
			return new MyCmdProc(commitMgr, registry, objcache);
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
		assertEquals(0, perm.view1.size());
		
		log(String.format("1st"));
		TopLevel toplevel = perm.createTopLevel();
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		toplevel.process(cmd);
		assertEquals(0, perm.view1.size()); //haven't done yet
		
		log(String.format("2nd"));
		toplevel = perm.createTopLevel();
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		
		//we don't have an event bus. so cmd processing does not update objcache
		//do this for two reasons
		// -so objects don't change partially way through a web request
		// -objcache is synchronized so is perf issue
		chkScooterStr(perm, ucmd.objectId, "bob");
		
		log(String.format("2nd"));
		toplevel = perm.createTopLevel();
		ucmd = new UpdateScooterCmd();
		ucmd.s = "more2";
		ucmd.objectId = 1L;
		toplevel.process(ucmd);
		chkScooterStr(perm, ucmd.objectId, "more");
		
		assertEquals(0, perm.view1.size()); //haven't done yet
		assertEquals(3, dao.size());
		ViewManager viewMgr = perm.getViewMgr();
		Object obj = viewMgr.loadView(perm.view1, toplevel.vloader);
		assertEquals(1, perm.view1.size()); 
	}

	
	private void chkScooterStr(MyPerm perm, long objectId, String string) 
	{
		Scooter scooter = (Scooter) perm.getObjectFromCache(objectId);
		assertEquals(string, scooter.getS());
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