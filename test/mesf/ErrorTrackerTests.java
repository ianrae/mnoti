package mesf;

import static org.junit.Assert.*;
import mesf.log.Logger;

import org.junit.Test;
import org.mef.framework.sfx.ISfxErrorListener;
import org.mef.framework.sfx.SfxBaseObj;
import org.mef.framework.sfx.SfxContext;

public class ErrorTrackerTests extends BaseMesfTest
{
	public interface IErrorListener
	{
		void onError(String errMsg);
	}
	
	public interface IErrorTracker
	{
		void setListener(IErrorListener listener);
		public void errorOccurred(String errMsg);
		public int getErrorCount();
		public String getLastError();
	}
	
	public class ErrorTracker implements IErrorTracker
	{
		private int errorCount;
		private String lastError;
		private IErrorListener listener;

		@Override
		public synchronized void setListener(IErrorListener listener) 
		{
			this.listener = listener;
		}

		@Override
		public synchronized void errorOccurred(String errMsg) 
		{
			lastError = errMsg;
			errorCount++;
			Logger.log("ERROR: " + errMsg);
			if (listener != null)
			{
				listener.onError(errMsg);
			}			
		}

		@Override
		public int getErrorCount() 
		{
			return errorCount;
		}

		@Override
		public synchronized String getLastError() 
		{
			return this.lastError;
		}
		
	}
	
	@Test
	public void test() 
	{
		ErrorTracker tracker = new ErrorTracker();
		assertEquals(0, tracker.getErrorCount());
		
		tracker.errorOccurred("oops");
		assertEquals(1, tracker.getErrorCount());
		tracker.errorOccurred("oops");
		assertEquals(2, tracker.getErrorCount());
	}

}
