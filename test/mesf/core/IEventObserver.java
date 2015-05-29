package mesf.core;

import mesf.persistence.Event;
import mesf.persistence.Stream;


public interface IEventObserver
{
	boolean willAcceptEvent(Event event);
	void observeEvent(Event event);
}