package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;
import mesf.persistence.PersistenceContext;
import mesf.readmodel.ReadModel;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public class Permanent
{
	protected ObjectManagerRegistry registry;
	protected ObjectRepository objectRepo;
	protected ReadModelRepository readmodelRepo;
	protected StreamCache strcache;
	private CommitCache commitCache;
	private ProcRegistry procRegistry;
	private PersistenceContext persistenceCtx;
	
	/*
	 * tbls: commit, stream
	 * cache: CommitCache, StreamCache
	 * repositories: Object, Aggregate, ReadModel
	 * proc
	 */

	public Permanent(PersistenceContext persistenceCtx, ObjectManagerRegistry registry, ProcRegistry procRegistry)
	{
		this.persistenceCtx = persistenceCtx;
		this.registry = registry;
		this.strcache = new StreamCache(persistenceCtx.getStreamDAO());
		ObjectRepository objcache = new ObjectRepository(persistenceCtx.getStreamDAO(), registry);	
		this.objectRepo = objcache;
		this.readmodelRepo = new ReadModelRepository(strcache);
		commitCache = new CommitCache(persistenceCtx.getDao());
		this.procRegistry = procRegistry;
	}
	
	public void start()
	{
		Projector projector = new Projector(commitCache, strcache);
		
		List<ICommitObserver> obsL = new ArrayList<>();
		obsL.add(objectRepo);
		obsL.add(readmodelRepo);
				
		Long maxId = persistenceCtx.getDao().findMaxId();
		MContext mtx = createMContext();
		projector.run(mtx, obsL, maxId);
	}
	

	public MContext createMContext() 
	{
		CommitMgr mgr = new CommitMgr(persistenceCtx.getDao(), persistenceCtx.getStreamDAO(), commitCache, this.strcache);
		mgr.getMaxId(); //query db
		ReadModelLoader vloader = new ReadModelLoader(persistenceCtx.getDao(), persistenceCtx.getStreamDAO(), mgr.getMaxId());
		
		MContext mtx = new MContext(mgr, registry, this.objectRepo, this.readmodelRepo, vloader, this.commitCache, this.strcache);
		mtx.setProcRegistry(procRegistry);
		return mtx;
	}
	
	public BaseObject loadObjectFromRepo(long objectId) 
	{
		return objectRepo.getIfLoaded(objectId);
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