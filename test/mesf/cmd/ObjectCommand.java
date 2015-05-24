package mesf.cmd;

public class ObjectCommand implements ICommand
{
	public long objectId;

	@Override
	public long getObjectId() 
	{
		return objectId;
	}
}