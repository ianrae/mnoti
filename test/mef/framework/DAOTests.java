package mef.framework;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mef.framework.DAOTests.Foo;
import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;

public class DAOTests extends BaseTest
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
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		private Long id;
		private String name;
		private int width;
	}
	
	public static class Car
	{
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Long getFooId() {
			return fooId;
		}
		public void setFooId(Long fooId) {
			this.fooId = fooId;
		}
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		private Long id;
		private String name;
		private Long fooId;
		
	}
	
	public interface IDAO<T>
	{
		int size();
		T findById(Long id);
		List<T> all();
		void save(T t);
		void update(T t);
		void delete(T t);
	}
	
	public interface IFooDAO extends IDAO<Foo>
	{}
	
	public static class MockFooDAO implements IFooDAO
	{
		private static long nextId = 1;

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

		@Override
		public void save(Foo t) 
		{
			t.setId(nextId++);
		}

		@Override
		public void update(Foo t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Foo t) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public interface ICarDAO extends IDAO<Car>
	{}
	
	public static class MockCarDAO implements ICarDAO
	{

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Car findById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Car> all() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void save(Car t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void update(Car t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Car t) {
			// TODO Auto-generated method stub
			
		}
	}
	
	//cache
	public static class FooCache
	{
		private Map<Long, Foo> map = new HashMap<Long,Foo>();
		
		public void put(Foo foo)
		{
			map.put(foo.getId(), foo);
		}
		
		public Foo get(long id)
		{
			return map.get(id);
		}
		public void remove(long id)
		{
			map.remove(id);
		}
	}
	
	public static class WrappedFooDAO implements IFooDAO
	{
		private FooCache cache = new FooCache();
		private IFooDAO dao;
		
		public WrappedFooDAO(IFooDAO dao) 
		{
			this.dao = dao;
		}
		
		@Override
		public int size() 
		{
			return dao.size();
		}

		@Override
		public Foo findById(Long id) 
		{
			Foo foo = cache.get(id);
			if (foo != null)
			{
				return foo;
			}
			
			foo = dao.findById(id);
			return foo;
		}

		@Override
		public List<Foo> all() 
		{
			return dao.all();
		}

		@Override
		public void save(Foo t) 
		{
			cache.put(t);
			dao.save(t);
		}

		@Override
		public void update(Foo t) 
		{
			cache.put(t);
			dao.update(t);
		}

		@Override
		public void delete(Foo t) 
		{
			cache.remove(t.getId());
			dao.delete(t);
		}
		
	}

	@Test
	public void test() 
	{
		MockFooDAO dao = new MockFooDAO();
		assertEquals(0, dao.size());
	}

	@Test
	public void testCar() 
	{
		Foo foo = new Foo();
		WrappedFooDAO daoF = new WrappedFooDAO(new MockFooDAO());
		daoF.save(foo);
		chkLong(1L, foo.getId());
		
		MockCarDAO dao = new MockCarDAO();
		Car car = new Car();
		car.setName("abc");
		car.setFooId(foo.getId());
		dao.save(car);
	}
	
	
	//-----------------------------
	
	@Before
	public void init()
	{
		super.init();
	}
	
}
