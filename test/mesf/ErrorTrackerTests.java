package mesf;

import static org.junit.Assert.*;
import mesf.errortracker.DefaultErrorTracker;

import org.junit.Test;
import org.mef.framework.sfx.ISfxErrorListener;
import org.mef.framework.sfx.SfxBaseObj;
import org.mef.framework.sfx.SfxContext;

public class ErrorTrackerTests extends BaseMesfTest
{
	@Test
	public void test() 
	{
		DefaultErrorTracker tracker = new DefaultErrorTracker();
		assertEquals(0, tracker.getErrorCount());
		
		tracker.errorOccurred("oops");
		assertEquals(1, tracker.getErrorCount());
		tracker.errorOccurred("oops");
		assertEquals(2, tracker.getErrorCount());
	}

}
