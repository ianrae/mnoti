package mesf.config;

public interface IConfig {

	public enum ConfigItem
	{
		COMMIT_CACHE_SEGMENT_SIZE,
		EVENT_CACHE_SEGMENT_SIZE,
		STREAM_CACHE_SEGMENT_SIZE
	};
	
	
	int getIntValue(ConfigItem item);
}
