package mesf.core;

import mesf.entity.Entity;
import mesf.entity.IEntityMgr;

public class CommitWriter 
{
	private MContext mtx;
	public CommitWriter(MContext mtx)
	{
		this.mtx = mtx;
	}
	public long insertEntity(Entity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		return mtx.getCommitMgr().insertEntity(mgr, obj);
	}
	public void updateEntity(Entity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().updateEntity(mgr, obj);
	}
	
	public String getEntityType(Entity obj)
	{
		String type = mtx.getRegistry().findTypeForClass(obj.getClass());
		return type;
	}
}