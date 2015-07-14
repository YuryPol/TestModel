
//    {
//      "name": "highrollers",
//      "creteria": {
//        "state": [
//          "NY",
//          "NJ"
//        ],
//        "income": [
//          "affluent",
//          "middle"
//        ],
//        "gender": [
//          "M"
//        ],
//        "content": [
//          "business",
//          "sport",
//          "food",
//          "news"
//        ],
//        "age": [
//          "middle",
//          "young",
//          "child"
//        ]
//      },
//      "goal": 150000
//    }
    
public class BaseSet {
	private String name;
	private criteria this_criteria;
	private int availablity = 0;
	private int goal;
	private long unionOf = 0;
	
	public String getname() {
		return name;
	};
	public void setname(String nm) {
		name = nm;
	};
	
	public criteria getCriteria() {
		return this_criteria;
	};
	public void setCriteria(criteria crt) {
		this_criteria = crt;
	};
	
	public int getgoal() {
		return goal;
	};
	public void setgoal(int gl) {
		goal = gl;
	};
	
	boolean matches(criteria that_criteria)
	{
		return this_criteria.containsAll(that_criteria);
	}
	
	boolean contains(BaseSet another)
	{
		return ((unionOf & another.unionOf) == another.unionOf);
	}
	
	boolean modifyAvailablity(int change)
	{
		if (availablity + change >= 0)
		{
			availablity += change;
			return true;
		}
		else return false;
	}
	
	void addMember(long memberBitmap)
	{
		unionOf |= memberBitmap;
	}

}
