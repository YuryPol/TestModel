import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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

			Set<criteria> criteria_set = new HashSet<criteria>();

			// Create filter for criteria used in inventory sets
			for (inventoryset is : inventorydata.getInventorysets())
			{
				criteria_set.add(is.getcriteria());
			}
			
			System.out.println(criteria_set.toString());
			
			// Filter out segments and create raw inventory
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
