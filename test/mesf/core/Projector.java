package mesf.core;

import java.util.ArrayList;
import java.util.List;

public class Projector
{
	private CommitCache cache;
	private StreamCache scache;

	public Projector(CommitCache cache, StreamCache scache)
	{
		this.cache = cache;
		this.scache = scache;
	}
	
	public void run(MContext mtx, ICommitObserver observer, long startId)
	{
		List<ICommitObserver> obsL = new ArrayList<>();
		obsL.add(observer);
		
		List<Commit> L = cache.loadRange(startId - 1, mtx.getMaxId());
		for(Commit commit : L)	
		{
			doObserve(commit, obsL);
		}
	}
	public void run(MContext mtx, List<ICommitObserver> observerL, long startId)
	{
		List<Commit> L = cache.loadRange(startId - 1, mtx.getMaxId());
		for(Commit commit : L)	
		{
			doObserve(commit, observerL);
		}
	}
	
	private void doObserve(Commit commit, List<ICommitObserver> observerL)
	{
		Long streamId = commit.getStreamId();
		Stream stream = null;
		if (streamId != null && streamId != 0L)
		{
			stream = scache.findStream(streamId);
		}

		for(ICommitObserver observer : observerL)
		{
			observer.observe(stream, commit);
		}
	}

}