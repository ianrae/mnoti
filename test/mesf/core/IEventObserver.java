package mesf.core;

import mesf.persistence.Event;
import mesf.persistence.Stream;


public interface IEventObserver
{
	boolean willAccept(Event event);
	void observe(Event event);
}