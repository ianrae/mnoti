package mesf.core;

import java.util.ArrayList;
import java.util.List;

import org.mef.framework.entitydb.EntityDB;
import org.mef.framework.fluent.QStep;
import org.mef.framework.fluent.Query1;
import org.mef.framework.fluent.QueryContext;
import org.mef.framework.sfx.SfxContext;

public class MockCommitDAO implements ICommitDAO
	{
		protected List<Commit> _L = new ArrayList<Commit>();
		protected EntityDB<Commit> _entityDB = new EntityDB<Commit>();
		public QueryContext<Commit> queryctx; 

		@Override
		public void init(SfxContext ctx)
		{
			this.queryctx = new QueryContext<Commit>(ctx, Commit.class);

//			ProcRegistry registry = (ProcRegistry) ctx.getServiceLocator().getInstance(ProcRegistry.class);
//			EntityDBQueryProcessor<ObjectCommit> proc = new EntityDBQueryProcessor<ObjectCommit>(ctx, _L);
//			registry.registerDao(ObjectCommit.class, proc);
		}

		@Override
		public Query1<Commit> query() 
		{
			queryctx.queryL = new ArrayList<QStep>();
			return new Query1<Commit>(queryctx);
		}


		@Override
		public int size() 
		{
			return _L.size();
		}

		@Override
		public Commit findById(long id) 
		{
			Commit entity = this.findActualById(id);
			if (entity != null)
			{
				return entity; //!!new ObjectCommit(entity); //return copy
			}
			return null; //not found
		}

		protected Commit findActualById(long id) 
		{
			for(Commit entity : _L)
			{
				if (entity.getId() == id)
				{
					return entity;
				}
			}
			return null; //not found
		}

		@Override
		public List<Commit> all() 
		{
			return _L; //ret copy??!!
		}

		@Override
		public void delete(long id) 
		{
			Commit entity = this.findActualById(id);
			if (entity != null)
			{
				_L.remove(entity);
			}
		}

		@Override
		public void save(Commit entity) 
		{
			if (entity.getId() == null)
			{
				entity.setId(new Long(0L));
			}

			if (findActualById(entity.getId()) != null)
			{
				throw new RuntimeException(String.format("save: id %d already exists", entity.getId()));
			}


			if (entity.getId() == 0)
			{
				entity.setId(nextAvailIdNumber());
			}
			else
			{
				delete(entity.getId()); //remove existing
			}

			_L.add(entity);
		}

		private Long nextAvailIdNumber() 
		{
			long used = 0;
			for(Commit entity : _L)
			{
				if (entity.getId() > used)
				{
					used = entity.getId();
				}
			}
			return used + 1;
		}

		@Override
		public void update(Commit entity) 
		{
			this.delete(entity.getId());
			this.save(entity);
		}

	}