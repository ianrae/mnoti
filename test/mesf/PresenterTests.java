package mesf;

import static org.junit.Assert.*;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.UserTests.MyUserPerm;
import mesf.UserTests.MyUserProc;
import mesf.UserTests.User;
import mesf.cmd.ICommand;
import mesf.cmd.ObjectCommand;
import mesf.cmd.ProcRegistry;
import mesf.core.BaseObject;
import mesf.core.IObjectMgr;
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
import org.mef.framework.sfx.SfxTrail;

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
	public static class CommitWriter 
	{
		private MContext mtx;
		public CommitWriter(MContext mtx)
		{
			this.mtx = mtx;
		}
		public long insertObject(ICommand cmd, BaseObject obj)
		{
			String type = this.getObjectType(obj);
			IObjectMgr mgr = mtx.getRegistry().findByType(type);
			
			return mtx.getCommitMgr().insertObject(mgr, obj);
		}
		
		public String getObjectType(BaseObject obj)
		{
			String type = mtx.getRegistry().findTypeForClass(obj.getClass());
			return type;
		}
	}
	
	
	public static abstract class Presenter //extends CommandProcessor
	{
		protected Reply baseReply;
		protected MContext mtx;
		protected CommitWriter commitWriter;
		
		public Presenter(MContext mtx)
		{
			this.mtx = mtx;
			this.commitWriter = new CommitWriter(mtx);
		}
		
		protected abstract Reply createReply();
		
		public Reply process(Request request) 
		{
			this.baseReply = this.createReply();
			String methodName = getMethodName(request);
			Logger.log(String.format("[MEF] %s.%s ", this.getClass().getSimpleName(), methodName));
		
			doBeforeAction(request);
			if (baseReply.getDestination() != Reply.VIEW_NONE)
			{
				return baseReply;
			}
			
			MethodInvoker invoker = new MethodInvoker(this, methodName, Request.class);
			invoker.call(request, baseReply);			
			doAfterAction(request); //always do it
			
			return baseReply;
		}
		
		private String getMethodName(Request request) 
		{
//			String methodName = request.getClass().getName();
			String methodName = request.getClass().getSimpleName(); //avoid MyPres$InsertCmd
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
			return methodName;
		}
		
		
		protected void doBeforeAction(Request request)
		{
			try 
			{
				beforeRequest(request);
			}
			catch (Exception e) 
			{
//				this.addErrorException(e, "formatter");
				baseReply.setFailed(true);
				baseReply.setDestination(Reply.FOWARD_ERROR);
			}
		}
		protected void doAfterAction(Request request)
		{
			try 
			{
				afterRequest(request);
			}
			catch (Exception e) 
			{
//				this.addErrorException(e, "formatter");
				baseReply.setFailed(true);
				baseReply.setDestination(Reply.FOWARD_ERROR);
			}
		}
		
		protected void beforeRequest(Request request)
		{}
		protected void afterRequest(Request request)
		{}
		
		
		protected void insertObject(ICommand cmd, BaseObject obj)
		{
			this.commitWriter.insertObject(cmd, obj);
		}
	}
	
	public static class MyReply extends Reply
	{
		public int a;
	}
	
	public static class MyPres extends Presenter
	{
		public class InsertCmd extends Request
		{
			public int a;
			public String s;
		}
		
		
		private MyReply reply = new MyReply();
		public SfxTrail trail = new SfxTrail();
		
		public MyPres(MContext mtx)
		{
			super(mtx);
		}
		
		protected Reply createReply()
		{
			return reply;
		}
		
		public void onRequest(Request cmd)
		{
			Logger.log("i n d e xx");
			trail.add("index");
			reply.setDestination(Reply.VIEW_INDEX);
		}
		public void onInsertCmd(InsertCmd cmd)
		{
			Logger.log("insert");
			trail.add("index");
			
			User scooter = new User();
			scooter.setA(cmd.a);
			scooter.setB(10);
			scooter.setS(cmd.s);
			
			insertObject(cmd, scooter);
			reply.setDestination(Reply.VIEW_INDEX);
		}
		
		protected void beforeRequest(Request request)
		{
			trail.add("before");
		}
		protected void afterRequest(Request request)
		{
			trail.add("after");
		}
	}
	
	@Test
	public void test() throws Exception
	{
		MyUserPerm perm = this.createPerm();
		MContext mtx = perm.createMContext();
		MyPres pres = new MyPres(mtx);
		
		Request request = new Request();
		Reply reply = pres.process(request);
		
		assertNotNull(reply);
		assertTrue(reply instanceof MyReply);
		assertEquals(Reply.VIEW_INDEX, reply.getDestination());
		
		log(pres.trail.getTrail());
	}	
	
	@Test
	public void test22() throws Exception
	{
		MyUserPerm perm = this.createPerm();
		
		int n = 1; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			MContext mtx = perm.createMContext();
			MyPres pres = new MyPres(mtx);
			MyPres.InsertCmd cmd = pres.new InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			
			Reply reply = pres.process(cmd);
			
			long id = perm.createMContext().getMaxId();
			assertEquals(i+1, id); 
		}
		
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
