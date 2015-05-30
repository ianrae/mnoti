package mesf;

import static org.junit.Assert.assertEquals;
import mesf.event.BaseEvent;
import mesf.event.EventMgr;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventTests extends BaseMesfTest
{
//	public static class ScooterAddedEvent extends BaseEvent
//	{
//		private final int z;
//
//		@JsonCreator
//		public ScooterAddedEvent( @JsonProperty("entityId") long eventid, @JsonProperty("z") int z)
//		{
//			super(eventid);
//			this.z = z;
//		}
//		public int getZ() {
//			return z;
//		}
//	}

	public static class ScooterAddedEvent extends BaseEvent
	{
//		@JsonProperty private int z;
		private int z;

		public ScooterAddedEvent()
		{}
		public ScooterAddedEvent(long eventId, int z)
		{
			super(eventId);
//			this.entityId = eventId;
			this.z = z;
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
//		String s = fix("{'entityId':0,'z':15}");
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