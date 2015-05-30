package mesf.presenter;


public class BindingIntercept implements IReqquestInterceptor
{
	private int failureDestination;
	public BindingIntercept(int failureDestination)
	{
		this.failureDestination = failureDestination;
	}

	@Override
	public void process(Request request, Reply reply, InterceptorContext itx) 
	{
		if (request.getFormBinder() == null)
		{
			return;
		}

		if (! request.getFormBinder().bind())
		{
			//propogate validation errors
			//set reply to VIEW_EDIT -pass in ctor Boundary.creatPres(new FormBinder<User>(VIEW_EDIT);
			//nice then onUpdate only called if valid
			reply.setDestination(failureDestination);
			itx.haltProcessing = true;
		}		
	}
}