package mesf.core;

import mesf.persistence.Commit;


public interface ICommitObserver
{
	boolean willAccept(Stream stream, Commit commit);
	void observe(Stream stream, Commit commit);
}