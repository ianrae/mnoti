package mesf.event;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class EventMgr<T extends Event> implements IEventMgr
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
	public String renderEntity(Event obj) throws Exception 
	{
		ObjectMapper mapper = new ObjectMapper();
//		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);		
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
	public Event rehydrate(String json) throws Exception 
	{
		Event obj = this.createFromJson(json);
		return obj;
	}
}