package mesf.core;

import java.util.List;

import org.mef.framework.dao.IDAO;
import org.mef.framework.fluent.Query1;

public interface ICommitDAO  extends IDAO
{
	Commit findById(long id);
	List<Commit> all();
	void save(Commit entity);        
	void update(Commit entity);
	public Query1<Commit> query();
}