package mesf.entity;

import com.rits.cloning.Cloner;

public class BaseEntity extends Entity
{
	@Override
	public Entity clone()
	{
		Cloner cloner=new Cloner();
		Entity clone=cloner.deepClone(this);
		return clone;
	}
	
}