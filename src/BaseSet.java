
public class BaseSet {
	private Criteria criteria = null;
	private int availablity = 0;
	private long unionOf = 0;
	
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
