package mesf;

import static org.junit.Assert.assertEquals;
import mesf.cache.StreamCache;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockStreamDAO;
import mesf.persistence.Stream;

import org.junit.Before;
import org.junit.Test;

public class StreamCacheTests extends BaseMesfTest
{
	@Test
	public void test() 
	{
		IStreamDAO streamDAO = new MockStreamDAO();
		buildObjects(streamDAO);
		assertEquals(5, streamDAO.size());
		
		StreamCache cache = new StreamCache(streamDAO);
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
