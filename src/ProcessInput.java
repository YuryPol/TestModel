import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
 

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

			//convert json string to object
			InventroryData data= objectMapper.readValue(jsonData, InventroryData.class);
			
			String dataStr = data.toString();
			System.out.println(dataStr);
			
/*			BaseSetsChoices choices = objectMapper.readValue(jsonData, BaseSetsChoices.class);
*/		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
