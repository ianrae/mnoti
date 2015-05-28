package mesf;

import mef.framework.helpers.BaseTest;
import mesf.log.LogLevel;
import mesf.log.Logger;


public class BaseMesfTest extends BaseTest
{
	@Override
	public void init()
	{
		super.init();
		Logger.getLogger().setLevel(LogLevel.DEBUG);
	}

}
