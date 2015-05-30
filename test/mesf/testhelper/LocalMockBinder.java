package mesf.testhelper;

import java.util.Map;

import mesf.presenter.IFormBinder;

import org.mef.twixt.ValueContainer;
import org.mef.twixt.binder.MockTwixtBinder;
import org.mef.twixt.binder.TwixtBinder;

import play.data.Form;


public class LocalMockBinder<T extends ValueContainer> implements IFormBinder<T>
{
	MockTwixtBinder<T> inner;
	
	public LocalMockBinder(Class<T> clazz, Map<String,String> anyData)
	{
		inner = new MockTwixtBinder<T>((Class<T>) clazz, anyData);
	}

	@Override
	public boolean bind() 
	{
		return inner.bind();
	}

	@Override
	public Object getValidationErrors() {
		return inner.getValidationErrors();
	}

	@Override
	public T get() {
		return inner.get();
	}

	@Override
	public Form<T> getForm() {
		return inner.getForm();
	}

	@Override
	public Form<T> fillForm(T input) 
	{
		return inner.fillForm(input);
	}

}
