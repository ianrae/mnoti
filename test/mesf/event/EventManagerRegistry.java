package mesf.event;

import java.util.HashMap;
import java.util.Map;

public class EventManagerRegistry
{
	Map<Class, IEventMgr> map = new HashMap<>();
	
	public EventManagerRegistry()
	{
		
	}
	public void register(Class clazz, IEventMgr mgr)
	{
		map.put(clazz, mgr);
	}
	public IEventMgr findByType(String type) 
	{
		for(Class clazz : map.keySet())
		{
			IEventMgr mgr = map.get(clazz);
			if (mgr.getTypeName().equals(type))
			{
				return mgr;
			}
		}
		return null;
	}
	//used when creating new obj
	public String findTypeForClass(Class targetClazz)
	{
		for(Class clazz : map.keySet())
		{
			if (clazz == targetClazz)
			{
				IEventMgr mgr = map.get(clazz);
				return mgr.getTypeName();
			}
		}
		return null;
	}
}