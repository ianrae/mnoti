package mesf;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import mesf.PresenterTests.UserInitializer;
import mesf.UserTests.MyUserProc;
import mesf.UserTests.User;
import mesf.cmd.CommandProcessor;
import mesf.core.IDomainIntializer;
import mesf.core.MContext;
import mesf.core.Permanent;
import mesf.entity.BaseEntity;
import mesf.entity.Entity;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityMgr;
import mesf.event.EventManagerRegistry;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.PersistenceContext;
import mesf.presenter.BindingIntercept;
import mesf.presenter.IFormBinder;
import mesf.presenter.InterceptorContext;
import mesf.presenter.Presenter;
import mesf.presenter.Reply;
import mesf.presenter.Request;
import mesf.readmodel.ManyToOneRM;
import mesf.testhelper.FactoryGirl;
import mesf.testhelper.LocalMockBinder;
import mesf.util.SfxTrail;

import org.junit.Before;
import org.junit.Test;
import org.mef.twixt.StringValue;
import org.mef.twixt.Value;
import org.mef.twixt.binder.TwixtForm;
import org.mef.twixt.validate.ValContext;


public class TaskTests extends BaseMesfTest 
{
	public static class Task extends BaseEntity
	{
		private String s;
		private Long userId;

		public String getS() {
			return s;
		}
		public void setS(String s) {
			setlist.add("s");
			this.s = s;
		}
		public Long getUserId() {
			return userId;
		}
		public void setUserId(Long userId) {
			setlist.add("userId");
			this.userId = userId;
		}
	}


	public static class TaskReply extends Reply
	{
		public int a;
	}

	public static class TaskTwixt extends TwixtForm
	{
		public StringValue s;

		public TaskTwixt()
		{
			s = new StringValue();

			s.setValidator( (ValContext valctx, Value obj) -> {
				StringValue val = (StringValue) obj;
				if (! val.get().contains("a"))
				{
					valctx.addError("sdfdfs");
				}
			});
		}
	}

	public static class UserTaskRM extends ManyToOneRM
	{
		public static class UserTaskResolver implements ManyToOneRM.IResolver
		{
			@Override
			public Long getForiegnKey(MContext mtx, Commit commit) 
			{
				Task task = (Task) mtx.loadEntitySafe(Task.class, commit.getStreamId());
				return task.userId;
			}

		}
		public UserTaskRM()
		{
			super("user", User.class, "task", Task.class, new UserTaskResolver());
		}
	}

	public static class TaskInitializer implements IDomainIntializer
	{
		private Permanent perm;

		@Override
		public void init(Permanent perm)
		{
			//create long-running objects
			this.perm = perm;
			EntityManagerRegistry registry = perm.getEntityManagerRegistry();
			registry.register(Task.class, new EntityMgr<Task>(Task.class));
		}

		TaskPresenter createMyPres(MContext mtx)
		{
			TaskPresenter pres = new TaskPresenter(mtx);
			return pres;
		}
		TaskPresenter createMyPres(MContext mtx, int failDestination)
		{
			TaskPresenter pres = new TaskPresenter(mtx);
			pres.addInterceptor(new BindingIntercept(failDestination));
			return pres;
		}
	}
	public static class MyTaskPerm extends Permanent
	{
		public UserTaskRM readModel1;
		public TaskInitializer taskInit;
		public UserInitializer userInit;

		public MyTaskPerm(PersistenceContext persistenceCtx) 
		{
			super(persistenceCtx);

			readModel1 = new UserTaskRM();
			registerReadModel(readModel1);

			taskInit = new TaskInitializer();
			userInit = new UserInitializer();			
		}
	}


	public static class TaskPresenter extends Presenter
	{
		public static class InsertCmd extends Request
		{
			public InsertCmd(long userId) {
				this.userId = userId;
			}
			public int a;
			public String s;
			public long userId;
		}
		public static class UpdateCmd extends Request
		{
			public UpdateCmd(long id, IFormBinder binder)
			{
				this.entityId = id;
				this.binder = binder;
			}
		}

		private TaskReply reply = new TaskReply();
		public SfxTrail trail = new SfxTrail();

		public TaskPresenter(MContext mtx)
		{
			super(mtx);
		}
		protected Reply createReply()
		{
			return reply;
		}

		public void onInsertCmd(InsertCmd cmd)
		{
			Logger.log("insert");
			trail.add("index");

			Task scooter = new Task();
			scooter.setS(cmd.s);
			scooter.setUserId(cmd.userId);

			insertEntity(scooter);
			reply.setDestination(Reply.VIEW_INDEX);
		}

