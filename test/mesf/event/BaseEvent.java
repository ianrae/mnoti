package mesf.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class BaseEvent
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