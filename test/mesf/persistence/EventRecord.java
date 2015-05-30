package mesf.persistence;

public class EventRecord 
{
	private Long id;
	private Long streamId;
	private String json;
	private String eventName; //event name
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getStreamId() {
		return streamId;
	}
	public void setStreamId(Long streamId) {
		this.streamId = streamId;
	}
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
}