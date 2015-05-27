package mesf.core;


//used by commands
public class ObjectHydrater
{
	private ObjectCache objcache;
	
	public ObjectHydrater(ObjectCache objcache)
	{
		this.objcache = objcache;
	}
	
	public BaseObject loadObject(String type, Long objectId, ObjectLoader oloader) throws Exception
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