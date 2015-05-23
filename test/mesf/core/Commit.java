package mesf.core;

public class Commit 
{
	private Long id;
	private Long streamId;
	private String json;

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
}