package mesf;

import static org.junit.Assert.assertEquals;
import mesf.event.BaseEvent;
import mesf.event.EventMgr;

import org.junit.Before;
import org.junit.Test;

public class EventTests extends BaseMesfTest
{
	public static class ScooterAddedEvent extends BaseEvent
	{
		private int z;

		@Override
		public BaseEvent clone()
		{
			ScooterAddedEvent copy = new ScooterAddedEvent();
			copy.setEntityId(getEntityId()); //!
			copy.z = this.z;
			return copy;
		}

		public int getZ() {
			return z;
		}
	}



	@Test
	public void test() throws Exception 
	{
		log("sdf");
		String json = "{'z':15}";

		EventMgr<ScooterAddedEvent> mgr = new EventMgr(ScooterAddedEvent.class);
		ScooterAddedEvent scooter = mgr.createFromJson(fix(json));
		chkScooter(scooter, 15);
		
		String json2 = mgr.renderEntity(scooter);
		String s = fix("{'z':15}");
		assertEquals(s, json2);
	}


	//-----------------------------
	private void chkScooter(ScooterAddedEvent scooter, int expectedZ)
	{
		assertEquals(expectedZ, scooter.z);
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
