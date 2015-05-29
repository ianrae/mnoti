package mesf.core;

import java.util.List;

import mesf.log.Logger;
import mesf.persistence.Event;
import mesf.persistence.IEventDAO;

//thread-safe long running cache of commit DTOs
public class EventCache
{
	class EventLoader implements ISegCacheLoader<Event>
	{
		public EventLoader() 
		{
		}

		@Override
		public List<Event> loadRange(long startIndex, long n) 
		{
			//there is no el[0] so shift down
			//0,4 means load records 1..4
			List<Event> L = dao.loadRange(startIndex + 1, n);
			Logger.logDebug("LD %d.%d (got %d)", startIndex,n, L.size());
			return L;
		}
		
	}
	
	SegmentedCache<Event> segcache;
	private IEventDAO dao;
	
	public EventCache(IEventDAO dao)
	{
		this.dao = dao;
		segcache = new SegmentedCache<Event>(4, new EventLoader());
	}
	
	public synchronized List<Event> loadRange(long startIndex, long n) 
	{
		List<Event> L = segcache.getRange(startIndex, n);
		return L;
	}
	
	public synchronized void clearLastSegment(long maxId)
	{
		segcache.clearLastSegment(maxId);
	}
}