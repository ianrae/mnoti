package mesf.cmd;

import mesf.core.CommitMgr;
import mesf.core.MContext;
import mesf.entity.BaseEntity;
import mesf.entity.EntityHydrater;
import mesf.entity.EntityLoader;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityRepository;
import mesf.entity.IEntityMgr;
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
	
	public void insertObject(BaseCommand cmd, BaseEntity obj)
	{
		String type = this.getObjectType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		//!break rules here and we modify command. Since controller needs to know id of newly created object
		cmd.entityId = mtx.getCommitMgr().insertObject(mgr, obj);
	}
	public void updateObject(BaseEntity obj)
	{
		String type = this.getObjectType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().updateObject(mgr, obj);
	}
	public void deleteObject(BaseEntity obj)
	{
		String type = this.getObjectType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().deleteObject(mgr, obj);
	}
	public String getObjectType(BaseEntity obj)
	{
		String type = mtx.getRegistry().findTypeForClass(obj.getClass());
		return type;
	}
}