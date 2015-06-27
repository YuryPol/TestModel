import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */

/**
 * @author ypolyako
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
	
	private HashMap<String, ArrayList<String>> criteria;
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
	public Map<String, ArrayList<String>> getcriteria() {
		return criteria;
	}

	/**
	 * @param criteria the criteria to set
	 */
	public void setcriteria(HashMap<String, ArrayList<String>> criteria) {
		this.criteria = criteria;
	}

}
