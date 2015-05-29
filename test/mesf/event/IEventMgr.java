package mesf.event;

public interface IEventMgr
{
	String getTypeName();
	String renderEntity(BaseEvent obj) throws Exception ;
	BaseEvent rehydrate(String json) throws Exception;
}