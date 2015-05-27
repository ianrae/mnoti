package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mef.framework.helpers.BaseTest;
import mesf.core.MockStreamDAO;
import mesf.core.Stream;
import mesf.core.IStreamDAO;
import mesf.core.ISegCacheLoader;
import mesf.core.SegmentedCache;

import org.junit.Before;
import org.junit.Test;

public class StreamTableCacheTests extends BaseTest
{
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
		
	}

	@Test
	public void test() 
	{
		IStreamDAO streamDAO = new MockStreamDAO();
		buildObjects(streamDAO);
		assertEquals(5, streamDAO.size());
		
		StreamTableCache cache = new StreamTableCache(streamDAO);
		long streamId = cache.findSnapshotId(2);
		assertEquals(11, streamId);
		streamId = cache.findSnapshotId(5);
		assertEquals(14, streamId);
	}

	private void buildObjects(IStreamDAO streamDAO) 
	{
		for(int i = 0; i < 5; i++)
		{
			Stream stream = new Stream();
			stream.setSnapshotId(10L + i);
			stream.setType("scooter");
			streamDAO.save(stream);
		}
	}
	

	@Before
	public void init()
	{
		super.init();
	}	

}
