import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Simulation {

	public static void main(String[] args) {
        Connection con = null;
        ResultSet rs = null;
        String url = "jdbc:mysql://localhost:3306/demo";
        String user = "root";
        String password = "IraAnna12";
        int increment = Integer.parseInt(args[0].trim());
        int served_count = 0;
        int missed_count = 0;
        
        System.out.println(
                new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
        Random rand= new Random();
        try {
            con = DriverManager.getConnection(url, user, password);
            
            // create result_serving table
            PreparedStatement createResultServingTable = con.prepareStatement("DROP TABLE IF EXISTS result_serving;");
            createResultServingTable.executeUpdate();
            createResultServingTable = con.prepareStatement("CREATE TABLE result_serving  ENGINE=MEMORY AS SELECT *, 0 AS served_count FROM structured_data_base;");
            createResultServingTable.executeUpdate();
            // process result_serving table
            PreparedStatement max_weightStatement = con.prepareStatement("select max(weight) from raw_inventory");
            rs = max_weightStatement.executeQuery();
            if (!rs.next())
            	return; // no raw data
            int max_weight = Math.abs(rs.getInt(1));
            PreparedStatement getRequest = con.prepareStatement("select basesets from raw_inventory where weight >= ? order by weight asc limit 1");
            PreparedStatement choseInventorySet = con.prepareStatement(
            		"select set_key_is, (goal-served_count)/goal as weight_now from result_serving "
            		+ "where goal > served_count and ( ? & set_key_is ) = set_key_is order by weight_now desc limit 1;");
            PreparedStatement incrementServedCount = con.prepareStatement("update result_serving set served_count = served_count + "
            		+ Integer.toString(increment) + " where set_key_is = ?");
            //
            // Process raw inventory
            //
            for(int i = 0; i < max_weight; i += increment) {
	            // create the request
	            getRequest.setInt(1, rand.nextInt(max_weight));
	            rs = getRequest.executeQuery();
	            if (!rs.next()) // should not happen
	            	continue;
	            long basesets = rs.getLong(1);
	            // select inventory set to serve
	            choseInventorySet.setLong(1, basesets);
	            rs = choseInventorySet.executeQuery();
	            long set_key_is = 0;
	            if (rs.next())
	            {
	            	// not all matching inventory sets were served up
	            	// otherwise record the miss
	            	set_key_is = rs.getLong(1);
	            	incrementServedCount.setLong(1, set_key_is);
	            	int result = incrementServedCount.executeUpdate();
	            	served_count += increment;
	            }
	            else
	            	missed_count++;
	            // increment served_count in result_serving
	            if (served_count != 0 && served_count % 10000 == 0)
	            {
	                System.out.print(
	                        new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
	                System.out.print(" : served_count=" +  String.valueOf(served_count) + ", missed_count=" 
	                        + String.valueOf(missed_count) + "\r");            	
	            }
            }
            System.out.print(
                    new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
            System.out.println(" : served_count=" +  String.valueOf(served_count) + ", missed_count=" + String.valueOf(missed_count));            	
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Simulation.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                 if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(Simulation.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }

	}

}
