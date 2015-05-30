package mesf.event;

public interface IEventMgr
{
	String getTypeName();
	String renderEntity(Event obj) throws Exception ;
	Event rehydrate(String json) throws Exception;
}