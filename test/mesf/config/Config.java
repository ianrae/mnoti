package mesf.config;

import mesf.config.IConfig.ConfigItem;


public class Config
{
	private static IConfig theSingleton;
	
	public static void setConfig(IConfig log)
	{
		theSingleton = log;
	}
	public static IConfig getConfig()
	{
		initIfNeeded();
		return theSingleton;
	}
	private synchronized static void initIfNeeded() 
	{
		if (theSingleton == null)
		{
			theSingleton = new DefaultConfig();
		}
	}
	
	
	public static int getIntValue(ConfigItem item) 
	{
		initIfNeeded();
		return theSingleton.getIntValue(item);
	}
}