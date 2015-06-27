
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
	private Criteria criteria;
	private int availablity = 0;
	private int goal;
	private long unionOf = 0;
	
	public String getname() {
		return name;
	};
	public void setname(String nm) {
		name = nm;
	};
	
	public Criteria getCriteria() {
		return criteria;
	};
	public void setCriteria(Criteria crt) {
		criteria = crt;
	};
	
	public int getgoaly() {
		return goal;
	};
	public void setgoal(int gl) {
		goal = gl;
	};
	
	boolean matches(Criteria crt)
	{
		return criteria.containsAll(crt);
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
