package mesf.cmd;

import java.util.HashMap;
import java.util.Map;

public class ProcRegistry
{
	private Map<Class, CommandProcessor> map = new HashMap<>();
	
	public ProcRegistry()
	{}
	
	public void register(Class clazz, CommandProcessor proc)
	{
		map.put(clazz, proc);
	}
	
	public CommandProcessor find(Class clazz)
	{
		return map.get(clazz);
	}
	
}