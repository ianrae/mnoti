package mesf;

import static org.junit.Assert.*;
import mef.framework.helpers.BaseTest;

import org.junit.Test;

public class LoggerTests extends BaseTest
{

	public enum LogLevel
	{
		OFF,
		NORMAL,
		DEBUG
	}
	
	public interface ILogger
	{
		void log(String s);
		void log(String fmt, Object... arguments);
		void logDebug(String s);
		void logDebug(String fmt, Object... arguments);
		void setLevel(LogLevel level);
	}
	
	public static class DefaultLogger implements ILogger
	{
		private LogLevel level = LogLevel.NORMAL;

		@Override
		public void log(String s) 
		{
			if (level == LogLevel.OFF)
			{
				return;
			}
			System.out.println(s);
		}

		@Override
		public void log(String fmt, Object... arguments) 
		{
			if (level == LogLevel.OFF)
			{
				return;
			}
			String msg = String.format(fmt, arguments);
			log(msg);
		}

		@Override
		public void logDebug(String s) 
		{
			if (level != LogLevel.DEBUG)
			{
				return;
			}
			System.out.println(s);
		}

		@Override
		public void logDebug(String fmt, Object... arguments) 
		{
			if (level != LogLevel.DEBUG)
			{
				return;
			}
			String msg = String.format(fmt, arguments);
			log(msg);
		}

		@Override
		public void setLevel(LogLevel level) 
		{
			this.level = level;
		}
	}
	
	public static class Logger
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
	
	@Test
	public void test() 
	{
		Logger.log("hey");
		int n = 45;
		Logger.log("n=%d",n);
		Logger.logDebug("hey debug");
		Logger.logDebug("n=%d debug",n);
		
		System.out.println("part2");
		Logger.getLogger().setLevel(LogLevel.DEBUG);
		Logger.log("hey");
		Logger.log("n=%d",n);
		Logger.logDebug("hey debug");
		Logger.logDebug("n=%d debug",n);
		
		System.out.println("part3");
		Logger.getLogger().setLevel(LogLevel.OFF);
		Logger.log("NOhey");
		Logger.log("NOn=%d",n);
		Logger.logDebug("NOhey debug");
		Logger.logDebug("NOn=%d debug",n);
	}

}
