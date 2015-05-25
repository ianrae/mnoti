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
		
		List<Commit> rawL = dao.loadRange(stream.getSnapshotId(), maxId);
		List<Commit> L = new ArrayList<>();
		for(Commit commit : rawL)
		{
			if (commit.getStreamId().equals(objectId))
			{
				L.add(commit);
			}
		}
		return L;
	}
}