package mesf.cmd;

import mesf.core.BaseObject;
import mesf.core.CommitMgr;
import mesf.core.IObjectMgr;
import mesf.core.MContext;
import mesf.core.ObjectHydrater;
import mesf.core.ObjectManagerRegistry;
import mesf.core.ObjectRepository;
import mesf.core.ObjectLoader;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public abstract class CommandProcessor
{
	protected MContext mtx;

	public CommandProcessor()
	{
	}
	
	public void setMContext(MContext mtx)
	{
		this.mtx = mtx;
	}
	
	public abstract void process(ICommand cmd);
	
	public void insertObject(ObjectCommand cmd, BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = mtx.getRegistry().findByType(type);
		
		//!break rules here and we modify command. Since controller needs to know id of newly created object
		cmd.objectId = mtx.getCommitMgr().insertObject(mgr, obj);
	}
	public void updateObject(BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().updateObject(mgr, obj);
	}
	public void deleteObject(BaseObject obj)
	{
		String type = this.getObjectType(obj);
		IObjectMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().deleteObject(mgr, obj);
	}
	public String getObjectType(BaseObject obj)
	{
		String type = mtx.getRegistry().findTypeForClass(obj.getClass());
		return type;
	}
}