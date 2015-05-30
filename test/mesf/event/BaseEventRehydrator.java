package mesf.event;

import mesf.core.MContext;
import mesf.persistence.EventRecord;

public class BaseEventRehydrator
{
	public MContext mtx;
	
	public BaseEventRehydrator(MContext mtx) 
	{
		this.mtx = mtx;
	}

	public Event rehyrdateIfType(EventRecord event, String eventName) 
	{
		//note we receive raw event db objects. for speed.
		//only hydrate into BaseEvent objects as needed
		
		if (event.getEventName().equals(eventName))
		{
			IEventMgr mm = mtx.getEventRegistry().findByType(event.getEventName());
			try {
				Event eee = mm.rehydrate(event.getJson());
				return eee;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}