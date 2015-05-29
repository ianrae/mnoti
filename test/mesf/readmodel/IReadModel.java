package mesf.readmodel;

import mesf.core.MContext;

public interface IReadModel {

	void setLastCommitId(long id);
	void setLastEventId(long id);
	void freshen(MContext mtx);
}
