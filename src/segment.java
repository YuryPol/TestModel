import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 
 */

/**
 * @author Yury
 *
 */
public class segment implements Serializable {

	/**
	      "criteria": {
	        "state": [
	          "CA"
	        ],
	        "content": [
	          "sport",
	          "food"
	        ],
	        "age": [
	          "young"
	        ]
	      	},
		    "count": 80000
	 */
	
	private criteria this_criteria;
	private int count;
	
	public segment() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the criteria
	 */
	public criteria getcriteria() {
		return this_criteria;
	}

	/**
	 * @param that_criteria the criteria to set
	 */
	public void setcriteria(criteria that_criteria) {
		this.this_criteria = that_criteria;
	}

}
