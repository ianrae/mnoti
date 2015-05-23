package mesf;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class ObjManagerTests extends BaseTest 
{
	public static class BaseObject
	{
		protected Set<String> setlist = new HashSet<String>();
		
		public List<String> getSetList()
		{
			List<String> L = new ArrayList<>();
			for(String s : setlist)
			{
				L.add(s);
			}
			return L;
		}
		public void clearSetList()
		{
			setlist.clear();
		}
	}
	
	@JsonFilter("myFilter")
	public static class Scooter extends BaseObject
	{
		private int a;
		private int b;
		private String s;
		
		public int getA() {
			return a;
		}
		public void setA(int a) 
		{
			setlist.add("a");
			this.a = a;
		}
		public int getB() {
			return b;
		}
		public void setB(int b) {
			setlist.add("b");
			this.b = b;
		}
		public String getS() {
			return s;
		}
		public void setS(String s) {
			setlist.add("s");
			this.s = s;
		}
		
	}
	
	public static class ObjectMgr<T extends BaseObject>
	{
		private Class<?> clazz;

		public ObjectMgr(Class<?> clazz)
		{
			this.clazz = clazz;
		}

		public T createFromJson(String json) throws Exception
		{
			ObjectMapper mapper = new ObjectMapper();
			T scooter = (T) mapper.readValue(json, clazz);	
			return scooter;
		}
		
		public void mergeJson(T scooter, String json) throws Exception
		{
			ObjectMapper mapper = new ObjectMapper();
			ObjectReader r = mapper.readerForUpdating(scooter);
			r.readValue(json);
		}

		public String renderSetList(T scooter) throws Exception 
		{
			ObjectMapper mapper = new ObjectMapper();
			SimpleFilterProvider sfp = new SimpleFilterProvider();
			// create a  set that holds name of User properties that must be serialized
			Set<String> userFilterSet = new HashSet<String>();
			for(String s : scooter.getSetList())
			{
				userFilterSet.add(s);
			}

			sfp.addFilter("myFilter",SimpleBeanPropertyFilter.filterOutAllExcept(userFilterSet));

			// create an objectwriter which will apply the filters 
			ObjectWriter writer = mapper.writer(sfp);

			String json = writer.writeValueAsString(scooter);
			return json;
		}
	}

	@Test
	public void test() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		
		ObjectMgr<Scooter> mgr = new ObjectMgr(Scooter.class);
		Scooter scooter = mgr.createFromJson(fix(json));
		chkScooter(scooter, 15,26,"abc");
		
		assertEquals(3, scooter.getSetList().size());
		scooter.clearSetList();
		assertEquals(0, scooter.getSetList().size());
	}

	@Test
	public void testPartial() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'s':'def'}";
		ObjectMgr<Scooter> mgr = new ObjectMgr(Scooter.class);
		Scooter scooter = mgr.createFromJson(fix(json));
		assertEquals(15, scooter.a);
		assertEquals(0, scooter.b);
		assertEquals("def", scooter.s);
	}

	@Test
	public void testOverlay() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMgr<Scooter> mgr = new ObjectMgr(Scooter.class);
		Scooter scooter = mgr.createFromJson(fix(json));
		chkScooter(scooter, 15,26,"abc");

		json = "{'a':150,'s':'def'}"; //some properties
		mgr.mergeJson(scooter, fix(json));
		chkScooter(scooter, 150,26,"def");
	}

	@Test
	public void testWriteFilter() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMgr<Scooter> mgr = new ObjectMgr(Scooter.class);
		Scooter scooter = mgr.createFromJson(fix(json));
		chkScooter(scooter, 15,26,"abc");
		
		scooter.clearSetList();
		scooter.setA(100);
		scooter.setB(200);

		String json2 = mgr.renderSetList(scooter); //renders only fields that were changed
		String s = fix("{'a':100,'b':200}");
		assertEquals(s, json2);
	}

	//-----------------------------
	private void chkScooter(Scooter scooter, int expectedA, int expectedB, String expectedStr)
	{
		assertEquals(expectedA, scooter.a);
		assertEquals(expectedB, scooter.b);
		assertEquals(expectedStr, scooter.s);

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
