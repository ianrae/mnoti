package mesf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import mesf.UserTests.MyUserPerm;
import mesf.UserTests.MyUserProc;
import mesf.UserTests.User;
import mesf.cmd.ProcRegistry;
import mesf.core.EventProjector;
import mesf.core.MContext;
import mesf.core.Permanent;
import mesf.core.Projector;
import mesf.entity.BaseEntity;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityMgr;
import mesf.entity.IEntityMgr;
import mesf.event.BaseEvent;
import mesf.event.BaseEventRehydrator;
import mesf.event.EventManagerRegistry;
import mesf.event.EventMgr;
import mesf.event.IEventMgr;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.Event;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IEventDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockCommitDAO;
import mesf.persistence.MockEventDAO;
import mesf.persistence.MockStreamDAO;
import mesf.persistence.PersistenceContext;
import mesf.persistence.Stream;
import mesf.presenter.MethodInvoker;
import mesf.presenter.NotAuthorizedException;
import mesf.presenter.NotLoggedInException;
import mesf.presenter.Reply;
import mesf.presenter.Request;
import mesf.readmodel.ReadModel;
import mesf.core.IEventObserver;

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
		public long insertEntity(BaseEntity obj)
		{
			String type = this.getEntityType(obj);
			IEntityMgr mgr = mtx.getRegistry().findByType(type);
			
			return mtx.getCommitMgr().insertEntity(mgr, obj);
		}
		
		public String getEntityType(BaseEntity obj)
		{
			String type = mtx.getRegistry().findTypeForClass(obj.getClass());
			return type;
		}
	}
	
	public static class EventWriter 
	{
		private MContext mtx;
		public EventWriter(MContext mtx)
		{
			this.mtx = mtx;
		}
		public void insertEvent(BaseEvent event)
		{
			String type = this.getEventType(event);
			IEventMgr mgr = mtx.getEventRegistry().findByType(type);
			
			Event record = new Event();
			record.setStreamId(event.getEntityId());
			
			record.setEventName(mgr.getTypeName());
			String json = null;
			try {
				json = mgr.renderEntity(event);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			record.setJson(json);
			this.mtx.getEventDAO().save(record);
			
			Logger.logDebug("EV [%d] %d %s", record.getId(), event.getEntityId(), record.getEventName());
		}
		
		
		public String getEventType(BaseEvent obj)
		{
			String type = mtx.getEventRegistry().findTypeForClass(obj.getClass());
			return type;
		}
	}
	
	public static class InterceptorContext
	{
		public boolean haltProcessing;
	}
	
	public interface IReqquestInterceptor
	{
		void process(Request request, InterceptorContext itx);
	}
	
	
	public static abstract class Presenter //extends CommandProcessor
	{
		protected Reply baseReply;
		protected MContext mtx;
		protected CommitWriter commitWriter;
		protected EventWriter eventWriter;
		protected List<IReqquestInterceptor> interceptL = new ArrayList<>();
		
		public Presenter(MContext mtx)
		{
			this.mtx = mtx;
			this.commitWriter = new CommitWriter(mtx);
			this.eventWriter = new EventWriter(mtx);
		}
		
		public void addInterceptor(IReqquestInterceptor intercept)
		{
			interceptL.add(intercept);
		}
		
		protected abstract Reply createReply();
		
		public Reply process(Request request) 
		{
			Reply reply = null;
			try
			{
				reply = doProcess(request);
			}
			catch(NotLoggedInException ex)
			{
				baseReply.setDestination(Reply.FOWARD_NOT_AUTHENTICATED);
			}
			catch(NotAuthorizedException ex)
			{
				baseReply.setDestination(Reply.FOWARD_NOT_AUTHORIZED);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Logger.log("cont-after-except");
//				this.addErrorException(e, "formatter");
				baseReply.setFailed(true);
				baseReply.setDestination(Reply.FOWARD_ERROR);
			}
			return reply;
		}
		
		private Reply doProcess(Request request) 
		{
			this.baseReply = this.createReply();
			String methodName = getMethodName(request);
			Logger.log(String.format("[MEF] %s.%s ", this.getClass().getSimpleName(), methodName));

			if (! processInterceptors(request))
			{
				return baseReply;
			}
			
			MethodInvoker invoker = new MethodInvoker(this, methodName, Request.class);
			invoker.call(request, baseReply);			
			
			afterRequest(request); //always do it
			
			return baseReply;
		}
		
		private boolean processInterceptors(Request request) 
		{
			InterceptorContext itx = new InterceptorContext();
			for(IReqquestInterceptor interceptor : this.interceptL)
			{
				interceptor.process(request, itx);
				if (itx.haltProcessing)
				{
					return false; //halt
				}
			}
			beforeRequest(request, itx);
			if (itx.haltProcessing)
			{
				return false; //halt
			}
			return true; //continue
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
		
		protected void beforeRequest(Request request, InterceptorContext itx)
		{}
		protected void afterRequest(Request request)
		{}
		
		protected void insertObject(BaseEntity obj)
		{
			this.commitWriter.insertEntity(obj);
		}
		protected void insertEvent(BaseEvent ev)
		{
			this.eventWriter.insertEvent(ev);
		}
	}
	
	public static class MyReply extends Reply
	{
		public int a;
	}
	
	public static class UserAddedEvent extends BaseEvent
	{
		public UserAddedEvent()
		{}
		public UserAddedEvent(long entityid)
		{
			super(entityid);
		}
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
			
			insertObject(scooter);
			insertEvent(new UserAddedEvent(scooter.getId()));
			reply.setDestination(Reply.VIEW_INDEX);
		}
		
		protected void beforeRequest(Request request, InterceptorContext itx)
		{
			trail.add("before");
		}
		protected void afterRequest(Request request)
		{
			trail.add("after");
		}
	}
	
	public static class MyEventSub extends ReadModel
	{
		public SfxTrail trail = new SfxTrail();
		
		public BaseEventRehydrator mmtx;
		
		@Override
		public boolean willAcceptEvent(BaseEvent event) 
		{
			return true;
		}

		@Override
		public void observeEvent(BaseEvent event) 
		{
			if (event instanceof UserAddedEvent)
			{
				Logger.log("wooohoo");
			}
		}

		@Override
		public void freshen(MContext mtx) 
		{
			EventProjector projector = mtx.createEventProjector();
			projector.run(mtx, this, this.lastEventId);
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
			
			MContext mmtx = perm.createMContext();
			eventSub.mmtx = new BaseEventRehydrator(mmtx);
			eventSub.freshen(mmtx); //run event publishing 
		}
	}
	
	private static class MyIntercept implements IReqquestInterceptor
	{
		public SfxTrail trail;
		public int interceptorType;

		@Override
		public void process(Request request, InterceptorContext itx) 
		{
			trail.add("MYINTERCEPT");
			if (interceptorType == 2)
			{
				itx.haltProcessing = true;
			}
		}
	}

	@Test
	public void testFullChain() throws Exception
	{
		runOnce("before;index;after", 1L, 0);
		runOnce("MYINTERCEPT;before;index;after", 1L, 1);
		runOnce("MYINTERCEPT", 0L, 2);
	}

	private void runOnce(String expected, long expectedMaxId, int interceptorType) throws Exception
	{
		MyUserPerm perm = this.createPerm();
		
		MContext mtx = perm.createMContext();
		MyPres pres = new MyPres(mtx);
		if (interceptorType > 0)
		{
			MyIntercept intercept = new MyIntercept();
			intercept.trail = pres.trail;
			intercept.interceptorType = interceptorType;
			pres.addInterceptor(intercept);
		}
		
		MyPres.InsertCmd cmd = pres.new InsertCmd();
		cmd.a = 101;
		cmd.s = String.format("bob");
		
		Reply reply = pres.process(cmd);
		
		assertEquals(expected, pres.trail.getTrail());
		long id = perm.createMContext().getMaxId();
		assertEquals(expectedMaxId, id); 
	}

	private MyEventSub eventSub;
	
	//-----------------------
	private MyUserPerm createPerm() throws Exception
	{
		//create long-running objects
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		IEventDAO eventDAO = new MockEventDAO();
		
		EntityManagerRegistry registry = new EntityManagerRegistry();
		registry.register(User.class, new EntityMgr<User>(User.class));
		
		ProcRegistry procRegistry = new ProcRegistry();
		procRegistry.register(User.class, MyUserProc.class);
		
		EventManagerRegistry evReg = new EventManagerRegistry();
		evReg.register(UserAddedEvent.class, new EventMgr<UserAddedEvent>(UserAddedEvent.class));
		
		PersistenceContext persistenceCtx = new PersistenceContext(dao, streamDAO, eventDAO);
		MyUserPerm perm = new MyUserPerm(persistenceCtx, registry, procRegistry, evReg);
		
		eventSub = new MyEventSub();
		perm.registerReadModel(eventSub);
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
