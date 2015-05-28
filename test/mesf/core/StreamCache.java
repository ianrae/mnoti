package mesf.core;

import java.util.List;

import mesf.log.Logger;

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
	
	SegmentedCache<Stream> segcache;
	private IStreamDAO dao;
	
	public StreamCache(IStreamDAO dao)
	{
		this.dao = dao;
		segcache = new SegmentedCache<Stream>(4, new StreamTableLoader());
	}
	
	public synchronized long findSnapshotId(long objectId) 
	{
		List<Stream> L = segcache.getRange(objectId - 1, 1);
		if (L == null || L.size() < 1)
		{
			return 0L;
		}
		return L.get(0).getSnapshotId();
	}
	public synchronized Stream findStream(long objectId) 
	{
		List<Stream> L = segcache.getRange(objectId - 1, 1);
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