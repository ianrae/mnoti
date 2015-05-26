package mesf.core;

import java.util.List;


public class ViewLoader
{
	private ICommitDAO dao;
	private IStreamDAO streamDAO;
	private long maxId; //per current epoch

	public ViewLoader(ICommitDAO dao, IStreamDAO streamDAO, long maxId)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
		this.maxId = maxId;
	}
	
	public List<Commit> loadCommits(long startId)
	{
		long n = maxId - startId;
		List<Commit> L = dao.loadRange(startId, n);
		return L;
	}

	public long getMaxId() 
	{
		return maxId;
	}
}