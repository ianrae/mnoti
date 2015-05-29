package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.persistence.Event;
import mesf.persistence.Stream;
import mesf.readmodel.IReadModel;

public class EventProjector
{
	private EventCache cache;

	public EventProjector(EventCache cache)
	{
		this.cache = cache;
	}
	
	public void run(MContext mtx, IEventObserver observer, long startId)
	{
		if (startId >= mtx.getMaxId())
		{
			return; //nothing to do
		}
		cache.clearLastSegment(mtx.getMaxId());
		List<IEventObserver> obsL = new ArrayList<>();
		obsL.add(observer);
		run(mtx, obsL, startId);
	}
	public void run(MContext mtx, List<IEventObserver> observerL, long startId)
	{
		long startIndex = startId;
		if (startIndex > 0)
		{
			startIndex--; //yuck!!
		}
		List<Event> L = cache.loadRange(startIndex, mtx.getMaxId() - startIndex);
		for(Event commit : L)	
		{
			doObserve(commit, observerL);
		}
		
		for(IEventObserver observer : observerL)
		{
			if (observer instanceof IReadModel)
			{
				IReadModel rm = (IReadModel) observer;
				rm.setLastEventId(mtx.getMaxId());
			}
		}
	}
	
	private void doObserve(Event event, List<IEventObserver> observerL)
	{
		for(IEventObserver observer : observerL)
		{
			observer.observeEvent(event);
		}
	}

}