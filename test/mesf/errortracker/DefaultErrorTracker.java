package mesf.errortracker;

import mesf.log.Logger;

public class DefaultErrorTracker implements IErrorTracker
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