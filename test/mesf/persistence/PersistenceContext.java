package mesf.persistence;

public class PersistenceContext 
{
	private final IStreamDAO streamDAO;
	private final ICommitDAO dao;
	private final IEventRecordDAO eventDAO;

	public PersistenceContext(ICommitDAO dao, IStreamDAO streamDAO, IEventRecordDAO eventDAO)
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

	public IEventRecordDAO getEventDAO() {
		return eventDAO;
	}
}
