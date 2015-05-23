package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.ObjManagerTests.BaseObject;
import mesf.ObjManagerTests.IObjectMgr;
import mesf.core.Commit;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.Stream;

import org.junit.Before;
import org.junit.Test;

public class CommitMgrTests extends BaseTest 
{
	public static class CommitMgr
	{
		private ICommitDAO dao;
		private IStreamDAO streamDAO;

		public CommitMgr(ICommitDAO dao, IStreamDAO streamDAO)
		{
			this.dao = dao;
			this.streamDAO = streamDAO;
		}
		
		List<Commit> loadAll()
		{
			return dao.all();
		}
		List<Commit> loadAllFrom(long startId)
		{
			List<Commit> L = new ArrayList<>();
			for(Commit commit : loadAll())
			{
				if (commit.getId().longValue() >= startId)
				{
					L.add(commit);
				}
			}
			return L;
		}
		
		Commit loadByCommitId(Long id)
		{
			return dao.findById(id);
		}
		
		Commit loadSnapshotCommit(Long streamId)
		{
			Stream stream = streamDAO.findById(streamId);
			if (stream == null)
			{
				return null; //!!
			}
			
			Commit commit = dao.findById(stream.getSnapshotId());
			return commit;
		}
		List<Commit> loadStream(String type, Long id)
		{
			Stream stream = streamDAO.findById(id);
			if (stream == null)
			{
				return null; //!!
			}
			
			List<Commit> rawL = loadAllFrom(stream.getSnapshotId());
			List<Commit> L = new ArrayList<>();
			for(Commit commit : rawL)
			{
				if (commit.getStreamId().equals(id))
				{
					L.add(commit);
				}
			}
			return L;
		}
		
		public void writeNoOp()
		{
			Commit commit = new Commit();
			commit.setAction('-');
			this.dao.save(commit);
		}
		
		public void dump()
		{
			for(Commit commit : loadAll())
			{
				String s = String.format("[%d] %c <%d> json:%s", commit.getId(), commit.getAction(), commit.getStreamId(), commit.getJson());
				System.out.println(s);
			}
		}
		
		public void insertObject(IObjectMgr mgr, BaseObject obj)
		{
			Stream stream = new Stream();
			stream.setType("zx");
			this.streamDAO.save(stream);
			
			Long objectId = stream.getId();
			Commit commit = new Commit();
			commit.setAction('I');
			commit.setStreamId(objectId);
			String json = "";
			try {
				json = mgr.renderObject(obj);
			} catch (Exception e) {
				e.printStackTrace();  //!!handle later!!
			}
			commit.setJson(json);
			this.dao.save(commit);
			
			Long snapshotId = commit.getId();
			stream.setSnapshotId(snapshotId);
			this.streamDAO.update(stream);
		}
	}
	
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
		
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
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
		
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
