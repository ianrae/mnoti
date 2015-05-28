package mesf.readmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mesf.UserTests;
import mesf.UserTests.User;
import mesf.core.BaseObject;
import mesf.core.Commit;
import mesf.core.MContext;
import mesf.core.Projector;
import mesf.core.Stream;

public class AllIdsRM<T> extends ReadModel
{
	public Map<Long,T> map = new TreeMap<>(); //sorted
	private String type;
	
	public AllIdsRM(String type)
	{
		this.type = type;
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
	public void observe(Stream stream, Commit commit) 
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
			BaseObject obj = mtx.loadObject(User.class, id);
			L.add((T) obj);
		}
		return L;
	}
}