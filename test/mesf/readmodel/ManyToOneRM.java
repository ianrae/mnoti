package mesf.readmodel;

import java.util.Map;
import java.util.TreeMap;

import mesf.core.MContext;
import mesf.core.Projector;
import mesf.persistence.Commit;
import mesf.persistence.Stream;

public class ManyToOneRM extends ReadModel
{
	public interface IResolver
	{
		Long getForiegnKey(MContext mtx, Commit commit);
	}
	
	public Map<Long,TreeMap<Long,Long>> map = new TreeMap<>(); //sorted
	private String type1;
	private Class clazz1;
	private String typeMany;
	private Class clazzMany;
	private IResolver resolver;
	
	public ManyToOneRM(String type1, Class clazz1, String typeMany, Class clazzMany, IResolver resolver)
	{
		this.type1 = type1;
		this.clazz1 = clazz1;
		this.typeMany = typeMany;
		this.clazzMany = clazzMany;
		this.resolver = resolver;
	}
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		if (stream == null) 
		{
			return false;
		}
		
		if (stream.getType().equals(type1) || stream.getType().equals(typeMany)) 
		{
			return true;
		}
		return false;
	}

	@Override
	public void observe(MContext mtx, Stream stream, Commit commit) 
	{
		if (stream.getType().equals(type1))
		{
			switch(commit.getAction())
			{
			case 'I':
			case 'S':
				map.put(commit.getStreamId(), new TreeMap<Long,Long>());
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
		else //type many
		{
			Long key = resolver.getForiegnKey(mtx, commit); //commit is Task, get task.userId
			TreeMap<Long,Long> refL = map.get(key);
			switch(commit.getAction())
			{
			case 'I':
			case 'S':
				refL.put(commit.getStreamId(), 0L); 
				break;
			case 'U':
				break;
			case 'D':
				refL.remove(commit.getStreamId()); 
				break;
			default:
				break;
			}
		}
	}
	
	public void freshen(MContext mtx)
	{
		Projector projector = mtx.createProjector();
		projector.run(mtx, this, this.lastCommitId);
	}
	
	public Map<Long,Long> queryAll(MContext mtx, Long targetId) throws Exception
	{
		TreeMap<Long,Long> refL = map.get(targetId);
		return refL;
	}
}