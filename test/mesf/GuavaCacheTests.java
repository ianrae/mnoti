package mesf;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class GuavaCacheTests extends BaseMesfTest
{

	@Test
	public void test() 
	{
		Cache<Long, String> cache = CacheBuilder.newBuilder()
			    .maximumSize(4)
			    .build(); // look Ma, no CacheLoader

		assertEquals(0, cache.size());
		addToCache(cache, 4, 0);
		assertEquals(4, cache.size());
		dumpCache(cache);
		String s = cache.asMap().get(0L);
		assertEquals("value0", s);
		
		addToCache(cache, 2, 100);
		assertEquals(4, cache.size());
		dumpCache(cache);
		
		//oldest one is gone (is actually random)
		s = cache.asMap().get(2L);
		assertEquals(null, s);

		//add it back
		addToCache(cache, 1, 2);
		assertEquals(4, cache.size());
		dumpCache(cache);
		//oldest one is gone (is actually random)
		s = cache.asMap().get(2L);
		assertEquals("value2", s);
	}

	private void addToCache(Cache<Long, String> cache, int n, int valueOffset)
	{
		for(int i = 0; i < n; i++)
		{
			Long id = new Long(i + valueOffset);
			cache.put(id, String.format("value%d", i+valueOffset));
		}
	}
	private void dumpCache(Cache<Long, String> cache)
	{
		log("dump..");
		for(Long id : cache.asMap().keySet())
		{
			String s = cache.asMap().get(id);
			log(String.format("%d:%s", id, s ));
		}
		
	}
	
	
	@Before
	public void init()
	{
		super.init();
	}	
}
