package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.cache.EventCache;
import mesf.event.Event;
import mesf.event.BaseEventRehydrator;
import mesf.log.Logger;
import mesf.persistence.EventRecord;
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
		if (startId >= mtx.getEventMaxId())
		{
			return; //nothing to do
		}
		cache.clearLastSegment(mtx.getEventMaxId());
		List<IEventObserver> obsL = new ArrayList<>();
		obsL.add(observer);
		run(mtx, obsL, startId);
	}
	public void run(MContext mtx, List<IEventObserver> observerL, long startId)
	{
		long startIndex = startId;
//		if (startIndex > 0)
//		{
//			startIndex--; //yuck!!
//		}
		List<EventRecord> L = cache.loadRange(startIndex, mtx.getEventMaxId() - startIndex);
		for(EventRecord event : L)	
		{
			BaseEventRehydrator hydrator = new BaseEventRehydrator(mtx);
			Event ev = hydrator.rehyrdateIfType(event, event.getEventName());
			if (ev == null)
			{
				Logger.log("oops null");
			}
			else
			{
				doObserve(ev, observerL);
			}
		}
		
		for(IEventObserver observer : observerL)
		{
			if (observer instanceof IReadModel)
			{
				IReadModel rm = (IReadModel) observer;
				rm.setLastEventId(mtx.getEventMaxId());
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