import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
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
	
	static int BITMAP_SIZE = 64;

	public static void main(String[] args) 
	{
		byte[] jsonData = null;
		String test;
		try {
			jsonData = Files.readAllBytes(Paths.get("C:/Users/ypolyako/Downloads/Inventory (3).json"));
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

			// Create filter for criteria used in inventory sets for debugging. TODO: remove
			Set<criteria> criteria_sets = new HashSet<criteria>();
			for (inventoryset is : inventorydata.getInventorysets())
			{
				criteria_sets.add(is.getcriteria());
			}			
			System.out.println(criteria_sets.toString());
			
			// Create inventory sets data. TODO: write into DB from the start
			HashMap<BitSet, BaseSet> base_sets = new HashMap<BitSet, BaseSet>();			
			int highBit = 0;
			for (inventoryset is : inventorydata.getInventorysets())
			{
				BaseSet tmp = new BaseSet(BITMAP_SIZE);
				tmp.setkey(highBit);
				tmp.setname(is.getName());
				tmp.setCriteria(is.getcriteria());
				base_sets.put(tmp.getkey(), tmp);
				highBit++;
			}
			if (highBit == 0)
			{
				System.out.println("no data in inventory sets");
				return;
			}			
			// account for inclusion 
			for (BaseSet bs : base_sets.values())
			{
				for (BaseSet bs1 : base_sets.values())
				{					
					if (bs.contains(bs1.getCriteria()))
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
				BaseSegement tmp = new BaseSegement(BITMAP_SIZE);
				tmp.setCriteria(seg.getcriteria());
				
				for (BaseSet bs1 : base_sets.values())
				{					
					if (bs1.getCriteria().matches(tmp.getCriteria()))
					{
						tmp.getkey().or(bs1.getkey());

						int old_capacity = 0;
						if (base_segments.get(tmp.getkey()) != null)
						{
							old_capacity = tmp.getcapacity();
						}
						tmp.setcapacity(old_capacity + seg.getCount());
						base_segments.put(tmp.getkey(), tmp);
					}
				}
			}			
			System.out.println(base_segments.values().toArray().toString());

			//
			// Write inventories into DB
			//
	        Connection con = null;
	        Statement st = null;
	        CallableStatement cs = null;
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
	            st.executeUpdate("DELETE FROM basestructdata"); 
	            
	            // populate structured data with inventory sets
	            insertStatement = con.prepareStatement
	            		("INSERT IGNORE INTO structured_data SET set_key = ?, set_name = ?");
	            for (BaseSet bs1 : base_sets.values()) {
	            	// insertStatement.setBytes(1, bs1.getKeyVarBin());
	            	insertStatement.setLong(1, bs1.getKeyBin()[0]);
		            insertStatement.setString(2, bs1.getname());
		            insertStatement.execute();
	            }	            
	            
	            // populate inventory sets and unions template
	            cs = con.prepareCall("{call CreateFullStructData(?)}");
	            Integer Index = highBit;
	            cs.setString(1, Index.toString());
	            cs.executeQuery();

	            // add unions to structured data
	            cs = con.prepareCall("{call AddUnionsToStructData}");
	            cs.executeQuery();
	            
	            // populate raw data with inventory sets' bitmaps
	            insertStatement = con.prepareStatement
	            		("INSERT IGNORE INTO raw_inventory SET basesets = ?, count = ?");
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
	