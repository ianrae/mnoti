package mesf.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class BaseEvent
{
	private Long entityId;

	@JsonIgnore
	public Long getEntityId() {
		return entityId;
	}
	public void setEntityId(Long id) {
		this.entityId = id;
	}

	public abstract BaseEvent clone();
}