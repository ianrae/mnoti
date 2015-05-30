package mesf.persistence;

import java.util.List;


public interface IEventRecordDAO  extends IDAO
{
	EventRecord findById(long id);
	List<EventRecord> all();
	List<EventRecord> loadRange(long startId, long n);
	List<EventRecord> loadStream(long startId, long streamId);
	void save(EventRecord entity);        
	void update(EventRecord entity);
	public Long findMaxId();
}