package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.dao.IDAO;
import org.mef.framework.entitydb.EntityDB;
import org.mef.framework.fluent.Query1;
import org.mef.framework.fluent.QStep;
import org.mef.framework.fluent.QueryContext;
import org.mef.framework.sfx.SfxContext;

public class ObjTableTests extends BaseTest 
{
	public static class ObjectStream
	{
		private Long id;
		private Long snapshotId;

		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getSnapshotId() {
			return snapshotId;
		}
		public void setSnapshotId(Long snapshotId) {
			this.snapshotId = snapshotId;
		}
	}


	public interface IObjectStreamDAO  extends IDAO
	{
		ObjectStream findById(long id);
		List<ObjectStream> all();
		void save(ObjectStream entity);        
		void update(ObjectStream entity);
		public Query1<ObjectStream> query();
	}	

	public class MockObjectStreamDAO implements IObjectStreamDAO
	{
		protected List<ObjectStream> _L = new ArrayList<ObjectStream>();
		protected EntityDB<ObjectStream> _entityDB = new EntityDB<ObjectStream>();
		public QueryContext<ObjectStream> queryctx; 

		@Override
		public void init(SfxContext ctx)
		{
			this.queryctx = new QueryContext<ObjectStream>(ctx, ObjectStream.class);

//			ProcRegistry registry = (ProcRegistry) ctx.getServiceLocator().getInstance(ProcRegistry.class);
//			EntityDBQueryProcessor<ObjectStream> proc = new EntityDBQueryProcessor<ObjectStream>(ctx, _L);
//			registry.registerDao(ObjectStream.class, proc);
		}

		@Override
		public Query1<ObjectStream> query() 
		{
			queryctx.queryL = new ArrayList<QStep>();
			return new Query1<ObjectStream>(queryctx);
		}


		@Override
		public int size() 
		{
			return _L.size();
		}

		@Override
		public ObjectStream findById(long id) 
		{
			ObjectStream entity = this.findActualById(id);
			if (entity != null)
			{
				return entity; //!!new ObjectStream(entity); //return copy
			}
			return null; //not found
		}

		protected ObjectStream findActualById(long id) 
		{
			for(ObjectStream entity : _L)
			{
				if (entity.getId() == id)
				{
					return entity;
				}
			}
			return null; //not found
		}

		@Override
		public List<ObjectStream> all() 
		{
			return _L; //ret copy??!!
		}

		@Override
		public void delete(long id) 
		{
			ObjectStream entity = this.findActualById(id);
			if (entity != null)
			{
				_L.remove(entity);
			}
		}

		@Override
		public void save(ObjectStream entity) 
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
			for(ObjectStream entity : _L)
			{
				if (entity.getId() > used)
				{
					used = entity.getId();
				}
			}
			return used + 1;
		}

		@Override
		public void update(ObjectStream entity) 
		{
			this.delete(entity.getId());
			this.save(entity);
		}

	}


	@Test
	public void test() throws Exception
	{
		log("sdf");
		IObjectStreamDAO dao = new MockObjectStreamDAO();
		assertEquals(0, dao.size());
		List<ObjectStream> L = dao.all();
		assertEquals(0, L.size());
		
		ObjectStream obj = new ObjectStream();
		obj.setSnapshotId(10L);
		
		dao.save(obj);
		assertEquals(1, dao.size());
		L = dao.all();
		assertEquals(1, L.size());
		assertEquals(10L, L.get(0).getSnapshotId().longValue());
		assertEquals(1L, L.get(0).getId().longValue());
	}

	protected static String fix(String s)
	{
		s = s.replace('\'', '"');
		return s;
	}

	@Before
	public void init()
	{
		super.init();
	}
}