		public void onUpdateCmd(UpdateCmd cmd) throws Exception 
		{
			Logger.log("update");
			trail.add("update");
			//binding fails handled in interceptor
			TaskTwixt twixt = (TaskTwixt) cmd.getFormBinder().get();
			Logger.log("twixt a=%s", twixt.s);
			Task scooter = loadEntity(cmd);
			twixt.copyTo(scooter);
			updateEntity(scooter);
			reply.setDestination(Reply.VIEW_INDEX);
		}

		private Task loadEntity(Request cmd) throws Exception
		{
			Task scooter = null;
			scooter = (Task) mtx.loadEntity(Task.class, cmd.getEntityId());
			return scooter;
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


	@Test
	public void test3() throws Exception
	{
		MyTaskPerm perm = this.createPerm();

		createUsers(perm, 4);

		int n = 1; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			MContext mtx = perm.createMContext();
			TaskPresenter pres = new TaskPresenter(mtx);
			long userId = 2;
			TaskPresenter.InsertCmd cmd = new TaskPresenter.InsertCmd(userId);
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			Reply reply = pres.process(cmd);

			mtx = perm.createMContext();
			pres = perm.taskInit.createMyPres(mtx, Reply.VIEW_EDIT);
			LocalMockBinder<TaskTwixt> binder = new LocalMockBinder<TaskTwixt>(TaskTwixt.class, buildMap(true));
			TaskPresenter.UpdateCmd ucmd = new TaskPresenter.UpdateCmd(4 + 1L, binder);
			reply = pres.process(ucmd);

			mtx = perm.createMContext();
			pres = perm.taskInit.createMyPres(mtx);
			cmd = new TaskPresenter.InsertCmd(userId);
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			reply = pres.process(cmd);
			
			mtx = perm.createMContext(); //recalc maxid
			log(String.format("acquire.. max=%d", mtx.getMaxId()));
			mtx.acquire(perm.readModel1.getClass());
			Map<Long,Long> map = perm.readModel1.queryAll(mtx, 2L);
			assertEquals(2, map.size());
			assertEquals(0L, map.get(5L).longValue());
			assertEquals(null, map.get(4L));
		}
	}

//this test changes static vars so may mess up other tests	
//	@Test
//	public void testChunk() throws Exception
//	{
//		MesfConfig.COMMIT_CACHE_CHUNK_SIZE = 100;
//		MesfConfig.EVENT_CACHE_CHUNK_SIZE = 100;
//		MesfConfig.STREAM_CACHE_CHUNK_SIZE = 100;
//		test3();
//	}

	private void createUsers(MyTaskPerm perm, int n)
	{
		for(int i = 0; i < n; i++)
		{
			log(String.format("user%d..	", i));
			MContext mtx = perm.createMContext();
			MyUserProc.InsertCmd cmd = new MyUserProc.InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			CommandProcessor proc = mtx.findProc(User.class);
			proc.process(cmd);
		}
	}

	@Test
	public void testValFail() throws Exception
	{
		MyTaskPerm perm = this.createPerm();

		MContext mtx = perm.createMContext();
		TaskPresenter pres = perm.taskInit.createMyPres(mtx);
		TaskPresenter.InsertCmd cmd = new TaskPresenter.InsertCmd(1L);
		cmd.a = 101;
		cmd.s = String.format("bob%d", 1);
		Reply reply = pres.process(cmd);

		mtx = perm.createMContext();
		pres = perm.taskInit.createMyPres(mtx, Reply.VIEW_EDIT);
		LocalMockBinder<TaskTwixt> binder = new LocalMockBinder<TaskTwixt>(TaskTwixt.class, buildMap(false));

		TaskPresenter.UpdateCmd ucmd = new TaskPresenter.UpdateCmd(1L, binder);
		reply = pres.process(ucmd);
		assertEquals(Reply.VIEW_EDIT, reply.getDestination());
	}

	private Map<String,String> buildMap(boolean okValues)
	{
		Map<String,String> map = new TreeMap<String,String>();
		if (okValues)
		{
			map.put("s", "abc");
		}
		else
		{
			map.put("s", "bb");
		}

		return map;
	}


	//-----------------------



	private MyTaskPerm createPerm() throws Exception
	{
		//create long-running objects
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		MyTaskPerm perm = new MyTaskPerm(persistenceCtx);

		perm.taskInit.init(perm);
		perm.userInit.init(perm);

		//		eventSub = new MyEventSub();
		//		perm.registerReadModel(eventSub);
		//		perm.start();
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
