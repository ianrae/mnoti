package mesf.errortracker;


public class ErrorTracker
{
	private static IErrorTracker theSingleton;
	
	public static void setErrorTracker(IErrorTracker errorTracker)
	{
		theSingleton = errorTracker;
	}
	public static IErrorTracker getErrorTracker()
	{
		initIfNeeded();
		return theSingleton;
	}
	private synchronized static void initIfNeeded() 
	{
		if (theSingleton == null)
		{
			theSingleton = new DefaultErrorTracker();
		}
	}
	
	public static void setListener(IErrorListener listener)
	{
		initIfNeeded();
		theSingleton.setListener(listener);
	}
	public static void errorOccurred(String errMsg)
	{
		initIfNeeded();
		theSingleton.errorOccurred(errMsg);
	}
	public static int getErrorCount()
	{
		initIfNeeded();
		return theSingleton.getErrorCount();
	}
	public static String getLastError()
	{
		initIfNeeded();
		return theSingleton.getLastError();
	}

}