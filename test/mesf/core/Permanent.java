package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.entity.BaseEntity;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityRepository;
import mesf.event.EventManagerRegistry;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.PersistenceContext;
import mesf.readmodel.ReadModel;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public class Permanent
{
	protected EntityManagerRegistry registry;
	protected EntityRepository entityRepo;
	protected ReadModelRepository readmodelRepo;
	protected StreamCache strcache;
	private CommitCache commitCache;
	private ProcRegistry procRegistry;
	private PersistenceContext persistenceCtx;
	private EventCache eventCache;
	private EventManagerRegistry eventRegistry;
	
	/*
	 * tbls: commit, stream
	 * cache: CommitCache, StreamCache
	 * repositories: Entity, Aggregate, ReadModel
	 * proc
	 */

	public Permanent(PersistenceContext persistenceCtx, EntityManagerRegistry registry, ProcRegistry procRegistry, EventManagerRegistry evReg)
	{
		this.persistenceCtx = persistenceCtx;
		this.registry = registry;
		this.strcache = new StreamCache(persistenceCtx.getStreamDAO());
		EntityRepository objcache = new EntityRepository(persistenceCtx.getStreamDAO(), registry);	
		this.entityRepo = objcache;
		this.readmodelRepo = new ReadModelRepository(strcache);
		commitCache = new CommitCache(persistenceCtx.getDao());
		this.procRegistry = procRegistry;
		this.eventCache = new EventCache(persistenceCtx.getEventDAO());
		this.eventRegistry = evReg;
	}
	
	public void start()
	{
		Projector projector = new Projector(commitCache, strcache);
		
		List<ICommitObserver> obsL = new ArrayList<>();
		obsL.add(entityRepo);
		obsL.add(readmodelRepo);
				
		Long maxId = persistenceCtx.getDao().findMaxId();
		MContext mtx = createMContext();
		projector.run(mtx, obsL, maxId);
		
		projectEvents();
	}
	
	private void projectEvents()
	{
		EventProjector projector = new EventProjector(this.eventCache);
		
		List<IEventObserver> obsL = new ArrayList<>();
		//way to dadd!!
				
		Long maxId = persistenceCtx.getDao().findMaxId();
		MContext mtx = createMContext();
		projector.run(mtx, obsL, maxId);
	}

	public MContext createMContext() 
	{
		CommitMgr mgr = new CommitMgr(persistenceCtx, commitCache, this.strcache);
		mgr.getMaxId(); //query db
		ReadModelLoader vloader = new ReadModelLoader(persistenceCtx, mgr.getMaxId());
		
		MContext mtx = new MContext(mgr, registry, this.entityRepo, this.readmodelRepo, vloader, this.commitCache, this.strcache, persistenceCtx);
		mtx.setProcRegistry(procRegistry);
		return mtx;
	}
	
	public BaseEntity loadEntityFromRepo(long entityId) 
	{
		return entityRepo.getIfLoaded(entityId);
	}
	
	protected void registerReadModel(ReadModel readModel)
	{
		readmodelRepo.registerReadModel(readModel);
	}
	public ReadModelRepository getreadmodelMgr()
	{
		return readmodelRepo;
	}
}