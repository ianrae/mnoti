package mesf.fluent;
import java.util.List;




public class QueryContext<T> 
{
	public List<QStep> queryL;
	public IQueryActionProcessor<T> proc;
	public Class<T> classOfT;
	
	public QueryContext(Class<T> clazz)
	{
		this.classOfT = clazz;
	}
	
	
}