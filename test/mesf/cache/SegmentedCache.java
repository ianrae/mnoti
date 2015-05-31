package mesf.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mesf.log.Logger;

public class SegmentedCache<T> implements ISegmentedCache<T>
{
	private Map<Long, List<T>> segmentMap = new HashMap<>();
	private long segSize;
	private ISegCacheLoader<T> loader;
	
	@Override
	public void init(long segSize, ISegCacheLoader<T> loader)
	{
		this.segSize = segSize;
		this.loader = loader;
	}
	
	@Override
	public void putList(long startIndex, List<T> L)
	{
		segmentMap.put(new Long(startIndex), L);
	}
	
	@Override
	public void clearLastSegment(long maxId)
	{
		long max = -1;
		for(Long seg : segmentMap.keySet())
		{
			if (seg > max)
			{
				max = seg;
			}
		}
		
		//max=4  5,6,7 so n=3
		//maxId=8 (we added one commit)
		
		if (max >= 0) //found last segment
		{
			long startIndex = max;
			int n = segmentMap.get(max).size();
			
			if (startIndex + n < maxId)
			{
				Logger.logDebug("LAST %d.%d", startIndex, n);
//				map.remove(max);
				
				//calculate # new commits we haven't yet loaded and load them
				long missing = maxId - (startIndex + n);
				
				if (missing + n <= this.segSize)
				{
					List<T> newL = loader.loadRange(startIndex + n, missing);
					List<T> L = segmentMap.get(max);
					L.addAll(newL);
				}
			}
		}
	}
	@Override
	public T getOne(long index)
	{
		long seg = (index / segSize) * segSize;
		
		List<T> L = segmentMap.get(seg);
		
		if (L == null)
		{
			L = loader.loadRange(seg, segSize);
			if (L != null)
			{
				segmentMap.put(new Long(seg), L);
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
	
	@Override
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