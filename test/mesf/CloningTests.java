package mesf;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mesf.TaskTests.Task;
import mesf.UserTests.User;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rits.cloning.Cloner;

public class CloningTests extends BaseMesfTest
{
	@Test
	public void test() 
	{
		Cloner cloner=new Cloner();
		
		User u = new User();
		u.setA(10);
		u.setB(65570);
		u.setS("abc");
		u.setId(4L);
		
		User clone=cloner.deepClone(u);
		assertEquals(4L, clone.getId().longValue());
		assertEquals(10, clone.getA());
		assertEquals(65570, clone.getB());
		assertEquals("abc", clone.getS());
		
		Task task = new Task();
	}

}
