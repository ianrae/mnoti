package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.readmodel.IReadModel;

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
		cache.clearLastSegment(mtx.getMaxId());
		scache.clearLastSegment(mtx.getMaxId());
		List<ICommitObserver> obsL = new ArrayList<>();
		obsL.add(observer);
		run(mtx, obsL, startId);
	}
	public void run(MContext mtx, List<ICommitObserver> observerL, long startId)
	{
		if (startId > 0)
		{
			startId--; //yuck!!
		}
		List<Commit> L = cache.loadRange(startId, mtx.getMaxId() - startId);
		for(Commit commit : L)	
		{
			doObserve(commit, observerL);
		}
		
		for(ICommitObserver observer : observerL)
		{
			if (observer instanceof IReadModel)
			{
				IReadModel rm = (IReadModel) observer;
				rm.setLastCommitId(mtx.getMaxId());
			}
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