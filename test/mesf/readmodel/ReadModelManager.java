package mesf.readmodel;

import java.util.ArrayList;
import java.util.List;

import mesf.core.Commit;
import mesf.core.ICommitObserver;
import mesf.core.Stream;
import mesf.core.StreamCache;

public class ReadModelManager implements ICommitObserver
{
	protected List<ReadModel> viewObserversL = new ArrayList<>();
	private StreamCache strcache;

	public ReadModelManager(StreamCache strcache)
	{
		this.strcache = strcache;
	}
	
	public void registerReadModel(ReadModel view)
	{
		this.viewObserversL.add(view);
	}
	
	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		return true;
	}

	@Override
	public void observe(Stream stream, Commit commit) 
	{
		for(ReadModel view : this.viewObserversL)
		{
			if (view.willAccept(stream, commit))
			{
				view.observe(stream, commit);
			}
		}
	}
	
	public synchronized Object loadView(ReadModel view, ReadModelLoader vloader) throws Exception
	{
		List<Commit> L = vloader.loadCommits(view.lastCommitId + 1);
		
		for(Commit commit : L)
		{
			Long streamId = commit.getStreamId();
			Stream stream = null;
			if (streamId != null)
			{
				stream = strcache.findStream(streamId);
			}
			
			observe(stream, commit);
		}
		
		if (L.size() > 0)
		{
			Commit last = L.get(L.size() - 1);
			view.lastCommitId = last.getId();
		}
		return view.obj;
	}
}