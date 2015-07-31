import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 */

/**
 * @author "Yury"
 *
 */
public class criteria extends HashMap<String, HashSet<String>> implements Serializable 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2146129670317599931L;

	/**
	 * 
	 */
	public criteria() {
		// TODO Auto-generated constructor stub
	}

	criteria(inventoryset is)
	{
		putAll(is.getcriteria());
	}

	public boolean matches(criteria another) {
		// get this names
		Set<String> thisNames = keySet();
		Set<String> anotherNames = another.keySet();
		if (anotherNames.containsAll(thisNames))
		{
			// another Criteria contains all names of this one, so it is more (or the same) specific
		    // check elements
		    for (String name: thisNames) {
		    	HashSet<String> anotherValues = another.get(name);
		    	HashSet<String> thisValues = get(name);
		    	if (Collections.disjoint(thisValues, anotherValues))
		    		// criteron's values are OR-ed with each other
		    		return false;
		    }
			return true;
		}
		else
			// it defies the common sense as each criterion AND-ed with others 
			// fewer selection criteria means wider set
			return false;
	}
}
