package mesf.core;

import java.util.List;

public class CommitCache
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
	
	public void clearLastSegment()
	{
		segcache.clearLastSegment();
	}
}