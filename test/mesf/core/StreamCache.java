package mesf.core;

import java.util.List;

import mesf.log.Logger;
import mesf.persistence.IStreamDAO;
import mesf.persistence.Stream;

//thread-safe long running cache of commit DTOs
public class StreamCache
{
	class StreamTableLoader implements ISegCacheLoader<Stream>
	{
		public StreamTableLoader() 
		{
		}

		@Override
		public List<Stream> loadRange(long startIndex, long n) 
		{
			List<Stream> L = dao.loadRange(startIndex + 1, n);
			Logger.logDebug("SLD %d.%d (got %d)", startIndex,n, L.size());
			return L;
		}
		
	}
	
	ISegmentedCache<Stream> segcache;
	private IStreamDAO dao;
	
	public StreamCache(IStreamDAO dao)
	{
		this.dao = dao;
		int n = MesfConfig.STREAM_CACHE_CHUNK_SIZE;
		segcache = new SegmentedGuavaCache<Stream>();
		segcache.init(n, new StreamTableLoader());
	}
	
	public synchronized long findSnapshotId(long entityId) 
	{
		List<Stream> L = segcache.getRange(entityId - 1, 1);
		if (L == null || L.size() < 1)
		{
			return 0L;
		}
		return L.get(0).getSnapshotId();
	}
	public synchronized Stream findStream(long entityId) 
	{
		List<Stream> L = segcache.getRange(entityId - 1, 1);
		if (L == null || L.size() < 1)
		{
			return null;
		}
		return L.get(0);
	}
	
	public synchronized void clearLastSegment(long maxId)
	{
		segcache.clearLastSegment(maxId);
	}
	
}