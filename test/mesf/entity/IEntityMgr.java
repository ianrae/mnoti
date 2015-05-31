package mesf.entity;



public interface IEntityMgr
{
	String getTypeName();
	String renderEntity(Entity obj) throws Exception ;
	String renderPartial(Entity obj) throws Exception; 
	Entity rehydrate(String json) throws Exception;
	void mergeHydrate(Entity obj, String json) throws Exception;
}