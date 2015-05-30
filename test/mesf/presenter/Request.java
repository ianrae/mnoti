package mesf.presenter;

import java.util.Map;

import mesf.auth.AuthUser;
import mesf.cmd.ICommand;


public class Request implements ICommand 
{
//	private Map<String, String> map;
	@SuppressWarnings("rawtypes")
	protected IFormBinder binder;
	public AuthUser authUser; //null means not authenticated
	protected long entityId = 0L;
	
	public Request()
	{}

	@Override
	public long getEntityId() 
	{
		return entityId;
	}
	
	public IFormBinder getFormBinder()
	{
		return binder;
	}
	
//	public void setParameters(Map<String, String> map)
//	{
//		this.map = map;
//	}
//	public String getParameter(String name)
//	{
//		return this.map.get(name);
//	}
//	
//	public IFormBinder getFormBinder()
//	{
//		return binder;
//	}
//	public void setFormBinder(IFormBinder binder)
//	{
//		this.binder = binder;
//	}
}
