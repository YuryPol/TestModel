import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 

public class ProcessInput {

    public static void some(String[] args) throws JsonParseException, IOException {
        
        //read json file data to String
        byte[] jsonData = Files.readAllBytes(Paths.get("Criteria.txt"));
         
        //create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
         
        //convert json string to object
        Criteria emp = objectMapper.readValue(jsonData, Criteria.class);
         
        System.out.println("Criteria Object\n"+emp);
    }

	public static void main(String[] args) {
		String folderPath = "C:/folderOfMyFile";
		Path path = Paths.get(folderPath, "myFileName.csv"); // or any text file

		Charset charset = Charset.forName("UTF-8");

		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line;
			while ((line = reader.readLine()) != null) {
				JSONParser parser = new JSONParser();
				Object obj = parser..parse(line);
				JSONObject jsonObject = (JSONObject) obj;

				String type = (String) jsonObject.get("type");
				if (type == "inventoryset") {
					// parse sets
					JSONArray criteriaJA = (JSONArray) jsonObject.get("criteria");
					Iterator<JsonNode> iterator = criteriaJA.iterator();
					Criteria criteria = new Criteria();
					while (iterator.hasNext()) {
						criteria.set(iterator.next().get)
					}
				}
				else if (type == "segments") {
					// parse raw data
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
