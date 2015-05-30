package mesf.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//immutable
public abstract class BaseEvent
{
//	@JsonProperty private final Long entityId;
	protected long entityId;

	public BaseEvent()
	{
		entityId = 0L;
	}
	public BaseEvent(long entityid)
	{
		this.entityId = entityid;
	}

	@JsonIgnore
	public long getEntityId() {
		return entityId;
	}
}