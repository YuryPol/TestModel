import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
 

public class ProcessInput {
	
	static int BITMAP_ZIZE = 64;

	public static void main(String[] args) 
	{
		byte[] jsonData = null;
		String test;
		try {
			jsonData = Files.readAllBytes(Paths.get("C:/Users/ypolyako/Downloads/Inventory (2).json"));
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

			// Create filter for criteria used in inventory sets for debugging. TODO: remove
			for (inventoryset is : inventorydata.getInventorysets())
			{
				criteria_sets.add(is.getcriteria());
			}
			
			System.out.println(criteria_sets.toString());
			
			// Filter out segments and create raw inventory
			
			HashMap<BitSet, BaseSet> base_sets = new HashMap<BitSet, BaseSet>();
			
			// Create inventory sets data. TODO: write into DB from the start
			int index = 0;
			for (inventoryset is : inventorydata.getInventorysets())
			{
				BaseSet tmp = new BaseSet(BITMAP_ZIZE);
				tmp.setkey(index);
				tmp.setname(is.getName());
				tmp.setCriteria(is.getcriteria());
				base_sets.put(tmp.getkey(), tmp);
				index++;
			}
			
			// account for inclusion 
			for (BaseSet bs : base_sets.values())
			{
				for (BaseSet bs1 : base_sets.values())
				{					
					if (bs.getCriteria().matches(bs1.getCriteria()))
					{
						bs.getkey().or(bs1.getkey());
					}
				}
			}
			
			System.out.println(base_sets.toString());
			
			// Create segments' raw data. TODO: write into DB from the start
			HashMap<BitSet, BaseSegement> base_segments = new HashMap<BitSet, BaseSegement>();

			for (segment seg : inventorydata.getSegments())
			{
				BaseSegement tmp = new BaseSegement(BITMAP_ZIZE);
				tmp.setCriteria(seg.getcriteria());
				
				for (BaseSet bs1 : base_sets.values())
				{					
					if (tmp.getCriteria().matches(bs1.getCriteria()))
					{
						tmp.getkey().or(bs1.getkey());
						tmp.setcapacity(seg.getCount());
						base_segments.put(tmp.getkey(), tmp);
					}
				}
			}
			
			System.out.println(base_segments.toString());

			// Write inventories into DB
	        Connection con = null;
	        Statement st = null;
	        PreparedStatement insertStatement = null;
	        String url = "jdbc:mysql://localhost:3306/demo";
	        String user = "root";
	        String password = "IraAnna12";
			
	        try {
	            con = DriverManager.getConnection(url, user, password);
	            
	            // clear everything
	            st = con.createStatement();
	            st.executeUpdate("DELETE FROM raw_inventory"); 
	            st.executeUpdate("DELETE FROM structured_data"); 
	            
	            // populate tables
	            
	            // structured data with inventory sets
	            insertStatement = con.prepareStatement
	            		("INSERT IGNORE INTO structured_data SET set_key = ?, set_name = ?");
	            for (BaseSet bs1 : base_sets.values()) {
	            	// insertStatement.setBytes(1, bs1.getKeyVarBin());
	            	insertStatement.setLong(1, bs1.getKeyBin()[0]);
		            insertStatement.setString(2, bs1.getname());
		            insertStatement.execute();
	            }
	            
	            // raw data with inventory sets' bitmaps
	            insertStatement = con.prepareStatement
	            		("INSERT IGNORE INTO raw_inventory SET set_key = ?, capacity = ?");
	            for (BaseSegement bs1 : base_segments.values()) {
	            	insertStatement.setLong(1, bs1.getKeyBin()[0]);
	            	insertStatement.setInt(2, bs1.getcapacity());
		            insertStatement.execute();
	            }
	        } catch (SQLException ex) {
	            Logger lgr = Logger.getLogger(GenInput.class.getName());
	            lgr.log(Level.SEVERE, ex.getMessage(), ex);

	        } finally {
	            try {
	                if (st != null) {
	                    st.close();
	                }
	                if (insertStatement != null) {
	                	insertStatement.close();
	                }
	                if (con != null) {
	                    con.close();
	                }
	            } catch (SQLException ex) {
	                Logger lgr = Logger.getLogger(GenInput.class.getName());
	                lgr.log(Level.WARNING, ex.getMessage(), ex);
	            }
	        }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
	