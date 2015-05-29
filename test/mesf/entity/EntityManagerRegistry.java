package mesf.entity;

import java.util.HashMap;
import java.util.Map;

public class EntityManagerRegistry
{
	Map<Class, IEntityMgr> map = new HashMap<>();
	
	public EntityManagerRegistry()
	{
		
	}
	public void register(Class clazz, IEntityMgr mgr)
	{
		map.put(clazz, mgr);
	}
	public IEntityMgr findByType(String type) 
	{
		for(Class clazz : map.keySet())
		{
			IEntityMgr mgr = map.get(clazz);
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
				IEntityMgr mgr = map.get(clazz);
				return mgr.getTypeName();
			}
		}
		return null;
	}
}