package mesf.persistence;

import java.util.List;


public interface IStreamDAO  extends IDAO
{
	Stream findById(long id);
	List<Stream> loadRange(long startId, long n);
	List<Stream> all();
	void save(Stream entity);        
	void update(Stream entity);
}