package mesf.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class EntityMgr<T extends Entity> implements IEntityMgr
{
	private Class<?> clazz;

	public EntityMgr(Class<?> clazz)
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

	@Override
	public String renderEntity(Entity obj) throws Exception 
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
	public String renderPartial(Entity obj) throws Exception 
	{
		return this.renderSetList((T) obj);
	}

	@Override
	public Entity rehydrate(String json) throws Exception 
	{
		Entity obj = this.createFromJson(json);
		return obj;
	}

	@Override
	public void mergeHydrate(Entity obj, String json) throws Exception 
	{
		mergeJson((T) obj, json);
	}
}