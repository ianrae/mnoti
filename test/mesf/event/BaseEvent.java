package mesf.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

//immutable
public abstract class BaseEvent
{
	private final Long entityId;
	
	public BaseEvent(long entityid)
	{
		this.entityId = entityid;
	}

//	@JsonIgnore
	public Long getEntityId() {
		return entityId;
	}
}