package mesf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import mef.framework.helpers.FactoryGirl;
import mef.framework.helpers.LocalMockBinder;
import mesf.TwixtFormTests.CarTwixt;
import mesf.UserTests.MyUserPerm;
import mesf.UserTests.MyUserProc;
import mesf.UserTests.User;
import mesf.cmd.ProcRegistry;
import mesf.core.EventProjector;
import mesf.core.IDomainIntializer;
import mesf.core.MContext;
import mesf.core.Permanent;
import mesf.core.Projector;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityMgr;
import mesf.event.Event;
import mesf.event.BaseEventRehydrator;
import mesf.event.EventManagerRegistry;
import mesf.event.EventMgr;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IEventRecordDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockCommitDAO;
import mesf.persistence.MockEventRecordDAO;
import mesf.persistence.MockStreamDAO;
import mesf.persistence.PersistenceContext;
import mesf.persistence.Stream;
import mesf.presenter.IFormBinder;
import mesf.presenter.IReqquestInterceptor;
import mesf.presenter.InterceptorContext;
import mesf.presenter.Presenter;
import mesf.presenter.Reply;
import mesf.presenter.Request;
import mesf.readmodel.ReadModel;
import mesf.core.IEventObserver;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.sfx.SfxTrail;
import org.mef.twixt.StringValue;
import org.mef.twixt.binder.TwixtForm;

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
	public static class MyReply extends Reply
	{
		public int a;
	}
	
	public static class UserTwixt extends TwixtForm
	{
		public StringValue s;
		
		public UserTwixt()
		{
			s = new StringValue();
		}
	}
	
	public static class UserAddedEvent extends Event
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
		public static class UpdateCmd extends Request
		{
			public UpdateCmd(long id, IFormBinder binder)
			{
				this.entityId = id;
				this.binder = binder;
			}
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
		public void onUpdateCmd(UpdateCmd cmd) throws Exception
		{
			Logger.log("update");
			trail.add("update");
			
			if (cmd.getFormBinder().bind())
			{
				UserTwixt twixt = (UserTwixt) cmd.getFormBinder().get();
				Logger.log("twixt a=%s", twixt.s);
				User scooter = (User) mtx.loadEntity(User.class, cmd.getEntityId());
				twixt.copyTo(scooter);
				updateObject(scooter);
			}
			
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
		
		@Override
		public boolean willAcceptEvent(Event event) 
		{
			return true;
		}

		@Override
		public void observeEvent(Event event) 
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
		
		int n = 2; 
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
			
			mtx = perm.createMContext();
			eventSub.freshen(mtx); //run event publishing 
		}
	}
	
	@Test
	public void test3() throws Exception
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
			
			mtx = perm.createMContext();
			pres = new MyPres(mtx);
			LocalMockBinder<UserTwixt> binder = new LocalMockBinder<UserTwixt>(UserTwixt.class, buildMap());
			
			MyPres.UpdateCmd ucmd = new MyPres.UpdateCmd(1L, binder);
			reply = pres.process(ucmd);
		}
	}
	
	
	private Map<String,String> buildMap()
	{
		Map<String,String> map = new TreeMap<String,String>();
		map.put("s", "abc");
		
		return map;
	}
	
	private static class MyIntercept implements IReqquestInterceptor
	{
		public SfxTrail trail;
		public int interceptorType;

		@Override
		public void process(Request request, Reply reply, InterceptorContext itx) 
		{
			trail.add("MYINTERCEPT");
			if (interceptorType == 2)
			{
				itx.haltProcessing = true;
			}
		}
	}
	private static class BindingIntercept implements IReqquestInterceptor
	{

		@Override
		public void process(Request request, Reply reply, InterceptorContext itx) 
		{
			if (request.getFormBinder() == null)
			{
				return;
			}
			
			if (! request.getFormBinder().bind())
			{
				//propogate validation errors
				//set reply to VIEW_EDIT -pass in ctor Boundary.creatPres(new FormBinder<User>(VIEW_EDIT);
				//nice then onUpdate only called if valid
				itx.haltProcessing = true;
			}		
		}
	}
	
	public static class UserInitializer implements IDomainIntializer
	{

		@Override
		public void init(Permanent perm)
		{
			//create long-running objects
			
			EntityManagerRegistry registry = perm.getEntityManagerRegistry();
			registry.register(User.class, new EntityMgr<User>(User.class));
			
			ProcRegistry procRegistry = perm.getProcRegistry();
			procRegistry.register(User.class, MyUserProc.class);
			
			EventManagerRegistry evReg = perm.getEventManagerRegistry();
			evReg.register(UserAddedEvent.class, new EventMgr<UserAddedEvent>(UserAddedEvent.class));
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
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		MyUserPerm perm = new MyUserPerm(persistenceCtx);
		
		UserInitializer userinit = new UserInitializer();
		userinit.init(perm);
		
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
