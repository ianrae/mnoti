package mesf;

import static org.junit.Assert.assertEquals;
import mef.framework.helpers.BaseTest;
import mesf.CommitMgrTests.MyCmdProc;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.cmd.ObjectCommand;
import mesf.cmd.ProcRegistry;
import mesf.core.BaseObject;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MContext;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.Permanent;
import mesf.readmodel.ReadModelLoader;

import org.junit.Before;
import org.junit.Test;

public class UserTests extends BaseTest 
{
	public static class User extends BaseObject
	{
		private int a;
		private int b;
		private String s;

		public int getA() {
			return a;
		}
		public void setA(int a) 
		{
			setlist.add("a");
			this.a = a;
		}
		public int getB() {
			return b;
		}
		public void setB(int b) {
			setlist.add("b");
			this.b = b;
		}
		public String getS() {
			return s;
		}
		public void setS(String s) {
			setlist.add("s");
			this.s = s;
		}
		@Override
		public BaseObject clone() 
		{
			User copy = new User();
			copy.setId(getId()); //!
			copy.a = this.a;
			copy.b = this.b;
			copy.s = this.s;
			return copy;
		}

	}
	
	
	public static class MyUserProc extends CommandProcessor
	{
		public static class InsertUserCmd extends ObjectCommand
		{
			public int a;
			public String s;
		}
		public static class UpdateUserCmd extends ObjectCommand
		{
			public String s;
		}
		public static class DeleteUserCmd extends ObjectCommand
		{
		}
		
		
		public MyUserProc()
		{
		}

		@Override
		public void process(ICommand cmd) 
		{
			try {
				if (cmd instanceof InsertUserCmd)
				{
					doInsertUserCmd((InsertUserCmd)cmd);
				}
				else if (cmd instanceof UpdateUserCmd)
				{
					doUpdateUserCmd((UpdateUserCmd)cmd);
				}
				else if (cmd instanceof DeleteUserCmd)
				{
					doDeleteUserCmd((DeleteUserCmd)cmd);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void doDeleteUserCmd(DeleteUserCmd cmd) throws Exception 
		{
			User scooter = loadTheObject(cmd.getObjectId());
			if (scooter == null)
			{
				return; //!!
			}
			
			deleteObject(scooter);
		}

		private User loadTheObject(long objectId) throws Exception 
		{
			User scooter = (User) mtx.loadObject(User.class, objectId);
			return scooter;
		}
		private void doUpdateUserCmd(UpdateUserCmd cmd) throws Exception 
		{
			User scooter = loadTheObject(cmd.getObjectId());
			if (scooter == null)
			{
				return; //!!
			}
			
			scooter.setS(cmd.s);
			updateObject(scooter);
		}

		private void doInsertUserCmd(InsertUserCmd cmd) throws Exception
		{
			User scooter = new User();
			scooter.setA(cmd.a);
			scooter.setB(10);
			scooter.setS(cmd.s);
			
			insertObject(cmd, scooter);
		}
	}
	
	
	public static class MyUserPerm extends Permanent
	{
//		public MyReadModel readModel1;
		
		public MyUserPerm(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry, ProcRegistry procRegistry) 
		{
			super(dao, streamDAO, registry, procRegistry);
			
//			readModel1 = new MyReadModel();
//			registerReadModel(readModel1);
		}
		
		@Override
		protected CommandProcessor createProc(CommitMgr commitMgr, ReadModelLoader vloader)
		{
			MContext mtx = new MContext(commitMgr, registry, objectRepo, readmodelRepo, vloader);
			
			CommandProcessor proc = new MyCmdProc();
			proc.setMContext(mtx);
			return proc;
		}
	}
	
	@Test
	public void test() throws Exception
	{
		MyUserPerm perm = this.createPerm();
		
		log(String.format("1st"));
		MContext mtx = perm.createMContext();
		MyUserProc.InsertUserCmd cmd = new MyUserProc.InsertUserCmd();
		cmd.a = 15;
		cmd.s = "bob";
		CommandProcessor proc = mtx.findProd(User.class);
		proc.process(cmd);
		assertEquals(1L, cmd.objectId); //!! we set this in proc (only on insert)
		
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
//		registry.register(User.class, new ObjectMgr<User>(User.class));
		
		ProcRegistry procRegistry = new ProcRegistry();
		procRegistry.register(User.class, MyCmdProc.class);
		
		MyUserPerm perm = new MyUserPerm(dao, streamDAO, registry, procRegistry);
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
