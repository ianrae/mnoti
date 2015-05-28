package mesf.readmodel;

import java.util.List;

import mesf.persistence.Commit;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;


public class ReadModelLoader
{
	private ICommitDAO dao;
	private IStreamDAO streamDAO;
	private long maxId; //per current epoch

	public ReadModelLoader(ICommitDAO dao, IStreamDAO streamDAO, long maxId)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
		this.maxId = maxId;
	}
	
	public List<Commit> loadCommits(long startId)
	{
		long n = maxId - startId + 1;
		List<Commit> L = dao.loadRange(startId, n);
		return L;
	}

	public long getMaxId() 
	{
		return maxId;
	}
}