package mesf.entity;



public interface IEntityMgr
{
	String getTypeName();
	String renderEntity(BaseEntity obj) throws Exception ;
	String renderPartial(BaseEntity obj) throws Exception; 
	BaseEntity rehydrate(String json) throws Exception;
	void mergeHydrate(BaseEntity obj, String json) throws Exception;
}