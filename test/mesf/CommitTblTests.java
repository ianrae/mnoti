package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.dao.IDAO;
import org.mef.framework.entitydb.EntityDB;
import org.mef.framework.fluent.QStep;
import org.mef.framework.fluent.Query1;
import org.mef.framework.fluent.QueryContext;
import org.mef.framework.sfx.SfxContext;

public class CommitTblTests extends BaseTest 
{
	public class Commit 
	{
		private Long id;
		private Long streamId;
		private String json;

		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public Long getStreamId() {
			return streamId;
		}
		public void setStreamId(Long streamId) {
			this.streamId = streamId;
		}
		public String getJson() {
			return json;
		}
		public void setJson(String json) {
			this.json = json;
		}
	}
	
	public interface ICommitDAO  extends IDAO
	{
		Commit findById(long id);
		List<Commit> all();
		void save(Commit entity);        
		void update(Commit entity);
		public Query1<Commit> query();
	}	
	
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
	
	@Test
	public void test() throws Exception
	{
		log("sdf");
		ICommitDAO dao = new MockCommitDAO();
		assertEquals(0, dao.size());
		List<Commit> L = dao.all();
		assertEquals(0, L.size());
		
		Commit obj = new Commit();
		obj.setStreamId(10L);
		obj.setJson("{}");
		
		dao.save(obj);
		assertEquals(1, dao.size());
		L = dao.all();
		assertEquals(1, L.size());
		assertEquals(10L, L.get(0).getStreamId().longValue());
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
