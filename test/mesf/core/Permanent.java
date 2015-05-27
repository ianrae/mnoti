package mesf.core;

import java.util.List;

import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.readmodel.ReadModel;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public abstract class Permanent
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
		List<Commit> L = dao.all(); //!!use commitcache later
		for(Commit commit : L)	
		{
			doObserve(commit);
		}
	}
	
	private void doObserve(Commit commit)
	{
		Long streamId = commit.getStreamId();
		Stream stream = null;
		if (streamId != null && streamId != 0L)
		{
			stream = strcache.findStream(streamId);
		}

		objectRepo.observe(stream, commit);
		readmodelRepo.observe(stream, commit);
	}

	public MContext createMContext() 
	{
		CommitMgr mgr = new CommitMgr(dao, streamDAO, commitCache, this.strcache);
		mgr.getMaxId(); //query db
		ReadModelLoader vloader = new ReadModelLoader(dao, streamDAO, mgr.getMaxId());
		CommandProcessor proc = createProc(mgr, vloader);
		
		MContext mtx = new MContext(mgr, registry, this.objectRepo, this.readmodelRepo, vloader);
		mtx.setProcRegistry(procRegistry);
		return mtx;
	}
	
	abstract protected CommandProcessor createProc(CommitMgr mgr, ReadModelLoader vloader);
	

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