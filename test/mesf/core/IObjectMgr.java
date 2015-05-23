package mesf.core;


public interface IObjectMgr
{
	String getTypeName();
	String renderObject(BaseObject obj) throws Exception ;
	String renderPartialObject(BaseObject obj) throws Exception; 
	BaseObject rehydrate(String json) throws Exception;
	void mergeHydrate(BaseObject obj, String json) throws Exception;
}