package mesf;

import static org.junit.Assert.*;
import mef.framework.helpers.BaseTest;
import mesf.log.LogLevel;
import mesf.log.Logger;

import org.junit.Test;

public class LoggerTests extends BaseTest
{

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
