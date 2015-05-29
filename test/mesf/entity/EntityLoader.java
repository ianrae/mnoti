package mesf.entity;

import java.util.ArrayList;
import java.util.List;

import mesf.core.StreamCache;
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
	
	public List<Commit> loadStream(String type, Long objectId)
	{
		Stream stream = strcache.findStream(objectId);
		if (stream == null)
		{
			return null; //!!
		}
		
		List<Commit> L = dao.loadStream(stream.getSnapshotId(), objectId);
		return L;
	}

	public List<Commit> loadPartialStream(Long objectId, Long startId)
	{
		List<Commit> L = dao.loadStream(startId, objectId);
		return L;
	}

	public long getMaxId() 
	{
		return maxId;
	}
}