import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


// class contains criterion name and values. E.g. 
//  {
//    "income": [
//      "affluent"
//    ],
//    "gender": [
//      "M"
//    ],
//    "content": [
//      "business",
//      "sport",
//      "news"
//    ]
//  }

@SuppressWarnings("serial")
public class Criteria extends HashMap<String, HashSet<String>>
{
	Criteria(String names[], Collection<? extends String> values[]) {
	    // Put elements to the map
		for (int i = 0; i < names.length; i++) {
			put(names[i], new HashSet<String>(values[i]));
		}
	 }

	public boolean containsAll(Criteria another) {
		// get this names
		Set<String> thisNames = keySet();
		Set<String> anotherNames = another.keySet();
		if (anotherNames.containsAll(thisNames))
		{
			// another Criteria contains all names of this one, so it is more (or the same) specific
		    // check elements
		    for (Object name: thisNames) {
		    	HashSet<String> anotherValues = another.get(name);
		    	if (!anotherValues.containsAll(get(name)))
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
