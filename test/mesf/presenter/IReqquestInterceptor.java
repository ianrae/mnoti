package mesf.presenter;


public interface IReqquestInterceptor
{
	void process(Request request, Reply reply, InterceptorContext itx);
}