package mesf;

import static org.junit.Assert.*;

import java.io.IOException;

import mef.framework.helpers.BaseTest;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class JacksonTests extends BaseTest 
{
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
