package mesf.log;

public class DefaultLogger implements ILogger
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