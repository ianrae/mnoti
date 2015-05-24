package mesf;

import static org.junit.Assert.assertEquals;

import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.core.Commit;
import mesf.core.CommitMgr;
import mesf.core.ICommitDAO;
import mesf.core.ISegCacheLoader;
import mesf.core.IStreamDAO;
import mesf.core.MockCommitDAO;
import mesf.core.MockStreamDAO;
import mesf.core.SegmentedCache;

import org.junit.Before;
import org.junit.Test;

public class CommitCacheTests extends BaseTest 
{
	public static class CommitCache
	{
		class CommitLoader implements ISegCacheLoader<Commit>
		{
			public CommitLoader() 
			{
			}

			@Override
			public List<Commit> loadRange(long startIndex, long n) 
			{
				System.out.println(String.format("LD %d.%d", startIndex,n));
				//there is no el[0] so shift down
				//0,4 means load records 1..4
				List<Commit> L = dao.loadRange(startIndex + 1, n);
				return L;
			}
			
		}
		
		SegmentedCache<Commit> segcache;
		private ICommitDAO dao;
		
		public CommitCache(ICommitDAO dao)
		{
			this.dao = dao;
			segcache = new SegmentedCache<Commit>(4, new CommitLoader());
		}
		
		public List<Commit> loadRange(long startIndex, long n) 
		{
			List<Commit> L = segcache.getRange(startIndex, n);
			return L;
		}
	}

	@Test
	public void test() throws Exception
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		CommitMgr mgr = new CommitMgr(dao, streamDAO);
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
