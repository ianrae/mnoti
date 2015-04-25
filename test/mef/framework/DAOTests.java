package mef.framework;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.sfx.SfxContext;

public class DAOTests 
{
	public static class Foo
	{
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		private String name;
		private int width;
	}
	
	public interface IDAO<T>
	{
		int size();
		T findById(Long id);
		List<T> all();
	}
	
	public interface IFooDAO extends IDAO<Foo>
	{}
	
	public static class MockFooDAO implements IFooDAO
	{

		@Override
		public int size() {
			return 0;
		}

		@Override
		public Foo findById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Foo> all() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	

	@Test
	public void test() 
	{
		MockFooDAO dao = new MockFooDAO();
		assertEquals(0, dao.size());
	}

	
	//-----------------------------
	private SfxContext ctx;
	
	@Before
	public void init()
	{
		ctx = new SfxContext();
	}
	
}
