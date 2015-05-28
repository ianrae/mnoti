package mesf.persistence;

import java.util.List;

import org.mef.framework.dao.IDAO;
import org.mef.framework.fluent.Query1;

public interface ICommitDAO  extends IDAO
{
	Commit findById(long id);
	List<Commit> all();
	List<Commit> loadRange(long startId, long n);
	List<Commit> loadStream(long startId, long streamId);
	void save(Commit entity);        
	void update(Commit entity);
	public Query1<Commit> query();
	public Long findMaxId();
}