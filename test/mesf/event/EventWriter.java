package mesf.event;

import mesf.core.MContext;
import mesf.log.Logger;
import mesf.persistence.EventRecord;

public class EventWriter 
{
	private MContext mtx;
	public EventWriter(MContext mtx)
	{
		this.mtx = mtx;
	}
	public void insertEvent(Event event)
	{
		String type = this.getEventType(event);
		IEventMgr mgr = mtx.getEventRegistry().findByType(type);
		
		EventRecord record = new EventRecord();
		record.setStreamId(event.getEntityId());
		
		record.setEventName(mgr.getTypeName());
		String json = null;
		try {
			json = mgr.renderEntity(event);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		record.setJson(json);
		this.mtx.getEventDAO().save(record);
		Logger.logDebug("EV [%d] %d %s", record.getId(), event.getEntityId(), record.getEventName());
		
		//if there is an eventbus, send event to it
		IEventBus bus = mtx.getEventBus();
		if (bus != null)
		{
			bus.eventOccurred(event);
		}
		
	}
	
	
	public String getEventType(Event obj)
	{
		String type = mtx.getEventRegistry().findTypeForClass(obj.getClass());
		return type;
	}
}