package mesf.errortracker;

public interface IErrorTracker
{
	void setListener(IErrorListener listener);
	public void errorOccurred(String errMsg);
	public int getErrorCount();
	public String getLastError();
}