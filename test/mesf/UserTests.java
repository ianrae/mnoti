package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mef.framework.helpers.BaseTest;
import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.cmd.ObjectCommand;
import mesf.cmd.ProcRegistry;
import mesf.core.BaseObject;
import mesf.core.Commit;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.ICommitObserver;
import mesf.core.IStreamDAO;
import mesf.core.MContext;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectMgr;
import mesf.core.Permanent;
import mesf.core.Projector;
import mesf.core.Stream;
import mesf.readmodel.ReadModel;
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
		public static class InsertCmd extends ObjectCommand
		{
			public int a;
			public String s;
		}
		public static class UpdateCmd extends ObjectCommand
		{
			public String s;
		}
		public static class DeleteCmd extends ObjectCommand
		{
		}
		
		
		public MyUserProc()
		{
		}

		@Override
		public void process(ICommand cmd) 
		{
			try {
				if (cmd instanceof InsertCmd)
				{
					doInsertCmd((InsertCmd)cmd);
				}
				else if (cmd instanceof UpdateCmd)
				{
					doUpdateCmd((UpdateCmd)cmd);
				}
				else if (cmd instanceof DeleteCmd)
				{
					doDeleteCmd((DeleteCmd)cmd);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void doDeleteCmd(DeleteCmd cmd) throws Exception 
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
		private void doUpdateCmd(UpdateCmd cmd) throws Exception 
		{
			User scooter = loadTheObject(cmd.getObjectId());
			if (scooter == null)
			{
				return; //!!
			}
			
			scooter.setS(cmd.s);
			updateObject(scooter);
		}

		private void doInsertCmd(InsertCmd cmd) throws Exception
		{
			User scooter = new User();
			scooter.setA(cmd.a);
			scooter.setB(10);
			scooter.setS(cmd.s);
			
			insertObject(cmd, scooter);
		}
	}
	
	public static class UsersRM extends ReadModel
	{
		public Map<Long,BaseObject> map = new TreeMap<>(); //sorted
		
		public int size()
		{
			return map.size();
		}

		@Override
		public boolean willAccept(Stream stream, Commit commit) 
		{
			if (stream != null && stream.getType().equals("user"))
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
		
		public void freshen(MContext mtx)
		{
			Projector projector = mtx.createProjector();
					
			projector.run(mtx, this, this.lastCommitId);
		}
		public List<User> queryAll(MContext mtx) throws Exception
		{
			List<User> L = new ArrayList<>();
			for(Long id : map.keySet())
			{
				BaseObject obj = mtx.loadObject(User.class, id);
				L.add((User) obj);
			}
			return L;
		}
		
	}
	
	
	public static class MyUserPerm extends Permanent
	{
		public UsersRM readModel1;
		
		public MyUserPerm(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry, ProcRegistry procRegistry) 
		{
			super(dao, streamDAO, registry, procRegistry);
			
			readModel1 = new UsersRM();
			registerReadModel(readModel1);
		}
		
	}
	
	@Test
	public void test() throws Exception
	{
		MyUserPerm perm = this.createPerm();
		
		int n = 5; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			MContext mtx = perm.createMContext();
			MyUserProc.InsertCmd cmd = new MyUserProc.InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			CommandProcessor proc = mtx.findProd(User.class);
			proc.process(cmd);
			assertEquals(i+1, cmd.objectId); //!! we set this in proc (only on insert)
		}
		
		MContext mtx = perm.createMContext();
		perm.readModel1.freshen(mtx);
		List<User> L = perm.readModel1.queryAll(mtx);
		assertEquals(5, L.size());
		for(User u : L)
		{
			assertNotNull(u);
			log(u.getId().toString());
		}
		
		log("again..");
		n = 1; 
		for(int i = 0; i < n; i++)
		{
			log(String.format("%d..	", i));
			mtx = perm.createMContext();
			MyUserProc.InsertCmd cmd = new MyUserProc.InsertCmd();
			cmd.a = 101+i;
			cmd.s = String.format("bob%d", i+1);
			CommandProcessor proc = mtx.findProd(User.class);
			proc.process(cmd);
		}
		
		mtx = perm.createMContext();
		perm.readModel1.freshen(mtx);;
		L = perm.readModel1.queryAll(mtx);
		assertEquals(6, L.size());
		for(User u : L)
		{
			assertNotNull(u);
			log(u.getId().toString());
		}
		
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
