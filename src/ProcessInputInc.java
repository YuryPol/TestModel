import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Yury
 * processes JSON input
 */
public class ProcessInputInc {

	static int BITMAP_SIZE = 64;
	static boolean DEBUG = false;

	public static void main(String[] args) 
	{
        System.out.println(
                new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
		byte[] jsonData = null;
		try {
			jsonData = Files.readAllBytes(Paths.get(args[0]
					// "C:/Users/Yury/Documents/GitHub/TestModel/Input/document.json"
					));
		} catch (NoSuchFileException e0) {
			try {
				jsonData = Files.readAllBytes(Paths.get(args[0]
						// "C:/Users/ypolyako/workspace/TestModel/Input/document.json"
						));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
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
				boolean match_found = false;
				for (BaseSet bs1 : base_sets.values())
				{
					if (bs1.getCriteria().equals(is.getcriteria()))
					{
						match_found = true;
						break;
					}
				}
				if (match_found)
					continue; // skip repeated set
				
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
			
			// Create segments' raw data. TODO: write into DB from the start
			// TODO: test it, produces too few records
			HashMap<BitSet, BaseSegement> base_segments = new HashMap<BitSet, BaseSegement>();
			for (segment seg : inventorydata.getSegments())
			{
				boolean match_found = false;
				BaseSegement tmp = new BaseSegement(BITMAP_SIZE);
				tmp.setCriteria(seg.getcriteria());
				
				for (BaseSet bs1 : base_sets.values())
				{					
					if (bs1.getCriteria().matches(tmp.getCriteria()))
					{
						tmp.getkey().or(bs1.getkey());
						match_found = true;

/*						int old_capacity = 0;
						if (base_segments.get(tmp.getkey()) != null)
						{
							old_capacity = tmp.getcapacity();
						}
						tmp.setcapacity(old_capacity + seg.getCount());
*/						
					}
				}
				if (match_found) 
				{
					int capacity = seg.getCount();
					BaseSegement existing = null;
					if ((existing = base_segments.get(tmp.getkey())) != null)
					{
						capacity += existing.getcapacity();
					}
					tmp.setcapacity(capacity);
					base_segments.put(tmp.getkey(), tmp);
				}
				if (DEBUG & tmp.getcapacity() == 0)
				{
					// TODO: remove later, for debugging only
					System.out.println("segment criteria: " + tmp.getCriteria().toString());
				}
			}			
			if (DEBUG) for (BaseSet bs1 : base_sets.values())
			{
				// TODO: remove later, for debugging only
				System.out.println("BaseSet criteria: " + bs1.getCriteria().toString());				
			}

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
	            System.out.println(
	                    new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
	            
	            con = DriverManager.getConnection(url, user, password);
	            
	            // clear everything
	            st = con.createStatement();
	            st.executeUpdate("TRUNCATE raw_inventory"); 
	            st.executeUpdate("TRUNCATE structured_data_base"); 
	            
	            // populate structured data with inventory sets
	            insertStatement = con.prepareStatement
	            		("INSERT IGNORE INTO structured_data_base SET set_key = ?, set_name = ?, set_key_is = ?, criteria = ?");
	            for (BaseSet bs1 : base_sets.values()) {
	            	// insertStatement.setBytes(1, bs1.getKeyVarBin());
	            	insertStatement.setLong(1, bs1.getKeyBin()[0]);
		            insertStatement.setString(2, bs1.getname());
	            	insertStatement.setLong(3, bs1.getKeyBin()[0]);
	            	insertStatement.setString(4, bs1.getCriteria().toString());
		            insertStatement.execute();
	            }
	            
	            System.out.println(
	                    new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
	            
	            // populate raw data with inventory sets' bitmaps
	            insertStatement = con.prepareStatement
	            		("INSERT INTO raw_inventory SET basesets = ?, count = ?, criteria = ? ON DUPLICATE KEY UPDATE count = VALUES(count) + count");
	            for (BaseSegement bs1 : base_segments.values()) {
	            	insertStatement.setLong(1, bs1.getKeyBin()[0]);
	            	insertStatement.setInt(2, bs1.getcapacity());
	            	insertStatement.setString(3, bs1.getCriteria().toString());
		            insertStatement.execute();
	            }
	            
	            System.out.println(
	                    new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
	            System.out.println("Finished");

	        } catch (SQLException ex) {
	            Logger lgr = Logger.getLogger(ProcessInputInc.class.getName());
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
	                Logger lgr = Logger.getLogger(ProcessInputInc.class.getName());
	                lgr.log(Level.WARNING, ex.getMessage(), ex);
	            }
	        }
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
