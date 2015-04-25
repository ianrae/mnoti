package mef.framework;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.auth.IAuthorizer;
import org.mef.framework.auth2.AuthUser;
import org.mef.framework.replies.Reply;
import org.mef.framework.sfx.SfxBaseObj;
import org.mef.framework.sfx.SfxContext;

public class OtherTests 
{
	//so the action composition just do to Object.createInstance and call parameterless ctor
	public static class TheGlobal
	{
		public static SfxContext theCtx;
	}
	
	public static abstract class XPresenter extends SfxBaseObj 
	{
		protected AuthUser authUser;
		protected IAuthorizer auth;
		protected Reply baseReply;

		public XPresenter()
		{
			super(TheGlobal.theCtx);
		}
		
		public Reply getBaseReply()
		{
			return baseReply;
		}
		//if controller has DI then it can inject this
		public void setAuthorizer(IAuthorizer auth)
		{
			this.auth = auth;
		}
		
		public boolean doBeforeAction(AuthUser user)
		{
			this.authUser = user;
			return onBeforeAction(user);
		}
		
		public void afterAction()
		{
			if (baseReply == null)
			{
				return;
			}
			if (! baseReply.failed() && ! baseReply.isForward())
			{
				this.onAfterAction();
			}
		}
		
		protected abstract boolean onBeforeAction(AuthUser user);
		protected abstract void onAfterAction(); //Controller must call this
		
		protected boolean isLoggedIn()
		{
			if (authUser == null)
			{
				baseReply.setDestination(Reply.FOWARD_NOT_AUTHENTICATED);
				return false;
			}
			return true;
		}
		
	}

	public static class MyReply extends Reply
	{
		String aaa;
		String fakeVM; //viewmodel
	}
	
	public static class MyPresenter extends XPresenter
	{
		private MyReply reply;
		
		public MyPresenter()
		{
			super();
			baseReply = reply = new MyReply();
		}

		public void index() 
		{
			reply.aaa = "abc";
			reply.setDestination(Reply.VIEW_INDEX);
			this.log("index..");
		}
		public void newItem()
		{
			reply.setDestination(Reply.VIEW_NEW);
		}
		public void flaky(boolean b)
		{
			if (! b)
			{
				reply.setFailed(true);
			}
			else
			{
				reply.setDestination(Reply.VIEW_NEW);
			}
		}

		@Override
		protected boolean onBeforeAction(AuthUser user) 
		{
//			return true; //true means continue. false means before-action has filled in a reply
			return this.isLoggedIn();
		}

		//only called if reply not failed and not forwarding
		@Override
		public void onAfterAction() 
		{
			reply.fakeVM = "bbb";
		}
		
	}

	@Test
	public void test() 
	{
		if (createPresenter())
		{
			presenter.index();
		}
		MyReply reply = getReply();
		assertEquals(Reply.VIEW_INDEX, reply.getDestination());
		assertEquals(false, reply.failed());
		assertEquals("bbb", reply.fakeVM);
	}
	@Test
	public void testNew() 
	{
		if (createPresenter())
		{
			presenter.newItem();
		}
		MyReply reply = getReply();
		assertEquals(Reply.VIEW_NEW, reply.getDestination());
		assertEquals(false, reply.failed());
		assertEquals("bbb", reply.fakeVM);
	}
	@Test
	public void testNewFailed() 
	{
		if (createPresenter(null)) //not logged in
		{
			presenter.newItem();
		}
		MyReply reply = getReply();
		assertEquals(Reply.FOWARD_NOT_AUTHENTICATED, reply.getDestination());
		assertEquals(false, reply.failed());
		assertEquals(null, reply.fakeVM);
	}
	@Test
	public void testFlaky() 
	{
		if (createPresenter())
		{
			presenter.flaky(false);
		}
		MyReply reply = getReply();
		assertEquals(true, reply.failed());
		assertEquals(0, reply.getDestination());
		assertEquals(null, reply.fakeVM);
	}

	
	//-----------------------------
	private SfxContext ctx;
	private AuthUser authUser;
	private MyPresenter presenter;
	private IAuthorizer authorizer;
	
	@Before
	public void init()
	{
		ctx = new SfxContext();
	}
	
	private boolean createPresenter()
	{
		TheGlobal.theCtx = ctx;
		
		//action composition would do this
		presenter = new MyPresenter();
		authUser = new AuthUser(); //simulate login
		presenter.setAuthorizer(authorizer); //somehow using DI
		boolean b = presenter.doBeforeAction(authUser);
		
		
		return b;
	}
	private boolean createPresenter(AuthUser user)
	{
		TheGlobal.theCtx = ctx;
		
		//action composition would do this
		presenter = new MyPresenter();
		authUser = user;
		presenter.setAuthorizer(authorizer); //somehow using DI
		boolean b = presenter.doBeforeAction(authUser);
		
		return b;
	}
	
	private MyReply getReply()
	{
		presenter.afterAction(); //Controller's render would call this
		return presenter.reply;
	}
}
