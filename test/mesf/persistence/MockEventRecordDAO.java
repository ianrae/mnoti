package mesf.persistence;

import java.util.ArrayList;
import java.util.List;

import mesf.entitydb.EntityDB;


public class MockEventRecordDAO implements IEventRecordDAO
	{
		protected List<EventRecord> _L = new ArrayList<EventRecord>();
		protected EntityDB<EventRecord> _entityDB = new EntityDB<EventRecord>();

//		@Override
//		public void init(SfxContext ctx)
//		{
//			this.queryctx = new QueryContext<EventRecord>(ctx, EventRecord.class);
//
////			ProcRegistry registry = (ProcRegistry) ctx.getServiceLocator().getInstance(ProcRegistry.class);
////			EntityDBQueryProcessor<ObjectEvent> proc = new EntityDBQueryProcessor<ObjectEvent>(ctx, _L);
////			registry.registerDao(ObjectEvent.class, proc);
//		}
//
//		@Override
//		public Query1<EventRecord> query() 
//		{
//			queryctx.queryL = new ArrayList<QStep>();
//			return new Query1<EventRecord>(queryctx);
//		}


		@Override
		public int size() 
		{
			return _L.size();
		}

		@Override
		public EventRecord findById(long id) 
		{
			EventRecord entity = this.findActualById(id);
			if (entity != null)
			{
				return entity; //!!new ObjectEvent(entity); //return copy
			}
			return null; //not found
		}

		protected EventRecord findActualById(long id) 
		{
			for(EventRecord entity : _L)
			{
				if (entity.getId() == id)
				{
					return entity;
				}
			}
			return null; //not found
		}

		@Override
		public List<EventRecord> all() 
		{
			return _L; //ret copy??!!
		}

		@Override
		public void delete(long id) 
		{
			EventRecord entity = this.findActualById(id);
			if (entity != null)
			{
				_L.remove(entity);
			}
		}

		@Override
		public void save(EventRecord entity) 
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
			for(EventRecord entity : _L)
			{
				if (entity.getId() > used)
				{
					used = entity.getId();
				}
			}
			return used + 1;
		}

		@Override
		public void update(EventRecord entity) 
		{
			this.delete(entity.getId());
			this.save(entity);
		}

		@Override
		public Long findMaxId() 
		{
			List<EventRecord> L = all();
			if (L.size() == 0)
			{
				return 0L;
			}
			EventRecord commit = L.get(L.size() - 1);
			return commit.getId();
		}

		@Override
		public List<EventRecord> loadRange(long startId, long n) 
		{
			List<EventRecord> resultL = new ArrayList<>();
			
			for(EventRecord entity : _L)
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
		public List<EventRecord> loadStream(long startId, long streamId) 
		{
			List<EventRecord> resultL = new ArrayList<>();
			
			for(EventRecord entity : _L)
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