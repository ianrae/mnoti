package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;

import mef.framework.helpers.BaseTest;
import mesf.core.MockStreamDAO;
import mesf.core.Stream;
import mesf.core.IStreamDAO;
import mesf.core.StreamTableCache;

import org.junit.Before;
import org.junit.Test;

public class StreamTableCacheTests extends BaseTest
{
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
		
		Stream str = cache.findStream(5);
		assertEquals(14, str.getSnapshotId().longValue());
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
