package mesf;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import mesf.cache.ISegCacheLoader;
import mesf.cache.SegmentedCache;

import org.junit.Before;
import org.junit.Test;

public class IteratorTests extends BaseMesfTest
{
	public static class MyIter implements Iterable<String>{
	    public String[] a=null; //make this final if you can
	    public MyIter(String[] arr){
	        a=arr; //maybe you should copy this array, for fear of external modification
	    }

	    //the interface is sufficient here, the outside world doesn't need to know
	    //about your concrete implementation.
	    public Iterator<String> iterator(){
	        //no point implementing a whole class for something only used once
	        return new Iterator<String>() {
	            private int count=0;
	            //no need to have constructor which takes MyIter, (non-static) inner class has access to instance members
	            public boolean hasNext(){
	                //simplify
	                return count < a.length;
	            }
	            public String next(){
	                return a[count++]; //getting clever
	            }

	            public void remove(){
	                throw new UnsupportedOperationException();
	            }
	        };
	    }
	}
	
	public static class MyLoader implements ISegCacheLoader<String>
	{
		public List<String> list = new ArrayList<>();
		
		public List<String> loadRange(long startIndex, long n)
		{
			System.out.println(String.format("LD %d.%d", startIndex,n));
			List<String> resultL = new ArrayList<>();
			
			for(long i = 0; i < list.size(); i++)
			{
				if (i >= startIndex && i < (startIndex + n))
				{
					resultL.add(list.get((int) i));
				}
			}
			
			return resultL;
		}
	}
	
	
	@Test
	public void test() 
	{
		String[] ar = new String[] { "ab", "cd", "ef" };
		
		MyIter iter = new MyIter(ar);
		
		for(String s : iter)
		{
			log(s);
		}
		
	}
	
	@Test
	public void testSeg() 
	{
		MyLoader loader = new MyLoader();
		SegmentedCache<String> cache = new SegmentedCache<String>();
		cache.init(4, loader);
		String[] ar = new String[] { "0", "1", "2", "3"};
		cache.putList(0, Arrays.asList(ar));
		
		String s = cache.getOne(0);
		chkCache(cache, "0", 0);
		chkCache(cache, "1", 1);
		chkCache(cache, "2", 2);
		chkCache(cache, "3", 3);
		chkCache(cache, null, 4);

		String[] ar2 = new String[] { "4", "5", "6", "7"};
		cache.putList(4, Arrays.asList(ar2));
		
		for(long i = 4; i < 8; i++)
		{
			Long n = i;
			chkCache(cache, n.toString(), i);
		}
		chkCache(cache, null, 8);
		
		String[] ar3 = new String[] { "8"};
		cache.putList(8, Arrays.asList(ar3));
		
		for(long i = 8; i < 9; i++)
		{
			Long n = i;
			chkCache(cache, n.toString(), i);
		}
		chkCache(cache, null, 9);
		
		List<String> tmpL = cache.getRange(6, 5);
		for(String ss : tmpL)
		{
			log(ss);
		}
	}
	
	@Test
	public void testSeg2() 
	{
		MyLoader loader = new MyLoader();
		SegmentedCache<String> cache = new SegmentedCache<String>();
		cache.init(4, loader);
		String[] ar = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
		loader.list = Arrays.asList(ar);
		
		for(long i = 0; i < 10; i++)
		{
			Long n = i;
			chkCache(cache, n.toString(), i);
		}
		chkCache(cache, null, 10);
		
		List<String> tmpL = cache.getRange(6, 5);
		for(String ss : tmpL)
		{
			log(ss);
		}
	}
	
	//--helpers--
	private void chkCache(SegmentedCache<String> cache, String expected, long index)
	{
		assertEquals(expected, cache.getOne(index));
		
	}

	@Before
	public void init()
	{
		super.init();
	}
}
