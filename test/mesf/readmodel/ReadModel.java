package mesf.readmodel;

import mesf.core.ICommitObserver;
import mesf.core.IEventObserver;
import mesf.persistence.EventRecord;
import mesf.core.MContext;
import mesf.event.Event;
import mesf.persistence.Commit;
import mesf.persistence.Stream;

public abstract class ReadModel implements ICommitObserver, IEventObserver, IReadModel
{
	public long lastCommitId;
	public long lastEventId;
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
	public void observe(MContext mtx, Stream stream, Commit commit) 
	{
	}

	@Override
	public void setLastCommitId(long id) 
	{
		this.lastCommitId = id;
	}
	
	@Override
	public void setLastEventId(long id) 
	{
		this.lastEventId = id;
	}
	
	@Override
	public boolean willAcceptEvent(Event event) 
	{
		return false;
	}

	@Override
	public void observeEvent(Event event) 
	{
	}
	
	
	@Override
	public abstract void freshen(MContext mtx);
}