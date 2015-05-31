package mesf.entity;

import com.rits.cloning.Cloner;

//clone automatically using Cloner

public class BaseEntity extends Entity
{
	@Override
	public Entity clone()
	{
		Cloner cloner=new Cloner();
//		cloner.setDumpClonedClasses(true);
		Entity clone=cloner.deepClone(this);
		return clone;
	}
	
}