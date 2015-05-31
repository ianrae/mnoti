package mesf.presenter;

import java.util.ArrayList;
import java.util.List;

import mesf.core.CommitWriter;
import mesf.core.MContext;
import mesf.entity.Entity;
import mesf.event.Event;
import mesf.event.EventWriter;
import mesf.log.Logger;

public abstract class Presenter //extends CommandProcessor
	{
		protected Reply baseReply;
		protected MContext mtx;
		protected CommitWriter commitWriter;
		protected EventWriter eventWriter;
		protected List<IReqquestInterceptor> interceptL = new ArrayList<>();
		
		public Presenter(MContext mtx)
		{
			this.mtx = mtx;
			this.commitWriter = new CommitWriter(mtx);
			this.eventWriter = new EventWriter(mtx);
		}
		
		public void addInterceptor(IReqquestInterceptor intercept)
		{
			interceptL.add(intercept);
		}
		
		protected abstract Reply createReply();
		
		public Reply process(Request request) 
		{
			Reply reply = null;
			try
			{
				reply = doProcess(request);
			}
			catch(NotLoggedInException ex)
			{
				baseReply.setDestination(Reply.FOWARD_NOT_AUTHENTICATED);
			}
			catch(NotAuthorizedException ex)
			{
				baseReply.setDestination(Reply.FOWARD_NOT_AUTHORIZED);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Logger.log("cont-after-except");
//				this.addErrorException(e, "formatter");
				baseReply.setFailed(true);
				baseReply.setDestination(Reply.FOWARD_ERROR);
			}
			return reply;
		}
		
		private Reply doProcess(Request request) 
		{
			this.baseReply = this.createReply();
			String methodName = getMethodName(request);
			Logger.log(String.format("[MEF] %s.%s ", this.getClass().getSimpleName(), methodName));

			if (! processInterceptors(request, baseReply))
			{
				return baseReply;
			}
			
			MethodInvoker invoker = new MethodInvoker(this, methodName, Request.class);
			invoker.call(request, baseReply);			
			
			afterRequest(request); //always do it
			
			return baseReply;
		}
		
		private boolean processInterceptors(Request request, Reply reply) 
		{
			InterceptorContext itx = new InterceptorContext();
			for(IReqquestInterceptor interceptor : this.interceptL)
			{
				interceptor.process(request, reply, itx);
				if (itx.haltProcessing)
				{
					return false; //halt
				}
			}
			beforeRequest(request, itx);
			if (itx.haltProcessing)
			{
				return false; //halt
			}
			return true; //continue
		}

		private String getMethodName(Request request) 
		{
//			String methodName = request.getClass().getName();
			String methodName = request.getClass().getSimpleName(); //avoid MyPres$InsertCmd
			int pos = methodName.lastIndexOf('.');
			if (pos > 0)
			{
				methodName = methodName.substring(pos + 1);
				pos = methodName.indexOf('$');
				if (pos > 0)
				{
					methodName = methodName.substring(pos + 1);
				}
			}
			methodName = "on" + methodName;
			return methodName;
		}
		
		protected void beforeRequest(Request request, InterceptorContext itx)
		{}
		protected void afterRequest(Request request)
		{}
		
		protected void insertEntity(Entity obj)
		{
			this.commitWriter.insertEntity(obj);
		}
		protected void updateEntity(Entity obj)
		{
			this.commitWriter.updateEntity(obj);
		}
		protected void publishEvent(Event ev)
		{
			this.eventWriter.insertEvent(ev);
		}
	}