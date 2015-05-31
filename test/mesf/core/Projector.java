package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.cache.CommitCache;
import mesf.cache.StreamCache;
import mesf.persistence.Commit;
import mesf.persistence.Stream;
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
		if (startId >= mtx.getMaxId())
		{
			return; //nothing to do
		}
		cache.clearLastSegment(mtx.getMaxId());
		scache.clearLastSegment(mtx.getMaxId());
		List<ICommitObserver> obsL = new ArrayList<>();
		obsL.add(observer);
		run(mtx, obsL, startId);
	}
	public void run(MContext mtx, List<ICommitObserver> observerL, long startId)
	{
		long startIndex = startId;
		if (startIndex > 0)
		{
			startIndex--; //yuck!!
		}
		List<Commit> L = cache.loadRange(startIndex, mtx.getMaxId() - startIndex);
		for(Commit commit : L)	
		{
			doObserve(mtx, commit, observerL);
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
	
	private void doObserve(MContext mtx, Commit commit, List<ICommitObserver> observerL)
	{
		Long streamId = commit.getStreamId();
		Stream stream = null;
		if (streamId != null && streamId != 0L)
		{
			stream = scache.findStream(streamId);
		}

		for(ICommitObserver observer : observerL)
		{
			if (observer.willAccept(stream, commit))
			{
				observer.observe(mtx, stream, commit);
			}
		}
	}

}