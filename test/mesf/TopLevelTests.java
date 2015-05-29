package mesf;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import mef.framework.helpers.BaseTest;
import mesf.CommitMgrTests.InsertScooterCmd;
import mesf.CommitMgrTests.MyCmdProc;
import mesf.CommitMgrTests.UpdateScooterCmd;
import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.cmd.ProcRegistry;
import mesf.core.CommitMgr;
import mesf.core.MContext;
import mesf.core.EntityManagerRegistry;
import mesf.core.EntityMgr;
import mesf.core.Permanent;
import mesf.persistence.PersistenceContext;
import mesf.persistence.Stream;
import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockCommitDAO;
import mesf.persistence.MockStreamDAO;
import mesf.readmodel.ReadModel;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

import org.junit.Before;
import org.junit.Test;

public class TopLevelTests extends BaseMesfTest 
{
	public static class MyReadModel extends ReadModel
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

		@Override
		public void freshen(MContext mtx) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static class MyPerm extends Permanent
	{
		public MyReadModel readModel1;
		
		public MyPerm(PersistenceContext persistenceCtx, EntityManagerRegistry registry, ProcRegistry procRegistry) 
		{
			super(persistenceCtx, registry, procRegistry);
			
			readModel1 = new MyReadModel();
			registerReadModel(readModel1);
		}
	}
	
	@Test
	public void test() throws Exception
	{
		//create long-running objects
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		
		EntityManagerRegistry registry = new EntityManagerRegistry();
		registry.register(Scooter.class, new EntityMgr<Scooter>(Scooter.class));
		
		ProcRegistry procRegistry = new ProcRegistry();
		procRegistry.register(Scooter.class, MyCmdProc.class);
		
		PersistenceContext persistenceCtx = new PersistenceContext(dao, streamDAO);
		MyPerm perm = new MyPerm(persistenceCtx, registry, procRegistry);
		perm.start();
		assertEquals(0, perm.readModel1.size());
		
		log(String.format("1st"));
		MContext mtx = perm.createMContext();
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		CommandProcessor proc = mtx.findProc(Scooter.class);
		proc.process(cmd);
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		assertEquals(1L, cmd.objectId); //!! we set this in proc (only on insert)
		
		log(String.format("2nd"));
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.objectId = 1L;
		proc.process(ucmd);
		
		//we don't have an event bus. so cmd processing does not update objcache
		//do this for two reasons
		// -so objects don't change partially way through a web request
		// -objcache is synchronized so is perf issue
		chkScooterStr(perm, ucmd.objectId, "bob");
		
		log(String.format("2nd"));
		mtx = perm.createMContext();
		proc = mtx.findProc(Scooter.class);
		ucmd = new UpdateScooterCmd();
		ucmd.s = "more2";
		ucmd.objectId = 1L;
		proc.process(ucmd);
		chkScooterStr(perm, ucmd.objectId, "more");
		
		assertEquals(0, perm.readModel1.size()); //haven't done yet
		assertEquals(3, dao.size());
		ReadModelRepository readmodelMgr = perm.getreadmodelMgr();
//		Object obj = readmodelMgr.loadReadModel(perm.readModel1, mtx.getVloader());
//		assertEquals(1, perm.readModel1.size()); 
	}

	
	private void chkScooterStr(MyPerm perm, long objectId, String string) 
	{
		Scooter scooter = (Scooter) perm.loadObjectFromRepo(objectId);
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
