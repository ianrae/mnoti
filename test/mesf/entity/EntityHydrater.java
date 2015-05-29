package mesf.entity;

import mesf.core.BaseEntity;


//used by commands
public class EntityHydrater
{
	private EntityRepository objcache;
	
	public EntityHydrater(EntityRepository objcache)
	{
		this.objcache = objcache;
	}
	
	public BaseEntity loadObject(String type, Long objectId, EntityLoader oloader) throws Exception
	{
		//objcache should be immutable objects, so for our commands make a copy
		BaseEntity obj = objcache.loadObject(type, objectId, oloader);
		if (obj != null)
		{
			BaseEntity clone = obj.clone();
			return clone;
		}
		return null;
	}
}