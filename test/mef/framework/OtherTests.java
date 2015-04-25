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
//	//so the action composition just do to Object.createInstance and call parameterless ctor
//	public static class TheGlobal
//	{
//		public static SfxContext theCtx;
//	}
	
	public static abstract class XPresenter extends SfxBaseObj 
	{
		protected AuthUser authUser;
		protected IAuthorizer auth;
		protected Reply baseReply;

		public XPresenter(SfxContext ctx, IAuthorizer auth)
		{
			super(ctx);
			this.auth = auth;
		}
		
		public Reply getBaseReply()
		{
			return baseReply;
		}
		
		public boolean doBeforeAction(AuthUser authUser)
		{
			this.authUser = authUser;
			return onBeforeAction();
		}
		
		public void afterAction() //Controller must call this
		{
//			if (baseReply == null)
//			{
//				return;
//			}

			this.onAfterAction();
			
			boolean genViewModel = (! baseReply.failed() && ! baseReply.isForward());
			if (genViewModel)
			{
				generateViewModel();
			}
		}
		
		protected abstract boolean onBeforeAction();
		protected abstract void onAfterAction(); 
		protected abstract void generateViewModel();
		
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

	public interface IFooDAO
	{
		int size();
	}
	
	public static class MockFooDAO implements IFooDAO
	{

		@Override
		public int size() {
			return 0;
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
		private IFooDAO dao;
		
		public MyPresenter(SfxContext ctx, IAuthorizer auth, IFooDAO dao)
		{
			super(ctx, auth);
			baseReply = reply = new MyReply();
			this.dao = dao;
		}

		public void index() 
		{
			dao.size(); //...
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
		protected boolean onBeforeAction() 
		{
//			return true; //true means continue. false means before-action has filled in a reply
			return this.isLoggedIn();
		}

		//always called
		@Override
		public void onAfterAction() 
		{
		}

		//only called if not failed and not forwarded
		@Override
		protected void generateViewModel() 
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
		return createPresenter(new AuthUser());
	}
	private boolean createPresenter(AuthUser user)
	{
		//action composition would do this
		presenter = new MyPresenter(ctx, authorizer, new MockFooDAO());
		authUser = user;
		boolean b = presenter.doBeforeAction(authUser);
		
		return b;
	}
	
	/* so the controller would do
	 *  
	 *  Result index()
	 *    if (createPresenter()) //create, DI, and beforeAction
	 *    {
	 *       presenter.index();
	 *    }
	 *    renderOrForward(); //does presenter.afterAction then render
	 */
	
	private MyReply getReply()
	{
		presenter.afterAction(); //Controller's render would call this
		return presenter.reply;
	}
}
