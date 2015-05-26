package mesf.core;

import java.util.ArrayList;
import java.util.List;


public class StreamLoader
{
	private ICommitDAO dao;
	private IStreamDAO streamDAO;
	private long maxId; //per current epoch

	public StreamLoader(ICommitDAO dao, IStreamDAO streamDAO, long maxId)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
		this.maxId = maxId;
	}
	
	public List<Commit> loadStream(String type, Long objectId)
	{
		Stream stream = streamDAO.findById(objectId);
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