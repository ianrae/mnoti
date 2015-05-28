package mesf.log;

public class Logger
{
	private static ILogger theSingleton;
	
	public static void setLogger(ILogger log)
	{
		theSingleton = log;
	}
	public static ILogger getLogger()
	{
		initIfNeeded();
		return theSingleton;
	}
	private synchronized static void initIfNeeded() 
	{
		if (theSingleton == null)
		{
			theSingleton = new DefaultLogger();
		}
	}
	
	public static void log(String s)
	{
		initIfNeeded();
		theSingleton.log(s);
	}
	public static void log(String fmt, Object... arguments) 
	{
		initIfNeeded();
		theSingleton.log(fmt, arguments);
	}
	public static void logDebug(String s)
	{
		initIfNeeded();
		theSingleton.logDebug(s);
	}
	public static void logDebug(String fmt, Object... arguments) 
	{
		initIfNeeded();
		theSingleton.logDebug(fmt, arguments);
	}
}