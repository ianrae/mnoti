package mesf.view;

import java.util.ArrayList;
import java.util.List;

import mesf.core.Commit;
import mesf.core.ICommitObserver;
import mesf.core.IStreamDAO;
import mesf.core.Stream;

public class ViewManager implements ICommitObserver
{
	protected List<BaseView> viewObserversL = new ArrayList<>();
	private IStreamDAO streamDAO;

	public ViewManager(IStreamDAO streamDAO)
	{
		this.streamDAO = streamDAO;
	}
	
	public void registerViewObserver(BaseView view)
	{
		this.viewObserversL.add(view);
	}
	
	@Override
	public boolean willAccept(Stream stream, Commit commit) 
	{
		return true;
	}

	@Override
	public void observe(Stream stream, Commit commit) 
	{
		for(BaseView view : this.viewObserversL)
		{
			if (view.willAccept(stream, commit))
			{
				view.observe(stream, commit);
			}
		}
	}
	
	public synchronized Object loadView(BaseView view, ViewLoader vloader) throws Exception
	{
		List<Commit> L = vloader.loadCommits(view.lastCommitId + 1);
		
		for(Commit commit : L)
		{
			Long streamId = commit.getStreamId();
			Stream stream = null;
			if (streamId != null)
			{
				stream = streamDAO.findById(streamId);
			}
			
			observe(stream, commit);
		}
		
		if (L.size() > 0)
		{
			Commit last = L.get(L.size() - 1);
			view.lastCommitId = last.getId();
		}
		return view.obj;
	}
}