package mesf;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import mesf.ObjManagerTests.Scooter;
import mesf.cache.CommitCache;
import mesf.cache.StreamCache;
import mesf.cmd.BaseCommand;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ICommand;
import mesf.core.CommitMgr;
import mesf.core.ICommitObserver;
import mesf.core.MContext;
import mesf.entity.Entity;
import mesf.entity.EntityLoader;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityMgr;
import mesf.entity.EntityRepository;
import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IEventRecordDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockEventRecordDAO;
import mesf.persistence.PersistenceContext;
import mesf.persistence.Stream;
import mesf.testhelper.FactoryGirl;

import org.junit.Before;
import org.junit.Test;

public class CommitMgrTests extends BaseMesfTest 
{
	public static class CountObserver implements ICommitObserver
	{
		String type;
		public int count;
		
		public CountObserver(String type)
		{
			this.type = type;
		}
		
		@Override
		public boolean willAccept(Stream stream, Commit commit) 
		{
			if (stream != null && ! stream.getType().equals(type))
			{
				return false;
			}
			
			char action = commit.getAction();
			return (action == 'I' || action == 'D');
		}

		@Override
		public void observe(MContext mtx, Stream stream, Commit commit) 
		{
			char action = commit.getAction();
			if (action == 'I')
			{
				count++;
			}
			else if (action == 'D')
			{
				count--;
			}
		}
	}
	
	public static class MultiObserver implements ICommitObserver
	{
		List<ICommitObserver> L = new ArrayList<>();
		
		public MultiObserver()
		{}

		@Override
		public boolean willAccept(Stream stream, Commit commit)
		{
			return true;
		}

		@Override
		public void observe(MContext mtx, Stream stream, Commit commit) 
		{
			for(ICommitObserver observer : L)
			{
				if (observer.willAccept(stream, commit))
				{
					observer.observe(mtx, stream, commit);
				}
			}
		}
	}
	
	
	public static class InsertScooterCmd extends BaseCommand
	{
		public int a;
		public String s;
	}
	public static class UpdateScooterCmd extends BaseCommand
	{
		public String s;
	}
	public static class DeleteScooterCmd extends BaseCommand
	{
	}
	
	public static class MyCmdProc extends CommandProcessor
	{
		public MyCmdProc()
		{
		}

