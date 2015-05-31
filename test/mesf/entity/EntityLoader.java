package mesf.entity;

import java.util.List;

import mesf.cache.StreamCache;
import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;
import mesf.persistence.Stream;


public class EntityLoader
{
	private ICommitDAO dao;
	private StreamCache strcache;
	private long maxId; //per current epoch

	public EntityLoader(ICommitDAO dao, StreamCache strcache, long maxId)
	{
		this.dao = dao;
		this.strcache = strcache;
		this.maxId = maxId;
	}
	
	public List<Commit> loadStream(String type, Long entityId)
	{
		Stream stream = strcache.findStream(entityId);
		if (stream == null)
		{
			return null; //!!
		}
		
		List<Commit> L = dao.loadStream(stream.getSnapshotId(), entityId);
		return L;
	}

	public List<Commit> loadPartialStream(Long entityId, Long startId)
	{
		List<Commit> L = dao.loadStream(startId, entityId);
		return L;
	}

	public long getMaxId() 
	{
		return maxId;
	}
}