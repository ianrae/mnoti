package mesf.cmd;

import mesf.core.BaseObject;
import mesf.core.CommitMgr;
import mesf.core.IObjectMgr;
import mesf.core.ObjectHydrater;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectViewCache;
import mesf.core.StreamLoader;
import mesf.view.ViewLoader;
import mesf.view.ViewManager;

public abstract class CommandProcessor
{
	protected CommitMgr commitMgr;
	protected ObjectViewCache objcache;
	protected ObjectHydrater hydrater;
	protected ObjectManagerRegistry registry;
	protected StreamLoader sloader;
	private ViewManager viewMgr;
	private ViewLoader vloader;

	public CommandProcessor(CommitMgr commitMgr, ObjectManagerRegistry registry, ObjectViewCache objcache, ViewManager viewMgr, ViewLoader vloader)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.objcache = objcache;
		this.hydrater = new ObjectHydrater(objcache);
		this.sloader = commitMgr.createStreamLoader();
		this.viewMgr = viewMgr;
		this.vloader = vloader;
	}
	
	public abstract void process(ICommand cmd);
	
	protected void insertObject(ObjectCommand cmd, BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = registry.findByType(type);
		
		//!break rules here and we modify command. Since controller needs to know id of newly created object
		cmd.objectId = commitMgr.insertObject(mgr, obj);
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