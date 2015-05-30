package mesf.core;

import mesf.event.BaseEvent;
import mesf.persistence.Event;
import mesf.persistence.Stream;


public interface IEventObserver
{
	boolean willAcceptEvent(BaseEvent event);
	void observeEvent(BaseEvent event);
}