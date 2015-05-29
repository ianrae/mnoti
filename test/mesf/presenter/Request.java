package mesf.presenter;

import java.util.Map;

import mesf.cmd.ICommand;

import org.mef.framework.auth2.AuthUser;
import org.mef.twixt.binder.IFormBinder;

public class Request implements ICommand 
{
//	private Map<String, String> map;
//	private IFormBinder binder;
//	public AuthUser authUser; //null means not authenticated
	protected long objectId = 0L;
	
	public Request()
	{}

	@Override
	public long getEntityId() 
	{
		return objectId;
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
