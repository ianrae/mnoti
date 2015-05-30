package mesf.persistence;

import java.util.ArrayList;
import java.util.List;

import mesf.entitydb.EntityDB;


public class MockStreamDAO implements IStreamDAO
	{
		protected List<Stream> _L = new ArrayList<Stream>();
		protected EntityDB<Stream> _entityDB = new EntityDB<Stream>();

//		@Override
//		public void init(SfxContext ctx)
//		{
//			this.queryctx = new QueryContext<Stream>(ctx, Stream.class);
//
////			ProcRegistry registry = (ProcRegistry) ctx.getServiceLocator().getInstance(ProcRegistry.class);
////			EntityDBQueryProcessor<ObjectStream> proc = new EntityDBQueryProcessor<ObjectStream>(ctx, _L);
////			registry.registerDao(ObjectStream.class, proc);
//		}
//
//		@Override
//		public Query1<Stream> query() 
//		{
//			queryctx.queryL = new ArrayList<QStep>();
//			return new Query1<Stream>(queryctx);
//		}
//

		@Override
		public int size() 
		{
			return _L.size();
		}

		@Override
		public Stream findById(long id) 
		{
			Stream entity = this.findActualById(id);
			if (entity != null)
			{
				return entity; //!!new ObjectStream(entity); //return copy
			}
			return null; //not found
		}

		protected Stream findActualById(long id) 
		{
			for(Stream entity : _L)
			{
				if (entity.getId() == id)
				{
					return entity;
				}
			}
			return null; //not found
		}

		@Override
		public List<Stream> all() 
		{
			return _L; //ret copy??!!
		}

		@Override
		public void delete(long id) 
		{
			Stream entity = this.findActualById(id);
			if (entity != null)
			{
				_L.remove(entity);
			}
		}

		@Override
		public void save(Stream entity) 
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
			for(Stream entity : _L)
			{
				if (entity.getId() > used)
				{
					used = entity.getId();
				}
			}
			return used + 1;
		}

		@Override
		public void update(Stream entity) 
		{
			this.delete(entity.getId());
			this.save(entity);
		}
		
		@Override
		public List<Stream> loadRange(long startId, long n) 
		{
			List<Stream> resultL = new ArrayList<>();
			
			for(Stream entity : _L)
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

	}