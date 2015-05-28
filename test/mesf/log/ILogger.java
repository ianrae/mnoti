package mesf.log;

public interface ILogger
{
	void log(String s);
	void log(String fmt, Object... arguments);
	void logDebug(String s);
	void logDebug(String fmt, Object... arguments);
	void setLevel(LogLevel level);
}