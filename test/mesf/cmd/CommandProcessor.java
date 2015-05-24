package mesf.cmd;

import mesf.core.BaseObject;
import mesf.core.CommitMgr;
import mesf.core.IObjectMgr;
import mesf.core.ObjectHydrater;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectViewCache;

public abstract class CommandProcessor
{
	protected CommitMgr commitMgr;
	protected ObjectViewCache objcache;
	protected ObjectHydrater hydrater;
	protected ObjectManagerRegistry registry;

	public CommandProcessor(CommitMgr commitMgr, ObjectManagerRegistry registry, ObjectViewCache objcache)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.objcache = objcache;
		this.hydrater = new ObjectHydrater(objcache);
	}
	
	public abstract void process(ICommand cmd);
	
	protected void insertObject(BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = registry.findByType(type);
		
		commitMgr.insertObject(mgr, obj);
	}
	protected void updateObject(BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = registry.findByType(type);
		
		commitMgr.updateObject(mgr, obj);
	}
	protected void deleteObject(BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = registry.findByType(type);
		
		commitMgr.deleteObject(mgr, obj);
	}
	protected String getObjectType(BaseObject obj)
	{
		String type = registry.findTypeForClass(obj.getClass());
		return type;
	}
}