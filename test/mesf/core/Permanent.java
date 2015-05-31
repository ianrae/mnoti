package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.cache.CommitCache;
import mesf.cache.EventCache;
import mesf.cache.StreamCache;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.entity.Entity;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityRepository;
import mesf.event.EventManagerRegistry;
import mesf.event.IEventBus;
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
	private IEventBus eventBus;
	
	/*
	 * tbls: commit, stream
	 * cache: CommitCache, StreamCache
	 * repositories: Entity, Aggregate, ReadModel
	 * proc
	 */
	public Permanent(PersistenceContext persistenceCtx)
	{
		this(persistenceCtx, null);
	}

	public Permanent(PersistenceContext persistenceCtx, IEventBus eventBus)
	{
		this.persistenceCtx = persistenceCtx;
		this.registry = new EntityManagerRegistry();
		this.procRegistry = new ProcRegistry();
		this.eventRegistry = new EventManagerRegistry();
		this.eventBus = eventBus;
		
		this.strcache = new StreamCache(persistenceCtx.getStreamDAO());
		EntityRepository objcache = new EntityRepository(persistenceCtx.getStreamDAO(), registry);	
		this.entityRepo = objcache;
		this.readmodelRepo = new ReadModelRepository(strcache);
		commitCache = new CommitCache(persistenceCtx.getDao());
		this.eventCache = new EventCache(persistenceCtx.getEventDAO());
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
		CommitMgr mgr = new CommitMgr(null, persistenceCtx, commitCache, this.strcache);
		mgr.getMaxId(); //query db
		ReadModelLoader vloader = new ReadModelLoader(persistenceCtx, mgr.getMaxId());
		
		MContext mtx = new MContext(mgr, registry, this.eventRegistry, this.entityRepo, this.readmodelRepo, vloader, 
				this.commitCache, this.strcache, this.eventCache, persistenceCtx, eventBus);
		mtx.setProcRegistry(procRegistry);
		
		mgr.setMtx(mtx); //!!yuck
		
		mtx.getEventMaxId(); //freshen event's maxid
		return mtx;
	}
	
	public Entity loadEntityFromRepo(long entityId) 
	{
		return entityRepo.getIfLoaded(entityId);
	}
	
	public void registerReadModel(ReadModel readModel)
	{
		readmodelRepo.registerReadModel(readModel);
	}
	public ReadModelRepository getreadmodelMgr()
	{
		return readmodelRepo;
	}

	public EntityManagerRegistry getEntityManagerRegistry() 
	{
		return this.registry;
	}

	public ProcRegistry getProcRegistry() 
	{
		return this.procRegistry;
	}

	public EventManagerRegistry getEventManagerRegistry() 
	{
		return this.eventRegistry;
	}
}