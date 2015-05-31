package mesf.cache;

import java.util.List;

public interface ISegmentedCache<T> 
{
	void init(long segSize, ISegCacheLoader<T> loader);
	public void putList(long startIndex, List<T> L);
	public void clearLastSegment(long maxId);
	public T getOne(long index);
	public List<T> getRange(long startIndex, long n);
}
