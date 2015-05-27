package mesf.core;

import java.util.List;

//thread-safe long running cache of commit DTOs
public class StreamTableCache
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
			System.out.println(String.format("SLD %d.%d (got %d)", startIndex,n, L.size()));
			return L;
		}
		
	}
	
	SegmentedCache<Stream> segcache;
	private IStreamDAO dao;
	
	public StreamTableCache(IStreamDAO dao)
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
	
}