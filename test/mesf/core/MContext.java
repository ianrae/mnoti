package mesf.core;

import mesf.ObjManagerTests.Scooter;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.entity.BaseEntity;
import mesf.entity.EntityHydrater;
import mesf.entity.EntityLoader;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityRepository;
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

	public MContext(CommitMgr commitMgr, EntityManagerRegistry registry, EntityRepository objcache, 
			ReadModelRepository readmodelMgr, ReadModelLoader vloader, CommitCache commitCache, StreamCache strcache)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.objcache = objcache;
		this.hydrater = new EntityHydrater(objcache);
		this.oloader = commitMgr.createObjectLoader();
		this.readmodelMgr = readmodelMgr;
		this.vloader = vloader;
		this.commitCache = commitCache;
		this.strcache = strcache;
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
	
	public BaseEntity loadObject(Class clazz, long entityId) throws Exception 
	{
		String type = this.getRegistry().findTypeForClass(clazz);
		BaseEntity obj = this.getHydrater().loadObject(type, entityId, this.getOloader());
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
