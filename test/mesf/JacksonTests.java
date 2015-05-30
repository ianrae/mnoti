package mesf;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JacksonTests extends BaseMesfTest 
{
	@JsonFilter("myFilter")
	public static class Taxi
	{
		public int a;
		public int b;
		public String s;
	}

	@Test
	public void test() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals(15, taxi.a);
		assertEquals(26, taxi.b);
		assertEquals("abc", taxi.s);
	}

	@Test
	public void testPartial() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'s':'def'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals(15, taxi.a);
		assertEquals(0, taxi.b);
		assertEquals("def", taxi.s);
	}

	@Test
	public void testOverlay() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		assertEquals(15, taxi.a);
		assertEquals(26, taxi.b);
		assertEquals("abc", taxi.s);
		chkTaxi(taxi, 15,26,"abc");

		ObjectReader r = mapper.readerForUpdating(taxi);
		json = "{'a':150,'s':'def'}";
		r.readValue(fix(json));
		chkTaxi(taxi, 150,26,"def");
	}

	@Test
	public void testWriteFilter() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";
		ObjectMapper mapper = new ObjectMapper();
		Taxi taxi = mapper.readValue(fix(json), Taxi.class);	
		chkTaxi(taxi, 15,26,"abc");

		SimpleFilterProvider sfp = new SimpleFilterProvider();
		// create a  set that holds name of User properties that must be serialized
		Set userFilterSet = new HashSet<String>();
		userFilterSet.add("a");
		userFilterSet.add("s");

		sfp.addFilter("myFilter",SimpleBeanPropertyFilter.filterOutAllExcept(userFilterSet));

		// create an objectwriter which will apply the filters 
		ObjectWriter writer = mapper.writer(sfp);

		String json2 = writer.writeValueAsString(taxi);
		String s = fix("{'a':15,'s':'abc'}");
		assertEquals(s, json2);
	}

	//-----------------------------
	private void chkTaxi(Taxi taxi, int expectedA, int expectedB, String expectedStr)
	{
		assertEquals(expectedA, taxi.a);
		assertEquals(expectedB, taxi.b);
		assertEquals(expectedStr, taxi.s);

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
