package mesf.core;

import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.entity.BaseEntity;
import mesf.entity.EntityHydrater;
import mesf.entity.EntityLoader;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityRepository;
import mesf.persistence.IEventDAO;
import mesf.persistence.PersistenceContext;
import mesf.readmodel.IReadModel;
import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public class MContext 
{
	protected CommitMgr commitMgr;
	protected EntityRepository objcache;
	protected EntityHydrater hydrater;
	protected EntityManagerRegistry registry;
	protected EntityLoader oloader;

	private ReadModelRepository readmodelMgr;
	private ReadModelLoader vloader;
	private ProcRegistry procRegistry;
	private CommitCache commitCache;
	private StreamCache strcache;
	private PersistenceContext persistenceCtx;

	public MContext(CommitMgr commitMgr, EntityManagerRegistry registry, EntityRepository objcache, 
			ReadModelRepository readmodelMgr, ReadModelLoader vloader, CommitCache commitCache, StreamCache strcache, PersistenceContext persistenceCtx)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.objcache = objcache;
		this.hydrater = new EntityHydrater(objcache);
		this.oloader = commitMgr.createEntityLoader();
		this.readmodelMgr = readmodelMgr;
		this.vloader = vloader;
		this.commitCache = commitCache;
		this.strcache = strcache;
		this.persistenceCtx = persistenceCtx;
	}
	
	public IEventDAO getEventDAO()
	{
		return this.persistenceCtx.getEventDAO();
	}
	
	//is optional
	public void setProcRegistry(ProcRegistry procRegistry)
	{
		this.procRegistry = procRegistry;
	}
	public ProcRegistry getProcRegistry()
	{
		return procRegistry;
	}

	public CommitMgr getCommitMgr() {
		return commitMgr;
	}

	public EntityRepository getObjcache() {
		return objcache;
	}

	public EntityHydrater getHydrater() {
		return hydrater;
	}

	public EntityManagerRegistry getRegistry() {
		return registry;
	}

	public EntityLoader getOloader() {
		return oloader;
	}

	public ReadModelRepository getReadmodelMgr() {
		return readmodelMgr;
	}

	public ReadModelLoader getVloader() {
		return vloader;
	}
	
	public BaseEntity loadEntity(Class clazz, long entityId) throws Exception 
	{
		String type = this.getRegistry().findTypeForClass(clazz);
		BaseEntity obj = this.getHydrater().loadEntity(type, entityId, this.getOloader());
		return obj;
	}
	
	public CommandProcessor findProc(Class clazz)
	{
		return getProcRegistry().find(clazz, this);
	}

	public long getMaxId() 
	{
		return commitMgr.getMaxId();
	}

	public Projector createProjector() 
	{
		return new Projector(commitCache, strcache);
	}
	
	public IReadModel acquire(Class clazz)
	{
		return readmodelMgr.acquire(this, clazz);
	}
	
}
