package mesf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.core.Commit;
import mesf.core.CommitCache;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.StreamCache;

import org.junit.Before;
import org.junit.Test;

public class CommitCacheTests extends BaseTest 
{
	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO, new CommitCache(dao), new StreamCache(streamDAO));
		int n = 6;
		for(int i = 0; i < n; i++)
		{
			mgr.writeNoOp();
		}
		
		mgr.dump();
		assertEquals(n, dao.size());
		
		CommitCache cache = new CommitCache(dao);
		List<Commit> L = cache.loadRange(0, n);
		for(Commit commit : L)
		{
			log(commit.getId().toString());
		}
		log("again..");
		L = cache.loadRange(0, n);
		for(Commit commit : L)
		{
			log(commit.getId().toString());
		}
	}

	//-----------------------------
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
