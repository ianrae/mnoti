package mesf;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import mesf.core.IDomainIntializer;
import mesf.core.MContext;
import mesf.core.Permanent;
import mesf.entity.BaseEntity;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityMgr;
import mesf.event.EventManagerRegistry;
import mesf.log.Logger;
import mesf.persistence.PersistenceContext;
import mesf.presenter.BindingIntercept;
import mesf.presenter.IFormBinder;
import mesf.presenter.InterceptorContext;
import mesf.presenter.Presenter;
import mesf.presenter.Reply;
import mesf.presenter.Request;
import mesf.testhelper.FactoryGirl;
import mesf.testhelper.LocalMockBinder;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.sfx.SfxTrail;
import org.mef.twixt.StringValue;
import org.mef.twixt.Value;
import org.mef.twixt.binder.TwixtForm;
import org.mef.twixt.validate.ValContext;


public class TaskTests extends BaseMesfTest 
{
	public static class Task extends BaseEntity
	{
		private String s;

		public String getS() {
			return s;
		}
		public void setS(String s) {
			setlist.add("s");
			this.s = s;
		}
		@Override
		public BaseEntity clone() 
		{
			Task copy = new Task();
			copy.setId(getId()); //!
			copy.s = this.s;
			return copy;
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

	public static class TaskPresenter extends Presenter
	{
		public static class InsertCmd extends Request
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

			insertObject(scooter);
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
			updateObject(scooter);
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

		int n = 1; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			MContext mtx = perm.createMContext();
			TaskPresenter pres = new TaskPresenter(mtx);
			TaskPresenter.InsertCmd cmd = new TaskPresenter.InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			Reply reply = pres.process(cmd);

			mtx = perm.createMContext();
			pres = createMyPres(mtx, perm, Reply.VIEW_EDIT);
			LocalMockBinder<TaskTwixt> binder = new LocalMockBinder<TaskTwixt>(TaskTwixt.class, buildMap(true));

			TaskPresenter.UpdateCmd ucmd = new TaskPresenter.UpdateCmd(1L, binder);
			reply = pres.process(ucmd);
		}
	}

	@Test
	public void testValFail() throws Exception
	{
		MyTaskPerm perm = this.createPerm();

		MContext mtx = perm.createMContext();
		TaskPresenter pres = createMyPres(mtx, perm);
		TaskPresenter.InsertCmd cmd = new TaskPresenter.InsertCmd();
		cmd.a = 101;
		cmd.s = String.format("bob%d", 1);
		Reply reply = pres.process(cmd);

		mtx = perm.createMContext();
		pres = createMyPres(mtx, perm, Reply.VIEW_EDIT);
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

	public static class TaskInitializer implements IDomainIntializer
	{
		@Override
		public void init(Permanent perm)
		{
			//create long-running objects
			EntityManagerRegistry registry = perm.getEntityManagerRegistry();
			registry.register(Task.class, new EntityMgr<Task>(Task.class));
		}

	}
	public static class MyTaskPerm extends Permanent
	{
//		public TasksRM readModel1;
		
		public MyTaskPerm(PersistenceContext persistenceCtx) 
		{
			super(persistenceCtx);
			
//			readModel1 = new UsersRM();
//			registerReadModel(readModel1);
		}
	}


	//-----------------------
	TaskPresenter createMyPres(MContext mtx, Permanent perm)
	{
		TaskPresenter pres = new TaskPresenter(mtx);
		return pres;
	}
	TaskPresenter createMyPres(MContext mtx, Permanent perm, int failDestination)
	{
		TaskPresenter pres = new TaskPresenter(mtx);
		pres.addInterceptor(new BindingIntercept(failDestination));
		return pres;
	}


	private MyTaskPerm createPerm() throws Exception
	{
		//create long-running objects
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		MyTaskPerm perm = new MyTaskPerm(persistenceCtx);

		TaskInitializer init = new TaskInitializer();
		init.init(perm);

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
