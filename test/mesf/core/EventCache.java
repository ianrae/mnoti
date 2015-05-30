package mesf.core;

import java.util.List;

import mesf.log.Logger;
import mesf.persistence.EventRecord;
import mesf.persistence.IEventDAO;

//thread-safe long running cache of commit DTOs
public class EventCache
{
	class EventLoader implements ISegCacheLoader<EventRecord>
	{
		public EventLoader() 
		{
		}

		@Override
		public List<EventRecord> loadRange(long startIndex, long n) 
		{
			//there is no el[0] so shift down
			//0,4 means load records 1..4
			List<EventRecord> L = dao.loadRange(startIndex + 1, n);
			Logger.logDebug("LD %d.%d (got %d)", startIndex,n, L.size());
			return L;
		}
		
	}
	
	SegmentedCache<EventRecord> segcache;
	private IEventDAO dao;
	
	public EventCache(IEventDAO dao)
	{
		this.dao = dao;
		segcache = new SegmentedCache<EventRecord>(4, new EventLoader());
	}
	
	public synchronized List<EventRecord> loadRange(long startIndex, long n) 
	{
		List<EventRecord> L = segcache.getRange(startIndex, n);
		return L;
	}
	
	public synchronized void clearLastSegment(long maxId)
	{
		segcache.clearLastSegment(maxId);
	}
}