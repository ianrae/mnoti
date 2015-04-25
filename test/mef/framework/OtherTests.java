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
		
		protected abstract boolean onBeforeAction(AuthUser user);
		
		protected boolean isLoggedIn()
		{
			if (authUser == null)
			{
				return false;
			}
			return true;
		}
		
	}

	public static class MyReply extends Reply
	{
		String aaa;
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

		@Override
		protected boolean onBeforeAction(AuthUser user) 
		{
//			return true; //true means continue. false means before-action has filled in a reply
			return this.isLoggedIn();
		}
		
	}

	@Test
	public void test() 
	{
		if (createPresenter())
		{
			presenter.index();
		}
		MyReply reply = presenter.reply;
		assertEquals(Reply.VIEW_INDEX, reply.getDestination());
		assertEquals(false, reply.failed());
	}
	@Test
	public void testNew() 
	{
		if (createPresenter())
		{
			presenter.newItem();
		}
		MyReply reply = presenter.reply;
		assertEquals(Reply.VIEW_NEW, reply.getDestination());
		assertEquals(false, reply.failed());
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
}
