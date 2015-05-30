package mesf.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//immutable
public abstract class Event
{
//	@JsonProperty private final Long entityId;
	protected long entityId;

	public Event()
	{
		entityId = 0L;
	}
	public Event(long entityid)
	{
		this.entityId = entityid;
	}

	//since is immutable we should serialize entityId
//	@JsonIgnore
	public long getEntityId() {
		return entityId;
	}
}