package mesf.persistence;

import java.util.List;

import org.mef.framework.dao.IDAO;
import org.mef.framework.fluent.Query1;

public interface IEventDAO  extends IDAO
{
	Event findById(long id);
	List<Event> all();
	List<Event> loadRange(long startId, long n);
	List<Event> loadStream(long startId, long streamId);
	void save(Event entity);        
	void update(Event entity);
	public Query1<Event> query();
	public Long findMaxId();
}