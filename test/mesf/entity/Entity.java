package mesf.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonFilter("myFilter")
public abstract class Entity
{
	protected Set<String> setlist = new HashSet<String>();

	@JsonIgnore
	public List<String> getSetList()
	{
		List<String> L = new ArrayList<>();
		for(String s : setlist)
		{
			L.add(s);
		}
		return L;
	}
	public void clearSetList()
	{
		setlist.clear();
	}
	
	private Long id;

	@JsonIgnore
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	
	public abstract Entity clone();
	
}