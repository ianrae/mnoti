package mef.framework.helpers;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.mef.framework.sfx.SfxContext;

public class BaseTest
{
	
	//-----------------------------
	protected SfxContext ctx;
	
	public void init()
	{
		ctx = new SfxContext();
	}
	
	protected void log(String s)
	{
		ctx.log(s);
	}
	
	
	protected void chkLong(Long expected, Long actual)
	{
		assertEquals(expected, actual);
	}
	
}
