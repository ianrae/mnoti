package mesf.core;

import mesf.readmodel.ReadModelLoader;
import mesf.readmodel.ReadModelRepository;

public class MContext 
{
	protected CommitMgr commitMgr;
	protected ObjectRepository objcache;
	protected ObjectHydrater hydrater;
	protected ObjectManagerRegistry registry;
	protected ObjectLoader oloader;

	private ReadModelRepository readmodelMgr;
	private ReadModelLoader vloader;

	public MContext(CommitMgr commitMgr, ObjectManagerRegistry registry, ObjectRepository objcache, ReadModelRepository readmodelMgr, ReadModelLoader vloader)
	{
		this.commitMgr = commitMgr;
		this.registry = registry;
		this.objcache = objcache;
		this.hydrater = new ObjectHydrater(objcache);
		this.oloader = commitMgr.createObjectLoader();
		this.readmodelMgr = readmodelMgr;
		this.vloader = vloader;
	}

	public CommitMgr getCommitMgr() {
		return commitMgr;
	}

	public ObjectRepository getObjcache() {
		return objcache;
	}

	public ObjectHydrater getHydrater() {
		return hydrater;
	}

	public ObjectManagerRegistry getRegistry() {
		return registry;
	}

	public ObjectLoader getOloader() {
		return oloader;
	}

	public ReadModelRepository getReadmodelMgr() {
		return readmodelMgr;
	}

	public ReadModelLoader getVloader() {
		return vloader;
	}
	
}
