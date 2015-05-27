package mesf.cmd;

import java.util.HashMap;
import java.util.Map;

import mesf.core.MContext;

public class ProcRegistry
{
	private Map<Class, Class<? extends CommandProcessor>> map = new HashMap<>();
	
	public ProcRegistry()
	{}
	
	public void register(Class clazz, Class<? extends CommandProcessor> procClazz)
	{
		map.put(clazz, procClazz);
	}
	
	public CommandProcessor find(Class clazz, MContext mtx)
	{
		Class<? extends CommandProcessor> procClazz = map.get(clazz);
		CommandProcessor proc = null;
		try {
			proc = procClazz.newInstance();
			proc.setMContext(mtx);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return proc;
	}
	
}