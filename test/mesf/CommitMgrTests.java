package mesf;

import static org.junit.Assert.*;

import java.util.List;

import mef.framework.helpers.BaseTest;
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
	}
	
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
		
		List<Commit> L = mgr.loadAll();
		assertEquals(0, L.size());
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
