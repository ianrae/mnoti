package mesf.core;

import mesf.event.Event;
import mesf.persistence.EventRecord;
import mesf.persistence.Stream;


public interface IEventObserver
{
	boolean willAcceptEvent(Event event);
	void observeEvent(Event event);
}