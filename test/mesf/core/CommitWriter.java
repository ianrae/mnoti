package mesf.core;

import mesf.entity.BaseEntity;
import mesf.entity.IEntityMgr;

public class CommitWriter 
{
	private MContext mtx;
	public CommitWriter(MContext mtx)
	{
		this.mtx = mtx;
	}
	public long insertEntity(BaseEntity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		return mtx.getCommitMgr().insertEntity(mgr, obj);
	}
	public void updateEntity(BaseEntity obj)
	{
		String type = this.getEntityType(obj);
		IEntityMgr mgr = mtx.getRegistry().findByType(type);
		
		mtx.getCommitMgr().updateEntity(mgr, obj);
	}
	
	public String getEntityType(BaseEntity obj)
	{
		String type = mtx.getRegistry().findTypeForClass(obj.getClass());
		return type;
	}
}