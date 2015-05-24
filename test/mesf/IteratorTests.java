package mesf;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;

public class IteratorTests extends BaseTest
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
	
	public static class SegmentedCache<T>
	{
		private Map<Integer, List<T>> map = new HashMap<>();
		private int segSize;
		
		public SegmentedCache(int segSize)
		{
			this.segSize = segSize;
		}
		
		public void putList(int startIndex, List<T> L)
		{
			map.put(new Integer(startIndex), L);
		}
		
		public T getOne(int index)
		{
			int seg = (index / segSize);
			
			List<T> L = map.get(seg);
			if (L != null)
			{
				int k = index % segSize;
				return L.get(k);
			}
			return null;
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
		SegmentedCache<String> cache = new SegmentedCache<String>(4);
		String[] ar = new String[] { "ab", "cd", "ef", "gh"};
		cache.putList(0, Arrays.asList(ar));
		
		String s = cache.getOne(0);
		assertEquals("ab", cache.getOne(0));
		assertEquals("cd", cache.getOne(1));
		assertEquals("ef", cache.getOne(2));
		assertEquals("gh", cache.getOne(3));
		assertEquals(null, cache.getOne(4));
		
	}
	

	@Before
	public void init()
	{
		super.init();
	}
}
