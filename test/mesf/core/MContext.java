package mesf.core;

import mesf.cache.CommitCache;
import mesf.cache.EventCache;
import mesf.cache.StreamCache;
import mesf.cmd.CommandProcessor;
import mesf.cmd.ProcRegistry;
import mesf.entity.Entity;
import mesf.entity.EntityHydrater;
import mesf.entity.EntityLoader;
import mesf.entity.EntityManagerRegistry;
import mesf.entity.EntityRepository;
import mesf.event.EventManagerRegistry;
import mesf.event.IEventBus;
import mesf.persistence.IEventRecordDAO;
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
	private EventManagerRegistry evReg;
	private EventCache eventCache;
	private long maxEventId;
	private IEventBus eventBus;
	
	public MContext(CommitMgr commitMgr, EntityManagerRegistry registry, EventManagerRegistry evReg, EntityRepository objcache, 
			ReadModelRepository readmodelMgr, ReadModelLoader vloader, CommitCache commitCache, StreamCache strcache, EventCache eventCache, 
			PersistenceContext persistenceCtx, IEventBus eventBus)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.evReg = evReg;
		this.objcache = objcache;
		this.hydrater = new EntityHydrater(objcache);
		this.oloader = commitMgr.createEntityLoader();
		this.readmodelMgr = readmodelMgr;
		this.vloader = vloader;
		this.commitCache = commitCache;
		this.strcache = strcache;
		this.eventCache = eventCache;
		this.persistenceCtx = persistenceCtx;
		this.eventBus = eventBus;
	}
	
	public IEventRecordDAO getEventDAO()
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
	public EventManagerRegistry getEventRegistry() {
		return evReg;
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
	
	public Entity loadEntitySafe(Class clazz, long entityId) 
	{
		Entity entity = null;
		try {
			entity = this.loadEntity(clazz, entityId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
	}
	public Entity loadEntity(Class clazz, long entityId) throws Exception 
	{
		String type = this.getRegistry().findTypeForClass(clazz);
		Entity obj = this.getHydrater().loadEntity(type, entityId, this.getOloader());
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

	public EventProjector createEventProjector() 
	{
		return new EventProjector(eventCache);
	}

	public long getEventMaxId() 
	{
		if (maxEventId != 0L)
		{
			return maxEventId;
		}
		
		IEventRecordDAO evdao = this.persistenceCtx.getEventDAO();
		maxEventId = evdao.findMaxId();
		return maxEventId;
	}

	public IEventBus getEventBus() {
		return eventBus;
	}
	
}
