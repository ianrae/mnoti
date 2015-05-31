package mesf.cache;

import java.util.List;

import mesf.config.Config;
import mesf.config.IConfig;
import mesf.log.Logger;
import mesf.persistence.EventRecord;
import mesf.persistence.IEventRecordDAO;

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
	
	ISegmentedCache<EventRecord> segcache;
	private IEventRecordDAO dao;
	
	public EventCache(IEventRecordDAO dao)
	{
		this.dao = dao;
		int n = Config.getIntValue(IConfig.ConfigItem.EVENT_CACHE_SEGMENT_SIZE);
		segcache = new SegmentedGuavaCache<EventRecord>();
		segcache.init(n, new EventLoader());
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