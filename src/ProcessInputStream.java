
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
	        	String invset;
	        	parseInventorySet(jsonParser);
		        System.out.println("Employee Object\n\n");
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
	 
	    private static void parseInventorySet(JsonParser jsonParser) throws JsonParseException, IOException 
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
 		        	return;
		        }
		        while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	            
                if (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                	return;
	            }
	        }
	        }
	    }
	            
	            
	    private static void parseJSON(JsonParser jsonParser,
	            List<Long> phoneNums, boolean insidePropertiesObj) throws JsonParseException, IOException {
	         
	        //loop through the JsonTokens
	        while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	            String name = jsonParser.getCurrentName();
	            if("id".equals(name)){
	                jsonParser.nextToken();
	            }else if("name".equals(name)){
	                jsonParser.nextToken();
	                jsonParser.getText();
	            }else if("properties".equals(name)){
	                jsonParser.nextToken();
	                while(jsonParser.nextToken() != JsonToken.END_OBJECT){
	                    String key = jsonParser.getCurrentName();
	                    jsonParser.nextToken();
	                    String value = jsonParser.getText();
	                }
	            }
	        }
	    }
	 
	}
