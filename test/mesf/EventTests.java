package mesf;

import static org.junit.Assert.assertEquals;
import mesf.ObjManagerTests.Scooter;
import mesf.entity.EntityMgr;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class EventTests extends BaseMesfTest
{
	public static abstract class BaseEvent
	{
		private Long id;

		@JsonIgnore
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}

		public abstract BaseEvent clone();
	}	

	public static class ScooterAddedEvent extends BaseEvent
	{
		private int z;

		@Override
		public BaseEvent clone()
		{
			ScooterAddedEvent copy = new ScooterAddedEvent();
			copy.setId(getId()); //!
			copy.z = this.z;
			return copy;
		}

		public int getZ() {
			return z;
		}
	}



	public interface IEntityMgr
	{
		String getTypeName();
		String renderEntity(BaseEvent obj) throws Exception ;
		BaseEvent rehydrate(String json) throws Exception;
	}

	public static class EventMgr<T extends BaseEvent> implements IEntityMgr
	{
		private Class<?> clazz;

		public EventMgr(Class<?> clazz)
		{
			this.clazz = clazz;
		}

		public T createFromJson(String json) throws Exception
		{
			ObjectMapper mapper = new ObjectMapper();
			T scooter = (T) mapper.readValue(json, clazz);	
			return scooter;
		}

		@Override
		public String renderEntity(BaseEvent obj) throws Exception 
		{
			ObjectMapper mapper = new ObjectMapper();
			SimpleFilterProvider dummy = new SimpleFilterProvider();
			dummy.setFailOnUnknownId(false);		

			// create an objectwriter which will apply the filters 
			ObjectWriter writer = mapper.writer(dummy);
			String json = writer.writeValueAsString(obj);
			return json;
		}

		@Override
		public String getTypeName() 
		{
			String type = clazz.getSimpleName().toLowerCase(); //default name. if class name changes we can still use same name
			return type;
		}

		@Override
		public BaseEvent rehydrate(String json) throws Exception 
		{
			BaseEvent obj = this.createFromJson(json);
			return obj;
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
