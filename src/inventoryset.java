import java.io.Serializable;
import java.util.Map;

/**
 * 
 */

/**
 * @author ypolyako
 *
 */
public class inventoryset implements Serializable {

	/**
	      {
	      "name": "highrollers",
	      "criteria": {
	        "state": [
	          "NY",
	          "NJ"
	        ],
	        "income": [
	          "affluent",
	          "middle"
	        ],
	        "gender": [
	          "M"
	        ],
	        "content": [
	          "business",
	          "sport",
	          "food",
	          "news"
	        ],
	        "age": [
	          "middle",
	          "young",
	          "child"
	        ]
	      },
	      "goal": 150000
	 * 
	 */
	
	private String name;
	private Map<String, String[]> criteria;
    private int goal;

	public inventoryset() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the criteria
	 */
	public Map<String, String[]> getcriteria() {
		return criteria;
	}

	/**
	 * @param criteria the criteria to set
	 */
	public void setcriteria(Map<String, String[]> criteria) {
		this.criteria = criteria;
	}

	/**
	 * @return the goal
	 */
	public int getGoal() {
		return goal;
	}

	/**
	 * @param goal the goal to set
	 */
	public void setGoal(int goal) {
		this.goal = goal;
	}

}
