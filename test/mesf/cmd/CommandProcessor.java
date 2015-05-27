package mesf.cmd;

import mesf.core.BaseObject;
import mesf.core.CommitMgr;
import mesf.core.IObjectMgr;
import mesf.core.ObjectHydrater;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectRepository;
import mesf.core.ObjectLoader;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public abstract class CommandProcessor
{
	protected CommitMgr commitMgr;
	protected ObjectRepository objcache;
	protected ObjectHydrater hydrater;
	protected ObjectManagerRegistry registry;
	protected ObjectLoader oloader;
	private ReadModelRepository readmodelMgr;
	private ReadModelLoader vloader;

	public CommandProcessor(CommitMgr commitMgr, ObjectManagerRegistry registry, ObjectRepository objcache, ReadModelRepository readmodelMgr, ReadModelLoader vloader)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.objcache = objcache;
		this.hydrater = new ObjectHydrater(objcache);
		this.oloader = commitMgr.createObjectLoader();
		this.readmodelMgr = readmodelMgr;
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