package mesf.readmodel;

import mesf.core.Commit;
import mesf.core.ICommitObserver;
import mesf.core.Stream;

public class ReadModel implements ICommitObserver
{
	long lastCommitId;
	public Object obj;
	
	public ReadModel()
	{
	}
	
	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		return false;
	}

	@Override
	public void observe(Stream stream, Commit commit) 
	{
	}
}