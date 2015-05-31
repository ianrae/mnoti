package mesf.readmodel;

import java.util.ArrayList;
import java.util.List;

import mesf.cache.StreamCache;
import mesf.core.ICommitObserver;
import mesf.core.MContext;
import mesf.persistence.Commit;
import mesf.persistence.Stream;

public class ReadModelRepository implements ICommitObserver, IReadModel
{
	protected List<ReadModel> readModelL = new ArrayList<>();
	private StreamCache strcache;

	public ReadModelRepository(StreamCache strcache)
	{
		this.strcache = strcache;
	}
	
	public void registerReadModel(ReadModel readModel)
	{
		this.readModelL.add(readModel);
	}
	
	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		return true;
	}

	@Override
	public void observe(MContext mtx, Stream stream, Commit commit) 
	{
		for(ReadModel readModel : this.readModelL)
		{
			if (readModel.willAccept(stream, commit))
			{
				readModel.observe(mtx, stream, commit);
			}
		}
	}

	@Override
	public void setLastCommitId(long id) 
	{
		for(ReadModel readModel : this.readModelL)
		{
			readModel.setLastCommitId(id);
		}
	}
	
	public synchronized IReadModel acquire(MContext mtx, Class clazz) 
	{
		for(IReadModel readModel : this.readModelL)
		{
			if (readModel.getClass() == clazz)
			{
				readModel.freshen(mtx);
				return readModel;
			}
		}
		return null;
	}
//	public synchronized Object loadReadModel(ReadModel readModel, ReadModelLoader vloader) throws Exception
//	{
//		List<Commit> L = vloader.loadCommits(readModel.lastCommitId + 1);
//		
//		for(Commit commit : L)
//		{
//			Long streamId = commit.getStreamId();
//			Stream stream = null;
//			if (streamId != null)
//			{
//				stream = strcache.findStream(streamId);
//			}
//			
//			observe(stream, commit);
//		}
//		
//		if (L.size() > 0)
//		{
//			Commit last = L.get(L.size() - 1);
//			readModel.lastCommitId = last.getId();
//		}
//		return readModel.obj;
//	}

	@Override
	public void freshen(MContext mtx) 
	{
		for(ReadModel readModel : this.readModelL)
		{
			if (readModel instanceof IReadModel)
			{
				IReadModel rm = readModel;
				rm.freshen(mtx);
			}
		}
	}

	@Override
	public void setLastEventId(long id)
	{
		for(ReadModel readModel : this.readModelL)
		{
			readModel.setLastEventId(id);
		}
	}
}