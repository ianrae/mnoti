package mesf.readmodel;

import java.util.ArrayList;
import java.util.List;

import mesf.core.Commit;
import mesf.core.ICommitObserver;
import mesf.core.Stream;
import mesf.core.StreamCache;

public class ReadModelManager implements ICommitObserver
{
	protected List<ReadModel> readModelL = new ArrayList<>();
	private StreamCache strcache;

	public ReadModelManager(StreamCache strcache)
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
	public void observe(Stream stream, Commit commit) 
	{
		for(ReadModel readModel : this.readModelL)
		{
			if (readModel.willAccept(stream, commit))
			{
				readModel.observe(stream, commit);
			}
		}
	}
	
	public synchronized Object loadReadModel(ReadModel readModel, ReadModelLoader vloader) throws Exception
	{
		List<Commit> L = vloader.loadCommits(readModel.lastCommitId + 1);
		
		for(Commit commit : L)
		{
			Long streamId = commit.getStreamId();
			Stream stream = null;
			if (streamId != null)
			{
				stream = strcache.findStream(streamId);
			}
			
			observe(stream, commit);
		}
		
		if (L.size() > 0)
		{
			Commit last = L.get(L.size() - 1);
			readModel.lastCommitId = last.getId();
		}
		return readModel.obj;
	}
}