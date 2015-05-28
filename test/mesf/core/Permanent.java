package mesf.core;

import java.util.ArrayList;
import java.util.List;

import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.persistence.ICommitDAO;
import mesf.persistence.IStreamDAO;
import mesf.readmodel.ReadModel;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public class Permanent
{
	protected ICommitDAO dao;
	protected IStreamDAO streamDAO;
	protected ObjectManagerRegistry registry;
	protected ObjectRepository objectRepo;
	protected ReadModelRepository readmodelRepo;
	protected StreamCache strcache;
	private CommitCache commitCache;
	private ProcRegistry procRegistry;
	
	/*
	 * tbls: commit, stream
	 * cache: CommitCache, StreamCache
	 * repositories: Object, Aggregate, ReadModel
	 * proc
	 */

	public Permanent(ICommitDAO dao, IStreamDAO streamDAO, ObjectManagerRegistry registry, ProcRegistry procRegistry)
	{
		this.dao = dao;
		this.streamDAO = streamDAO;
		this.registry = registry;
		this.strcache = new StreamCache(streamDAO);
		ObjectRepository objcache = new ObjectRepository(streamDAO, registry);	
		this.objectRepo = objcache;
		this.readmodelRepo = new ReadModelRepository(strcache);
		commitCache = new CommitCache(dao);
		this.procRegistry = procRegistry;
	}
	
	public void start()
	{
		Projector projector = new Projector(commitCache, strcache);
		
		List<ICommitObserver> obsL = new ArrayList<>();
		obsL.add(objectRepo);
		obsL.add(readmodelRepo);
				
		Long maxId = dao.findMaxId();
		MContext mtx = createMContext();
		projector.run(mtx, obsL, maxId);
	}
	

	public MContext createMContext() 
	{
		CommitMgr mgr = new CommitMgr(dao, streamDAO, commitCache, this.strcache);
		mgr.getMaxId(); //query db
		ReadModelLoader vloader = new ReadModelLoader(dao, streamDAO, mgr.getMaxId());
		
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