		@Override
		public void process(ICommand cmd) 
		{
			try {
				if (cmd instanceof InsertScooterCmd)
				{
					doInsertScooterCmd((InsertScooterCmd)cmd);
				}
				else if (cmd instanceof UpdateScooterCmd)
				{
					doUpdateScooterCmd((UpdateScooterCmd)cmd);
				}
				else if (cmd instanceof DeleteScooterCmd)
				{
					doDeleteScooterCmd((DeleteScooterCmd)cmd);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void doDeleteScooterCmd(DeleteScooterCmd cmd) throws Exception 
		{
			Scooter scooter = loadEntity(cmd.getEntityId());
			if (scooter == null)
			{
				return; //!!
			}
			
			deleteEntity(scooter);
		}

		private Scooter loadEntity(long entityId) throws Exception 
		{
			Scooter scooter = (Scooter) mtx.loadEntity(Scooter.class, entityId);
			return scooter;
		}
		private void doUpdateScooterCmd(UpdateScooterCmd cmd) throws Exception 
		{
			Scooter scooter = loadEntity(cmd.getEntityId());
			if (scooter == null)
			{
				return; //!!
			}
			
			scooter.setS(cmd.s);
			updateEntity(scooter);
		}

		private void doInsertScooterCmd(InsertScooterCmd cmd) throws Exception
		{
			Scooter scooter = new Scooter();
			scooter.setA(cmd.a);
			scooter.setB(10);
			scooter.setS(cmd.s);
			
			insertEntity(cmd, scooter);
		}
	}

	ICommitDAO dao;
	IStreamDAO streamDAO;
	private CommitMgr createCommitMgr()
	{
		IEventRecordDAO eventDAO = new MockEventRecordDAO();
		PersistenceContext persistenceCtx = FactoryGirl.createPersistenceContext();
		this.dao = persistenceCtx.getDao();
		this.streamDAO = persistenceCtx.getStreamDAO();
		CommitMgr mgr = new CommitMgr(null, persistenceCtx, new CommitCache(dao), new StreamCache(streamDAO));
		return mgr;
	}
	@Test
	public void test() throws Exception
	{
		CommitMgr mgr = createCommitMgr();
		
		List<Commit> L = mgr.loadAll();
		assertEquals(0, L.size());
		
		mgr.writeNoOp();
		mgr.writeNoOp();
		L = mgr.loadAll();
		assertEquals(2, L.size());
		Commit commit = L.get(1);
		assertEquals(2L, commit.getId().longValue());
		assertEquals('-', commit.getAction());
		
		mgr.dump();
	}

	@Test
	public void testInsert() throws Exception
	{
		CommitMgr mgr = createCommitMgr();
		
		String json = "{'a':15,'b':26,'s':'abc'}";
		EntityMgr<Scooter> omgr = new EntityMgr(Scooter.class);
		Scooter scooter = omgr.createFromJson(fix(json));		

		mgr.writeNoOp();
		mgr.insertEntity(omgr, scooter);
		List<Commit> L = mgr.loadAll();
		assertEquals(2, L.size());
		
		chkStreamSize(streamDAO, 1);
		Stream stream = streamDAO.findById(1L);
		assertEquals("scooter", stream.getType());
		assertEquals(1L, stream.getId().longValue());
		assertEquals(2L, stream.getSnapshotId().longValue());
		
		scooter.clearSetList();
		scooter.setA(444);
		mgr.updateEntity(omgr, scooter);
		mgr.freshenMaxId(); //update maxid
		L = mgr.loadAll();
		assertEquals(3, L.size());
		chkStreamSize(streamDAO, 1);
		
		CountObserver observer = new CountObserver("scooter");
		mgr.observeList(mgr.loadAll(), observer);
		assertEquals(1, observer.count);
		
		mgr.deleteEntity(omgr, scooter);
		mgr.freshenMaxId(); //update maxid
		L = mgr.loadAll();
		assertEquals(4, L.size());
		chkStreamSize(streamDAO, 1);

		mgr.dump();
		observer = new CountObserver("scooter");
		mgr.observeList(mgr.loadAll(), observer);
		assertEquals(0, observer.count);
	}
	
	
	@Test
	public void testReadModelCache() throws Exception
	{
		CommitMgr mgr = createCommitMgr();
		EntityLoader oloader = mgr.createEntityLoader();
		
		String json = "{'a':15,'b':26,'s':'abc'}";
		EntityMgr<Scooter> omgr = new EntityMgr(Scooter.class);
		Scooter scooter = omgr.createFromJson(fix(json));		

		mgr.writeNoOp();
		mgr.insertEntity(omgr, scooter);
		List<Commit> L = mgr.loadAll();
		assertEquals(2, L.size());
		scooter.clearSetList();
		scooter.setA(444);
		mgr.updateEntity(omgr, scooter);
		mgr.freshenMaxId();
		L = mgr.loadAll();
		assertEquals(3, L.size());
		chkStreamSize(streamDAO, 1);
		oloader = mgr.createEntityLoader();
		
		mgr.dump();
		EntityManagerRegistry registry = new EntityManagerRegistry();
		registry.register(Scooter.class, new EntityMgr<Scooter>(Scooter.class));
		EntityRepository objcache = new EntityRepository(streamDAO, registry);
		
		Entity obj = objcache.loadEntity("scooter", scooter.getId(), oloader);
		assertEquals(1L, obj.getId().longValue());
		chkScooter((Scooter) obj, 444, 26, "abc");

		Entity obj2 = objcache.loadEntity("scooter", scooter.getId(), oloader);
		assertEquals(1L, obj2.getId().longValue());
		chkScooter((Scooter) obj2, 444, 26, "abc");
		
		assertEquals(1, objcache.getSize());
		
		//commit more
		long maxId = mgr.getMaxId();
		scooter.clearSetList();
		scooter.setA(555);
		mgr.updateEntity(omgr, scooter);
		mgr.dump();
		
		mgr.freshenMaxId();
		L = mgr.loadAllFrom(maxId + 1);
		assertEquals(1, L.size());
		mgr.observeList(mgr.loadAll(), objcache);
		Scooter scoot2 = (Scooter) objcache.getIfLoaded(scooter.getId());
		assertEquals(555, scoot2.getA());
		oloader = mgr.createEntityLoader();
	}

	@Test
	public void testCmd() throws Exception
	{
		CommitMgr commitMgr = createCommitMgr();

		EntityManagerRegistry registry = new EntityManagerRegistry();
		registry.register(Scooter.class, new EntityMgr<Scooter>(Scooter.class));
		EntityRepository objcache = new EntityRepository(streamDAO, registry);
		
		MContext mtx = new MContext(commitMgr, registry, null, objcache, null, null, null, null, null, null, null);
		MyCmdProc proc = new MyCmdProc();
		proc.setMContext(mtx);
		InsertScooterCmd cmd = new InsertScooterCmd();
		cmd.a = 15;
		cmd.s = "bob";
		proc.process(cmd);
		
		UpdateScooterCmd ucmd = new UpdateScooterCmd();
		ucmd.s = "more";
		ucmd.entityId = 1L;
		proc.process(ucmd);
		
		DeleteScooterCmd dcmd = new DeleteScooterCmd();
		dcmd.entityId = 1L;
		proc.process(dcmd);
		
		long oldMaxId = commitMgr.getMaxId();
		commitMgr.freshenMaxId();
		commitMgr.dump();
		
		List<Commit> L = commitMgr.loadAllFrom(oldMaxId + 1);
		CountObserver observer = new CountObserver("scooter");
		commitMgr.observeList(L, observer);
		log(String.format("n %d", observer.count));
	}
	
	//--helpers--
	private void chkStreamSize(IStreamDAO streamDAO, int expected)
	{
		assertEquals(expected, streamDAO.size());
	}
	private void chkScooter(Scooter scooter, int expectedA, int expectedB, String expectedStr)
	{
		assertEquals(expectedA, scooter.getA());
		assertEquals(expectedB, scooter.getB());
		assertEquals(expectedStr, scooter.getS());
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
