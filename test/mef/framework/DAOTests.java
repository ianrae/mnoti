package mef.framework;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;

public class DAOTests extends BaseTest
{
	public static class Entity
	{}
	
	public static abstract class EntityWithLongId extends Entity
	{
		public abstract Long getId();
		public abstract void setId(Long id);
	}
	//!!stringid
	public static abstract class EntityWithId extends EntityWithLongId
	{
	}
	
	
	
	public static class Foo extends EntityWithId
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
		
		@Override
		public Long getId() {
			return id;
		}
		@Override
		public void setId(Long id) {
			this.id = id;
		}
		
		
		
		private Long id;
		private String name;
		private int width;
	}
	
	public static class Car extends EntityWithId
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
	
	public static interface ICache<T>
	{
		void put(long id, T entity);
		T get(long id);
		void remove(long id);
	}
	
	public static class MyCache<T> implements ICache<T>
	{
		private Map<Long, T> map = new HashMap<Long, T>();

		@Override
		public void put(long id, T entity) 
		{
			map.put(id, entity);
		}

		@Override
		public T get(long id) 
		{
			return map.get(id);
		}

		@Override
		public void remove(long id) 
		{
			map.remove(id);
		}
		
	}
	
	public interface IDAO<T>
	{
		int size();
		List<T> all();
		void save(T t);
		void update(T t);
		void delete(T t);
		void setCache(ICache<T> cache);
		ICache<T> getCache();
		void encache(T t);
		void decache(T t);
	}
	
	public interface IDAOWithLongId<T> extends IDAO<T>
	{
		T findById(Long id);
	}
	
	public interface IFooDAO extends IDAOWithLongId<Foo>
	{}
	
	public static class MockFooDAO implements IFooDAO
	{
		private long nextId = 1;
		private Map<Long, Foo> map = new HashMap<Long, Foo>();
		ICache<Foo> cache;

		@Override
		public int size() {
			return map.size();
		}

		@Override
		public Foo findById(Long id) 
		{
			if (this.cache != null)
			{
				Foo foo = cache.get(id);
				if (foo != null)
				{
					return foo;
				}
			}
			return map.get(id);
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
			map.put(t.getId(), t);
			this.encache(t);
		}

		@Override
		public void update(Foo t) 
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Foo t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCache(ICache<Foo> cache) 
		{
			this.cache = cache;
		}

		@Override
		public ICache<Foo> getCache() 
		{
			return this.cache;
		}

		@Override
		public void encache(Foo t) 
		{
			if (this.cache != null)
			{
				this.cache.put(t.getId(), t);
			}
		}

		@Override
		public void decache(Foo t) 
		{
			if (this.cache != null)
			{
				this.cache.remove(t.getId());
			}
		}
		
	}
	
	public interface ICarDAO extends IDAOWithLongId<Car>
	{}
	
	public static class MockCarDAO implements ICarDAO
	{
		private Map<Long, Car> map = new HashMap<Long, Car>();
		private long nextId = 1;

		@Override
		public int size() 
		{
			return map.size();
		}

		@Override
		public Car findById(Long id) 
		{
			return map.get(id);
		}

		@Override
		public List<Car> all() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void save(Car t) 
		{
			t.setId(this.nextId++);
			map.put(t.getId(), t);
		}

		@Override
		public void update(Car t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Car t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCache(ICache<Car> cache) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ICache<Car> getCache() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void encache(Car t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void decache(Car t) {
			// TODO Auto-generated method stub
			
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
		IDAO<Foo> dao = new MockFooDAO();
		dao.setCache(new MyCache<Foo>());
		dao.save(foo);
		chkLong(1L, foo.getId());
		
		MockCarDAO daocar = new MockCarDAO();
		Car car = new Car();
		car.setName("abc");
		car.setFooId(foo.getId());
		daocar.save(car);
	}
	
	@Test
	public void testCarAR() 
	{
		Foo foo = new Foo();
		IDAOWithLongId<Foo> daoF = new MockFooDAO();
		daoF.setCache(new MyCache<Foo>());
		daoF.save(foo);
		chkLong(1L, foo.getId());
		Foo foo2 = daoF.findById(1L);
		chkLong(1L, foo2.getId());
		
		ICarDAO ar = new MockCarDAO();
		Car car = new Car();
		car.setName("abc");
		car.setFooId(foo.getId());
		ar.save(car);
		
		Car car2 = ar.findById(1L);
		assertNotNull(car2);
		assertNotNull(daoF.findById(car2.getFooId()));
	}
	
	//-----------------------------
	
	@Before
	public void init()
	{
		super.init();
	}
	
}
