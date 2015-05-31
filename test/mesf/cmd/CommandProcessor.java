package mesf.cmd;

import mesf.core.CommitMgr;
import mesf.core.MContext;
import mesf.entity.Entity;
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
	
	public void insertEntity(BaseCommand cmd, Entity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		//!break rules here and we modify command. Since controller needs to know id of newly created entity
		cmd.entityId = mtx.getCommitMgr().insertEntity(mgr, obj);
	}
	public void updateEntity(Entity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().updateEntity(mgr, obj);
	}
	public void deleteEntity(Entity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().deleteEntity(mgr, obj);
	}
	public String getEntityType(Entity obj)
	{
		String type = mtx.getRegistry().findTypeForClass(obj.getClass());
		return type;
	}
}