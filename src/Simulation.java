import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Simulation {

	public static void main(String[] args) {
        Connection con = null;
        ResultSet rs = null;
        ResultSet rs1 = null;
        String url = "jdbc:mysql://localhost:3306/demo";
        String user = "root";
        String password = "IraAnna12";
        
        Random rand= new Random();
        try {
            con = DriverManager.getConnection(url, user, password);
            
            // clear the results
            PreparedStatement clearResultStatement = con.prepareStatement("delete from result_serving");
            clearResultStatement.execute();
            PreparedStatement clearMissesStatement =con.prepareStatement("delete from raw_inventory_used");
            clearMissesStatement.executeQuery();
            
            // read result_serving table
            PreparedStatement max_weightStatement = con.prepareStatement("select max(weight) from result_serving");
            rs = max_weightStatement.executeQuery();
            if (!rs.next())
            	return; // no raw data
            int max_weight = Math.abs(rs.getInt(1));
            PreparedStatement getRequest = con.prepareStatement("select basesets from raw_inventory where weight >= ? order by weight asc limit 1");
            PreparedStatement choseInventorySet = con.prepareStatement(
            		"select set_key_is, (goal-served_count)/goal as weight_now from result_serving "
            		+ "where goal > served_count and ( ? | set_key_is ) = set_key_is order by weight_now desc limit 1;");
            PreparedStatement incrementServedCount = con.prepareStatement("update result_serving set served_count = served_count + 1 where set_key_is = ?");
//            UPDATE relation 
//            SET name1 = CASE WHEN userid1 = 3 THEN 'jack' ELSE name1 END,
//                name2 = CASE WHEN userid2 = 3 THEN 'jack' ELSE name2 END
//            WHERE (userid1 = 3 AND userid2 = 4) 
//            OR (userid1 = 4 AND userid2 = 3);
            PreparedStatement changeRawServedCount = con.prepareStatement(
            		"update raw_inventory_used set "
            		+ " served_count case when ? & ? != 0 then served_count + 1 else served_count,"
            		+ " missed_count case when ? & ? == 0 then missed_count + 1 else missed_count end;");
            //
            // Process raw inventory
            //
            for(int i = 0; i < max_weight; i++) {
	            // create the request
	            getRequest.setInt(1, rand.nextInt(max_weight));
	            rs = getRequest.executeQuery();
	            long basesets = rs.getInt(1);
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
	            	rs = incrementServedCount.executeQuery();
	            }
	            // increment served_count in result_serving
	            changeRawServedCount.setLong(1, basesets);	            
	            changeRawServedCount.setLong(2, set_key_is);
	            rs = changeRawServedCount.executeQuery();
            }
	            
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
