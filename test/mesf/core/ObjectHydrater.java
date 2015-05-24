package mesf.core;


//used by commands
public class ObjectHydrater
{
	private ObjectViewCache objcache;
	
	public ObjectHydrater(ObjectViewCache objcache)
	{
		this.objcache = objcache;
	}
	
	public BaseObject loadObject(String type, Long objectId, CommitMgr commitMgr) throws Exception
	{
		//objcache should be immutable objects, so for our commands make a copy
		BaseObject obj = objcache.loadObject(type, objectId, commitMgr);
		if (obj != null)
		{
			BaseObject clone = obj.clone();
			return clone;
		}
		return null;
	}
}