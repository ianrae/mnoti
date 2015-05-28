package mesf.readmodel;

import mesf.core.Commit;
import mesf.core.ICommitObserver;
import mesf.core.MContext;
import mesf.core.Stream;

public abstract class ReadModel implements ICommitObserver, IReadModel
{
	public long lastCommitId;
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

	@Override
	public void setLastCommitId(long id) 
	{
		this.lastCommitId = id;
	}
	
	@Override
	public abstract void freshen(MContext mtx);
}