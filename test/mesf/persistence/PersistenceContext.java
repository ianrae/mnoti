package mesf.persistence;

public class PersistenceContext 
{
	private final IStreamDAO streamDAO;
	private final ICommitDAO dao;
	private final IEventDAO eventDAO;

	public PersistenceContext(ICommitDAO dao, IStreamDAO streamDAO, IEventDAO eventDAO)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
		this.eventDAO = eventDAO;
	}

	public IStreamDAO getStreamDAO() {
		return streamDAO;
	}

	public ICommitDAO getDao() {
		return dao;
	}

	public IEventDAO getEventDAO() {
		return eventDAO;
	}
}
