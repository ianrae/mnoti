package mesf.persistence;

import java.util.ArrayList;
import java.util.List;

import org.mef.framework.entitydb.EntityDB;
import org.mef.framework.fluent.QStep;
import org.mef.framework.fluent.Query1;
import org.mef.framework.fluent.QueryContext;
import org.mef.framework.sfx.SfxContext;

public class MockEventDAO implements IEventDAO
	{
		protected List<Event> _L = new ArrayList<Event>();
		protected EntityDB<Event> _entityDB = new EntityDB<Event>();
		public QueryContext<Event> queryctx; 

		@Override
		public void init(SfxContext ctx)
		{
			this.queryctx = new QueryContext<Event>(ctx, Event.class);

//			ProcRegistry registry = (ProcRegistry) ctx.getServiceLocator().getInstance(ProcRegistry.class);
//			EntityDBQueryProcessor<ObjectEvent> proc = new EntityDBQueryProcessor<ObjectEvent>(ctx, _L);
//			registry.registerDao(ObjectEvent.class, proc);
		}

		@Override
		public Query1<Event> query() 
		{
			queryctx.queryL = new ArrayList<QStep>();
			return new Query1<Event>(queryctx);
		}


		@Override
		public int size() 
		{
			return _L.size();
		}

		@Override
		public Event findById(long id) 
		{
			Event entity = this.findActualById(id);
			if (entity != null)
			{
				return entity; //!!new ObjectEvent(entity); //return copy
			}
			return null; //not found
		}

		protected Event findActualById(long id) 
		{
			for(Event entity : _L)
			{
				if (entity.getId() == id)
				{
					return entity;
				}
			}
			return null; //not found
		}

		@Override
		public List<Event> all() 
		{
			return _L; //ret copy??!!
		}

		@Override
		public void delete(long id) 
		{
			Event entity = this.findActualById(id);
			if (entity != null)
			{
				_L.remove(entity);
			}
		}

		@Override
		public void save(Event entity) 
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
			for(Event entity : _L)
			{
				if (entity.getId() > used)
				{
					used = entity.getId();
				}
			}
			return used + 1;
		}

		@Override
		public void update(Event entity) 
		{
			this.delete(entity.getId());
			this.save(entity);
		}

		@Override
		public Long findMaxId() 
		{
			List<Event> L = all();
			if (L.size() == 0)
			{
				return 0L;
			}
			Event commit = L.get(L.size() - 1);
			return commit.getId();
		}

		@Override
		public List<Event> loadRange(long startId, long n) 
		{
			List<Event> resultL = new ArrayList<>();
			
			for(Event entity : _L)
			{
				if (entity.getId() >= startId)
				{
					resultL.add(entity);
					if (resultL.size() >= n)
					{
						return resultL;
					}
				}
			}
			return resultL;
		}

		@Override
		public List<Event> loadStream(long startId, long streamId) 
		{
			List<Event> resultL = new ArrayList<>();
			
			for(Event entity : _L)
			{
				if (entity.getId() >= startId)
				{
					if (entity.getStreamId() == streamId)
					{
						resultL.add(entity);
					}
				}
			}
			return resultL;
		}

	}