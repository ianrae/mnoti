package mesf.config;

import java.util.HashMap;
import java.util.Map;

public class DefaultConfig implements IConfig
{
	private Map<String, Integer> intMap = new HashMap<>();
	
	public DefaultConfig()
	{
		intMap.put(ConfigItem.COMMIT_CACHE_SEGMENT_SIZE.name(), 4);
		intMap.put(ConfigItem.EVENT_CACHE_SEGMENT_SIZE.name(), 4);
		intMap.put(ConfigItem.STREAM_CACHE_SEGMENT_SIZE.name(), 4);
	}

	@Override
	public int getIntValue(ConfigItem item) 
	{
		String key = item.name();
		Integer n = intMap.get(key);
		if (n == null)
		{
			return Integer.MIN_VALUE;
		}
		return n;
	}

}
