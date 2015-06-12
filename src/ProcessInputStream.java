
	import java.io.File;
	import java.io.IOException;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	 
	import com.fasterxml.jackson.core.JsonFactory;
	import com.fasterxml.jackson.core.JsonParseException;
	import com.fasterxml.jackson.core.JsonParser;
	import com.fasterxml.jackson.core.JsonToken;
	 
	public class ProcessInputStream {
	 
	    public static void main(String[] args) throws JsonParseException, IOException {
	         
	        //create JsonParser object
	        JsonParser jsonParser = new JsonFactory().createParser(new File(args[0]));
	        
	        // Build Criteria list ordered by number of unique attributes and number of attribute's values (most specific to least specific)
	        
	        // Set bitmaps in Criteria list
	        
	        // Build sets using Criteria list and store them in DB
	        
	        //loop through the tokens
	         
	        if (getInputType(jsonParser) == "inventorysets")
	        {
	        	InventorySet invset = new InventorySet();
	        	parseInventorySet(jsonParser, invset);
		        System.out.println("Employee Object\n\n"+invset);
	        }
	         
	        jsonParser.close();
	    }
	    
	    private static String getInputType(JsonParser jsonParser) throws JsonParseException, IOException
	    {
	    	if (jsonParser.nextToken() != JsonToken.END_OBJECT){
	    		String name = jsonParser.getCurrentName();
	    		return name;
	    	}
	    	else
	    		return null;
	    }
	    
/*	    object		{2}
		
	    name	:	inventorysets
	    	    		
    	inventorysets		[7]	    		
	    	0		{1}	    		
	    	creteria		{4}
			state	:	NY					
			income	:	affluent					
			gender	:	M
		*/
	 
	    private static void parseInventorySet(JsonParser jsonParser, InventorySet invset) throws JsonParseException, IOException 
	    {	         
	        JsonToken current;
	        current = jsonParser.nextToken();
	        if (current != JsonToken.START_OBJECT) {
	          System.out.println("Error: root should be object: quiting.");
	          return;
	        }
	        //loop through the JsonTokens
	        while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	            if (current == JsonToken.START_ARRAY) {
	            	// keep going
	            	continue;
	            }
                // For each of the records in the array
		        if (jsonParser.nextToken() != JsonToken.START_OBJECT){
                	invset = null;
		        	return;
		        }
		        while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	            
                if (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                	// ended
                	invset = null;
                	return;
	            }
	        }
	    }
	            
	            
	    private static void parseJSON(JsonParser jsonParser, Employee emp,
	            List<Long> phoneNums, boolean insidePropertiesObj) throws JsonParseException, IOException {
	         
	        //loop through the JsonTokens
	        while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	            String name = jsonParser.getCurrentName();
	            if("id".equals(name)){
	                jsonParser.nextToken();
	                emp.setId(jsonParser.getIntValue());
	            }else if("name".equals(name)){
	                jsonParser.nextToken();
	                emp.setName(jsonParser.getText());
	            }else if("permanent".equals(name)){
	                jsonParser.nextToken();
	                emp.setPermanent(jsonParser.getBooleanValue());
	            }else if("address".equals(name)){
	                jsonParser.nextToken();
	                //nested object, recursive call
	                parseJSON(jsonParser, emp, phoneNums, insidePropertiesObj);
	            }else if("street".equals(name)){
	                jsonParser.nextToken();
	                emp.getAddress().setStreet(jsonParser.getText());
	            }else if("city".equals(name)){
	                jsonParser.nextToken();
	                emp.getAddress().setCity(jsonParser.getText());
	            }else if("zipcode".equals(name)){
	                jsonParser.nextToken();
	                emp.getAddress().setZipcode(jsonParser.getIntValue());
	            }else if("phoneNumbers".equals(name)){
	                jsonParser.nextToken();
	                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
	                    phoneNums.add(jsonParser.getLongValue());
	                }
	            }else if("role".equals(name)){
	                jsonParser.nextToken();
	                emp.setRole(jsonParser.getText());
	            }else if("cities".equals(name)){
	                jsonParser.nextToken();
	                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
	                    emp.getCities().add(jsonParser.getText());
	                }
	            }else if("properties".equals(name)){
	                jsonParser.nextToken();
	                while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	                    String key = jsonParser.getCurrentName();
	                    jsonParser.nextToken();
	                    String value = jsonParser.getText();
	                    emp.getProperties().put(key, value);
	                }
	            }
	        }
	    }
	 
	}
