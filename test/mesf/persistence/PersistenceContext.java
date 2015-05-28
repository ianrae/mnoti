package mesf.persistence;

public class PersistenceContext 
{
	private final IStreamDAO streamDAO;
	private final ICommitDAO dao;

	public PersistenceContext(ICommitDAO dao, IStreamDAO streamDAO)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
	}

	public IStreamDAO getStreamDAO() {
		return streamDAO;
	}

	public ICommitDAO getDao() {
		return dao;
	}
}
