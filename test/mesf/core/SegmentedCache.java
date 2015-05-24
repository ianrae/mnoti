package mesf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SegmentedCache<T>
{
	private Map<Long, List<T>> map = new HashMap<>();
	private long segSize;
	private ISegCacheLoader<T> loader;
	
	public SegmentedCache(long segSize, ISegCacheLoader<T> loader)
	{
		this.segSize = segSize;
		this.loader = loader;
	}
	
	public void putList(long startIndex, List<T> L)
	{
		map.put(new Long(startIndex), L);
	}
	
	public T getOne(long index)
	{
		long seg = (index / segSize) * segSize;
		
		List<T> L = map.get(seg);
		
		if (L == null)
		{
			L = loader.loadRange(seg, segSize);
			if (L != null)
			{
				map.put(new Long(seg), L);
			}
		}
		
		
		if (L != null)
		{
			long k = index % segSize;
			if (k >= L.size())
			{
				return null;
			}
			return L.get((int) k);
		}
		return null;
	}
	
	public List<T> getRange(long startIndex, long n)
	{
		List<T> resultL = new ArrayList<>();
		
		for(long i = startIndex; i < (startIndex + n); i++)
		{
			T val = getOne(i);
			if (val == null)
			{
				return resultL;
			}
			resultL.add(val);
		}
		return resultL;
	}
}