package mesf.cache;

import java.util.List;

import mesf.config.Config;
import mesf.config.IConfig;
import mesf.log.Logger;
import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;

//thread-safe long running cache of commit DTOs
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
			//there is no el[0] so shift down
			//0,4 means load records 1..4
			List<Commit> L = dao.loadRange(startIndex + 1, n);
			Logger.logDebug("LD %d.%d (got %d)", startIndex,n, L.size());
			return L;
		}
		
	}
	
	ISegmentedCache<Commit> segcache;
	private ICommitDAO dao;
	
	public CommitCache(ICommitDAO dao)
	{
		this.dao = dao;
		int n = Config.getIntValue(IConfig.ConfigItem.COMMIT_CACHE_SEGMENT_SIZE);
		segcache = new SegmentedGuavaCache<Commit>();
		segcache.init(n, new CommitLoader());
	}
	
	public synchronized List<Commit> loadRange(long startIndex, long n) 
	{
		List<Commit> L = segcache.getRange(startIndex, n);
		return L;
	}
	
	public synchronized void clearLastSegment(long maxId)
	{
		segcache.clearLastSegment(maxId);
	}
}