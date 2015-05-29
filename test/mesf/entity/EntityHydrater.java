package mesf.entity;

import mesf.core.BaseObject;


//used by commands
public class EntityHydrater
{
	private EntityRepository objcache;
	
	public EntityHydrater(EntityRepository objcache)
	{
		this.objcache = objcache;
	}
	
	public BaseObject loadObject(String type, Long objectId, EntityLoader oloader) throws Exception
	{
		//objcache should be immutable objects, so for our commands make a copy
		BaseObject obj = objcache.loadObject(type, objectId, oloader);
		if (obj != null)
		{
			BaseObject clone = obj.clone();
			return clone;
		}
		return null;
	}
}