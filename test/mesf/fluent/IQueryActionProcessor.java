package mesf.fluent;
import java.util.List;


public interface IQueryActionProcessor<T>
{
	void start(List<QueryAction> actionL);
	T findAny();
	List<T> findMany();
	long findCount();
	void processAction(int index, QueryAction action);
	Class<T> getRelationalFieldType(QueryAction action);
	void setObserver(IDAOObserver<T> observer);
}