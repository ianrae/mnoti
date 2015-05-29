package mesf.cmd;

public class BaseCommand implements ICommand
{
	public long objectId;

	@Override
	public long getObjectId() 
	{
		return objectId; //x
	}
}