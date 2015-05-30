package mesf.testhelper;

import mesf.persistence.ICommitDAO;
import mesf.persistence.IEventRecordDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.MockCommitDAO;
import mesf.persistence.MockEventRecordDAO;
import mesf.persistence.MockStreamDAO;
import mesf.persistence.PersistenceContext;

public class FactoryGirl 
{
	public static PersistenceContext createPersistenceContext()
	{
		ICommitDAO dao = new MockCommitDAO();
		IStreamDAO streamDAO = new MockStreamDAO();
		IEventRecordDAO eventDAO = new MockEventRecordDAO();
		
		PersistenceContext persistenceCtx = new PersistenceContext(dao, streamDAO, eventDAO);
		return persistenceCtx;
	}

}
