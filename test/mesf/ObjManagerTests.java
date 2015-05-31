package mesf;

import static org.junit.Assert.assertEquals;
import mesf.entity.Entity;
import mesf.entity.EntityMgr;

import org.junit.Before;
import org.junit.Test;

public class ObjManagerTests extends BaseMesfTest 
{
	public static class Scooter extends Entity
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
		@Override
		public Entity clone() 
		{
			Scooter copy = new Scooter();
			copy.setId(getId()); //!
			copy.a = this.a;
			copy.b = this.b;
			copy.s = this.s;
			return copy;
		}

	}

	@Test
	public void test() throws Exception
	{
		log("sdf");
		String json = "{'a':15,'b':26,'s':'abc'}";

		EntityMgr<Scooter> mgr = new EntityMgr(Scooter.class);
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
		EntityMgr<Scooter> mgr = new EntityMgr(Scooter.class);
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
		EntityMgr<Scooter> mgr = new EntityMgr(Scooter.class);
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
		EntityMgr<Scooter> mgr = new EntityMgr(Scooter.class);
		Scooter scooter = mgr.createFromJson(fix(json));
		chkScooter(scooter, 15,26,"abc");

		scooter.clearSetList();
		scooter.setA(100);
		scooter.setB(200);

		String json2 = mgr.renderSetList(scooter); //renders only fields that were changed
		String s = fix("{'a':100,'b':200}");
		assertEquals(s, json2);
	}

	@Test
	public void testRender() throws Exception
	{
		String json = "{'a':15,'b':26,'s':'abc'}";
		EntityMgr<Scooter> mgr = new EntityMgr(Scooter.class);
		Scooter scooter = mgr.createFromJson(fix(json));
		chkScooter(scooter, 15,26,"abc");

		scooter.clearSetList();
		scooter.setA(100);
		scooter.setB(200);

		String json2 = mgr.renderEntity(scooter);
//		String s = fix("{'a':100,'b':200,'s':'abc','setList':['a','b']}");
		String s = fix("{'a':100,'b':200,'s':'abc'}");
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
