package mef.framework;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mef.framework.DAOTests.Car;
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
//		public Foo getFooObject() {
//			return fooObject;
//		}
//		public void setFooObject(Foo fooObject) {
//			this.fooObject = fooObject;
//		}
		private Long id;
		private String name;
		private Long fooId;
//		private Foo fooObject;
		
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
		T findById(Long id);
		List<T> all();
		void save(T t);
		void update(T t);
		void delete(T t);
		void setCache(ICache<T> cache);
		ICache<T> getCache();
		void encache(T t);
		void decache(T t);
	}
	
	public interface IFooDAO extends IDAO<Foo>
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
		}

		@Override
		public void update(Foo t) {
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
	
	public interface ICarDAO extends IDAO<Car>
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
	
//	//cache
//	public static class FooCache implements ICache<Foo>
//	{
//		private Map<Long, Foo> map = new HashMap<Long,Foo>();
//		
//		public void put(Foo foo)
//		{
//			map.put(foo.getId(), foo);
//		}
//		
//		public Foo get(long id)
//		{
//			return map.get(id);
//		}
//		public void remove(long id)
//		{
//			map.remove(id);
//		}
//
//		@Override
//		public void put(long id, Foo entity) {
//			// TODO Auto-generated method stub
//			
//		}
//	}
	
	public static class WrappedFooDAO implements IFooDAO
	{
		private MyCache<Foo> cache = new MyCache<Foo>();
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
			cache.put(t.getId(), t);
			dao.save(t);
		}

		@Override
		public void update(Foo t) 
		{
			cache.put(t.getId(), t);
			dao.update(t);
		}

		@Override
		public void delete(Foo t) 
		{
			cache.remove(t.getId());
			dao.delete(t);
		}

		@Override
		public void setCache(ICache<Foo> cache) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ICache<Foo> getCache() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void encache(Foo t) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void decache(Foo t) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
//	//AR
//	public static class MockCarAR implements ICarDAO
//	{
//		private ICarDAO dao;
//		private WrappedFooDAO wrap;
//		
//		public MockCarAR(ICarDAO dao, WrappedFooDAO wrap)
//		{
//			this.dao = dao;
//			this.wrap = wrap;
//		}
//		
//		@Override
//		public int size() 
//		{
//			return dao.size();
//		}
//
//		@Override
//		public Car findById(Long id) 
//		{
//			Car car = dao.findById(id);
//			if (car != null)
//			{
//				Foo foo = wrap.findById(car.getFooId());
//				car.setFooObject(foo);
//			}
//			return car;
//		}
//
//		@Override
//		public List<Car> all() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void save(Car t) 
//		{
//			dao.save(t);
//		}
//
//		@Override
//		public void update(Car t) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void delete(Car t) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void setCache(ICache<Car> cache) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public ICache<Car> getCache() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public void encache(Car t) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void decach(Car t) {
//			// TODO Auto-generated method stub
//			
//		}
//	}
//

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
	
//	@Test
//	public void testCarAR() 
//	{
//		Foo foo = new Foo();
//		WrappedFooDAO daoF = new WrappedFooDAO(new MockFooDAO());
//		daoF.save(foo);
//		chkLong(1L, foo.getId());
//		Foo foo2 = daoF.findById(1L);
//		chkLong(1L, foo2.getId());
//		
//		MockCarAR ar = new MockCarAR(new MockCarDAO(), daoF);
//		Car car = new Car();
//		car.setName("abc");
//		car.setFooId(foo.getId());
//		ar.save(car);
//		
//		Car car2 = ar.findById(1L);
//		assertNotNull(car2);
//		assertNotNull(car2.getFooObject());
//	}
//	
	//-----------------------------
	
	@Before
	public void init()
	{
		super.init();
	}
	
}
