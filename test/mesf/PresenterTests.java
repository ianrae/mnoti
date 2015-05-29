package mesf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.UserTests.MyUserPerm;
import mesf.UserTests.MyUserProc;
import mesf.UserTests.User;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.cmd.ObjectCommand;
import mesf.cmd.ProcRegistry;
import mesf.core.BaseObject;
import mesf.core.MContext;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.Permanent;
import mesf.log.Logger;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockCommitDAO;
import mesf.persistence.MockStreamDAO;
import mesf.persistence.PersistenceContext;
import mesf.presenter.MethodInvoker;
import mesf.presenter.Reply;
import mesf.presenter.Request;
import mesf.readmodel.AllIdsRM;
import mesf.cmd.CommandProcessor;

import org.junit.Before;
import org.junit.Test;

/*
 * TaskTests and add a UserTaskRM, cascading delete
 * presenter, QryCmd
 * guava cache in scache,objcache,commitcache
 * metrics
 * logger and error tracker singletons
 * play 2.4
 * computerDatabase sample
 * snapshots
 */

public class PresenterTests extends BaseMesfTest 
{
	public static abstract class Presenter //extends CommandProcessor
	{
		protected Reply reply;
		
		public Presenter()
		{
			reply = new Reply();
		}
		
		public Reply process(Request request) 
		{
			String methodName = request.getClass().getName();
			int pos = methodName.lastIndexOf('.');
			if (pos > 0)
			{
				methodName = methodName.substring(pos + 1);
				pos = methodName.indexOf('$');
				if (pos > 0)
				{
					methodName = methodName.substring(pos + 1);
				}
			}
			methodName = "on" + methodName;
			Logger.log(String.format("[MEF] %s.%s ", this.getClass().getSimpleName(), methodName));
			
			MethodInvoker invoker = new MethodInvoker(this, methodName, Request.class);
			
			Object res = invoker.call(request, reply);			
			return reply;
		}
		
//		protected abstract HandlerResult beforeCmd(ICommand cmd);
//		protected abstract HandlerResult afterCmd(ICommand cmd);
	}
	
	public static class MyReply extends Reply
	{
		public int a;
	}
	
	public static class MyPres extends Presenter
	{
		private MyReply rr = new MyReply();
		
		public MyPres()
		{
			super();
			reply = rr;
		}
		
		public Reply onRequest(Request cmd)
		{
			Logger.log("i n d e xx");
			rr.setDestination(Reply.VIEW_INDEX);
			return reply;
		}
		
	}
	
	@Test
	public void test() throws Exception
	{
		MyPres pres = new MyPres();
		
		Request request = new Request();
		Reply reply = pres.process(request);
		
		assertNotNull(reply);
		
		
//		MyUserPerm perm = this.createPerm();
		
//		int n = 5; 
//		for(int i = 0; i < n; i++)
//		{
//			log(String.format("%d..	", i));
//			MContext mtx = perm.createMContext();
//			MyUserProc.InsertCmd cmd = new MyUserProc.InsertCmd();
//			cmd.a = 101+i;
//			cmd.s = String.format("bob%d", i+1);
//			CommandProcessor proc = mtx.findProc(User.class);
//			proc.process(cmd);
//			assertEquals(i+1, cmd.objectId); //!! we set this in proc (only on insert)
//		}
//		
//		MContext mtx = perm.createMContext();
//		mtx.acquire(perm.readModel1.getClass());
//		List<User> L = perm.readModel1.queryAll(mtx);
//		assertEquals(5, L.size());
//		for(User u : L)
//		{
//			assertNotNull(u);
//			log(u.getId().toString());
//		}
//		
//		log("again..");
//		n = 1; 
//		for(int i = 0; i < n; i++)
//		{
//			log(String.format("%d..	", i));
//			mtx = perm.createMContext();
//			MyUserProc.InsertCmd cmd = new MyUserProc.InsertCmd();
//			cmd.a = 101+i;
//			cmd.s = String.format("bob%d", i+1);
//			CommandProcessor proc = mtx.findProc(User.class);
//			proc.process(cmd);
//		}
//		
//		mtx = perm.createMContext();
//		mtx.acquire(perm.readModel1.getClass());
//		L = perm.readModel1.queryAll(mtx);
//		assertEquals(6, L.size());
//		for(User u : L)
//		{
//			assertNotNull(u);
//			log(u.getId().toString());
//		}
//		
//		
//		log("del..");
//		n = 1; 
//		for(int i = 0; i < n; i++)
//		{
//			log(String.format("%d..	", i));
//			mtx = perm.createMContext();
//			MyUserProc.DeleteCmd cmd = new MyUserProc.DeleteCmd();
//			cmd.objectId = 4;
//			CommandProcessor proc = mtx.findProc(User.class);
//			proc.process(cmd);
//		}
//		
//		mtx = perm.createMContext();
//		mtx.acquire(perm.readModel1.getClass());
//		L = perm.readModel1.queryAll(mtx);
//		assertEquals(5, L.size());
//		for(User u : L)
//		{
//			assertNotNull(u);
//			log(u.getId().toString());
//		}
//		
//		perm.readModel1.freshen(mtx);
	}

	
//	private void chkUserStr(MyPerm perm, long objectId, String string) 
//	{
//		User scooter = (User) perm.loadObjectFromRepo(objectId);
//		assertEquals(string, scooter.getS());
//	}


	//-----------------------
	private MyUserPerm createPerm() throws Exception
	{
		//create long-running objects
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		
		ObjectManagerRegistry registry = new ObjectManagerRegistry();
		registry.register(User.class, new ObjectMgr<User>(User.class));
		
		ProcRegistry procRegistry = new ProcRegistry();
		procRegistry.register(User.class, MyUserProc.class);
		
		PersistenceContext persistenceCtx = new PersistenceContext(dao, streamDAO);
		MyUserPerm perm = new MyUserPerm(persistenceCtx, registry, procRegistry);
		perm.start();
		return perm;
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
