package mesf.core;

import java.util.HashMap;
import java.util.Map;

public class EntityManagerRegistry
{
	Map<Class, IObjectMgr> map = new HashMap<>();
	
	public EntityManagerRegistry()
	{
		
	}
	public void register(Class clazz, IObjectMgr mgr)
	{
		map.put(clazz, mgr);
	}
	public IObjectMgr findByType(String type) 
	{
		for(Class clazz : map.keySet())
		{
			IObjectMgr mgr = map.get(clazz);
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
				IObjectMgr mgr = map.get(clazz);
				return mgr.getTypeName();
			}
		}
		return null;
	}
}