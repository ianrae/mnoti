package mesf.persistence;


public class Stream 
{
	private Long id;
	private String type;
	private Long snapshotId;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getSnapshotId() {
		return snapshotId;
	}
	public void setSnapshotId(Long snapshotId) {
		this.snapshotId = snapshotId;
	}
}
