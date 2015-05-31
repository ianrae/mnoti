package mesf.entity;



//used by commands
public class EntityHydrater
{
	private EntityRepository objcache;
	
	public EntityHydrater(EntityRepository objcache)
	{
		this.objcache = objcache;
	}
	
	public Entity loadEntity(String type, Long entityId, EntityLoader oloader) throws Exception
	{
		//objcache should be immutable entities, so for our commands make a copy
		Entity obj = objcache.loadEntity(type, entityId, oloader);
		if (obj != null)
		{
			Entity clone = obj.clone();
			return clone;
		}
		return null;
	}
}