import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
 

public class ProcessInput {

	public static void main(String[] args) 
	{
		byte[] jsonData = null;
		String test;
		try {
			jsonData = Files.readAllBytes(Paths.get("C:/Users/ypolyako/Downloads/inventory.json"));
			test = new String(jsonData);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		try {

			//create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			//convert json input to object
			InventroryData inventorydata= objectMapper.readValue(jsonData, InventroryData.class);

			System.out.println(inventorydata.toString());

			Set<criteria> criteria_sets = new HashSet<criteria>();

			// Create filter for criteria used in inventory sets
			for (inventoryset is : inventorydata.getInventorysets())
			{
				criteria_sets.add(is.getcriteria());
			}
			
			System.out.println(criteria_sets.toString());
			
			// Filter out segments and create raw inventory
			
			HashMap<BitSet, BaseSet> base_sets = new HashMap<BitSet, BaseSet>();
			
			// Create inventory sets data
			int index = 0;
			for (inventoryset is : inventorydata.getInventorysets())
			{
				index++;
				BaseSet tmp = new BaseSet();
				tmp.setkey(index);
				tmp.setname(is.getName());
				tmp.setCriteria(is.getcriteria());
				base_sets.put(tmp.getkey(), tmp);
			}
			
			// account for inclusion 
			for (BaseSet bs : base_sets.values())
			{
				for (BaseSet bs1 : base_sets.values())
				{
					if (bs.getkey() == bs1.getkey())
						continue;
						
					if (bs.getCriteria().matches(bs1.getCriteria()))
					{
						bs.getkey().or(bs1.getkey());
					}
				}
			}
			
			
			// Write raw inventory into DB
	        Connection con = null;
	        Statement st = null;
	        PreparedStatement insertStatement = null;
	        String url = "jdbc:mysql://localhost:3306/test_fia";
	        String user = "root";
	        String password = "password";
			

			System.out.println(base_sets.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
