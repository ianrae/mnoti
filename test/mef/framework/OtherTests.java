package mef.framework;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.auth2.AuthUser;
import org.mef.framework.replies.Reply;
import org.mef.framework.sfx.SfxBaseObj;
import org.mef.framework.sfx.SfxContext;

public class OtherTests 
{
	
	public static abstract class XPresenter extends SfxBaseObj 
	{
		protected AuthUser authUser;
		protected Reply baseReply;

		public XPresenter(SfxContext ctx)
		{
			super(ctx);
		}
		
		public Reply getBaseReply()
		{
			return baseReply;
		}
		
		public boolean doBeforeAction(AuthUser user)
		{
			this.authUser = user;
			return onBeforeAction(user);
		}
		
		protected abstract boolean onBeforeAction(AuthUser user);
		
	}

	public static class MyReply extends Reply
	{
		String aaa;
	}
	
	public static class MyPresenter extends XPresenter
	{
		private MyReply reply;
		
		public MyPresenter(SfxContext ctx)
		{
			super(ctx);
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
			return true; //true means continue. false means before-action has filled in a reply
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
	
	@Before
	public void init()
	{
		ctx = new SfxContext();
	}
	
	private boolean createPresenter()
	{
		presenter = new MyPresenter(ctx);
		return presenter.doBeforeAction(authUser);
	}
}
