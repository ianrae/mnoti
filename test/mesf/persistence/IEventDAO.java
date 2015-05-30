package mesf.persistence;

import java.util.List;

import org.mef.framework.dao.IDAO;
import org.mef.framework.fluent.Query1;

public interface IEventDAO  extends IDAO
{
	EventRecord findById(long id);
	List<EventRecord> all();
	List<EventRecord> loadRange(long startId, long n);
	List<EventRecord> loadStream(long startId, long streamId);
	void save(EventRecord entity);        
	void update(EventRecord entity);
	public Query1<EventRecord> query();
	public Long findMaxId();
}