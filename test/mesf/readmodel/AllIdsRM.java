package mesf.readmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mesf.UserTests;
import mesf.UserTests.User;
import mesf.core.MContext;
import mesf.core.Projector;
import mesf.entity.Entity;
import mesf.persistence.Commit;
import mesf.persistence.Stream;

public class AllIdsRM<T> extends ReadModel
{
	public Map<Long,T> map = new TreeMap<>(); //sorted
	private String type;
	private Class clazz;
	
	public AllIdsRM(String type, Class clazz)
	{
		this.type = type;
		this.clazz = clazz;
	}
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		if (stream != null && stream.getType().equals(type)) 
		{
			return true;
		}
		return false;
	}

	@Override
	public void observe(MContext mtx, Stream stream, Commit commit) 
	{
		switch(commit.getAction())
		{
		case 'I':
		case 'S':
			map.put(commit.getStreamId(), null);
			break;
		case 'U':
			break;
		case 'D':
			map.remove(commit.getStreamId());
			break;
		default:
			break;
		}
	}
	
	public void freshen(MContext mtx)
	{
		Projector projector = mtx.createProjector();
		projector.run(mtx, this, this.lastCommitId);
	}
	
	public List<T> queryAll(MContext mtx) throws Exception
	{
		List<T> L = new ArrayList<>();
		for(Long id : map.keySet())
		{
			Entity obj = mtx.loadEntity(clazz, id);
			L.add((T) obj);
		}
		return L;
	}
}