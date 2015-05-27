package mesf.core;

import java.util.List;

import org.mef.framework.dao.IDAO;
import org.mef.framework.fluent.Query1;

public interface IStreamDAO  extends IDAO
{
	Stream findById(long id);
	List<Stream> loadRange(long startId, long n);
	List<Stream> all();
	void save(Stream entity);        
	void update(Stream entity);
	public Query1<Stream> query();
}