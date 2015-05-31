package mesf.cache;

import java.util.List;

public interface ISegCacheLoader<T>
{
	List<T> loadRange(long startIndex, long n);
}