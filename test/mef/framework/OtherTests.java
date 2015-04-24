package mef.framework;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mef.framework.replies.Reply;
import org.mef.framework.sfx.SfxBaseObj;
import org.mef.framework.sfx.SfxContext;

public class OtherTests 
{
	
	public static class XPresenter extends SfxBaseObj 
	{
		
		public XPresenter(SfxContext ctx)
		{
			super(ctx);
		}
		
	}

	public static class MyReply extends Reply
	{
		String aaa;
	}
	
	public static class MyPresenter extends XPresenter
	{
		
		public MyPresenter(SfxContext ctx)
		{
			super(ctx);
		}
		
		protected MyReply createReply()
		{
			return new MyReply();
		}

		public MyReply index() 
		{
			MyReply reply = createReply();
			reply.aaa = "abc";
			reply.setDestination(Reply.VIEW_INDEX);
			return reply;
		}
		public MyReply newItem()
		{
			MyReply reply = createReply();
			reply.setDestination(Reply.VIEW_NEW);
			return reply;
		}
		
	}

	@Test
	public void test() 
	{
		MyPresenter pres = new MyPresenter(ctx);
		
		MyReply reply = pres.index();
		assertEquals(Reply.VIEW_INDEX, reply.getDestination());
	}
	@Test
	public void testNew() 
	{
		MyPresenter pres = new MyPresenter(ctx);
		
		MyReply reply = pres.newItem();
		assertEquals(Reply.VIEW_NEW, reply.getDestination());
	}

	
	private SfxContext ctx;
	
	@Before
	public void init()
	{
		ctx = new SfxContext();
	}
}
