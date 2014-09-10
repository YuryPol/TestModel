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
        PreparedStatement clearResultStatement=null;
        PreparedStatement clearMissesStatement=null;
        PreparedStatement countStatement = null;
        PreparedStatement updateRawData = null;
        PreparedStatement controlStatement = null;
        PreparedStatement getWeightStatement = null;
        PreparedStatement getCriteiaStatement = null;
        PreparedStatement getGoalStatement = null;
        PreparedStatement updateResultData = null;
        PreparedStatement updateMissedCalls = null;
        PreparedStatement getServedCountStatement = null;
        String url = "jdbc:mysql://localhost:3306/test_fia";
        String user = "root";
        String password = "IraAnna12";
        
        Random rand= new Random();
        try {
            con = DriverManager.getConnection(url, user, password);
            
            // clear the results
            clearResultStatement = con.prepareStatement("delete from result_data");
            clearResultStatement.execute();
            clearMissesStatement =con.prepareStatement("delete from misses");
            // read raw_data_weighted table
            countStatement = con.prepareStatement("select max(weight) from raw_data_weighted");
            rs = countStatement.executeQuery();
            rs.next();
            int max_weight = rs.getInt(1);
            int rand_weight = 0;
            long criteia = 0;
            int served_count = 0;
            int goal = 0;
            float cri_weight = 0;
            int full_count = 0;
            long set_map = 0;
            float selected_cri_weight = 0;
            long selected_set_map = 0;
            getWeightStatement = con.prepareStatement("select max(weight) from raw_data_weighted where weight <= ?");
            updateRawData = con.prepareStatement("update raw_data_weighted set count = count - 1 where weight = ? and count > 0");
            controlStatement = con.prepareStatement("select max(count) from raw_data_weighted");
            getCriteiaStatement = con.prepareStatement("select criteia from raw_data_weighted where weight = ? and count > 0");
            getGoalStatement = con.prepareStatement("select set_map, full_count, availability, goal from struct_data where BIT_COUNT(set_map)=1 and set_map & ?");
            getServedCountStatement = con.prepareStatement("select count from result_data where set_map = ?");
            updateResultData = con.prepareStatement("insert into result_data (set_map, count) values(?, 1) on duplicate key update count = count + 1");
            updateMissedCalls = con.prepareStatement("insert into misses (criteia, count) values(?, 1) on duplicate key update count = count + 1");
            
            do {
            	selected_set_map = 0;
            	selected_cri_weight = 0;
            	
            	rs=controlStatement.executeQuery();
	            if (!rs.next())
	            	break; // no raw data
            	if (rs.getInt(1) <= 0)
            		break; // raw data were used up

            	// issue the request
            	getWeightStatement.setInt(1, rand.nextInt(max_weight+1));
	            rs=getWeightStatement.executeQuery();
	            if (!rs.next())
	            	continue;
	            rand_weight = rs.getInt(1); 
	            updateRawData.setInt(1, rand_weight); // and decrement count
	            if (updateRawData.executeUpdate() == 0)
	            	continue; // count == 0 so try other request
	            
	            // issue the request
	            getCriteiaStatement.setInt(1, rand_weight);
	            rs = getCriteiaStatement.executeQuery();
	            if (!rs.next())
	            	continue;
	            criteia = rs.getInt(1);
	            
	            // handle the request	        
	            getGoalStatement.setLong(1, criteia);
	            rs = getGoalStatement.executeQuery();
	            if (!rs.next()) {
	            	// not a target or no match found
	            	updateMissedCalls.setLong(1, criteia);
	            	updateMissedCalls.executeUpdate();
	            	continue;  
	            }
	            
	            do {
	            	// find best candidate
	            	set_map = rs.getInt(1);
		            full_count = rs.getInt(2);
		            goal = rs.getInt(4);		            
		            if (goal == 0 || full_count == 0) {
			            	continue;
		            }
		            getServedCountStatement.setLong(1, rs.getInt(1));		           
		            rs1 = getServedCountStatement.executeQuery();
		            if (!rs1.next())
		            {
		            	served_count = 0;
		            }
		            else {
		            	served_count = rs1.getInt(1);
		            }
		            cri_weight = (float)(goal - served_count) / goal;
		            if (cri_weight > selected_cri_weight) {
		            	selected_set_map = set_map;
		            	selected_cri_weight = cri_weight;
		            }
	            } while (rs.next());         
	            
	            // issue the response
	            updateResultData.setLong(1, selected_set_map);
	            updateResultData.executeUpdate();
            } while (true);

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Simulation.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (countStatement != null) {
                	countStatement.close();
                }
                if (getWeightStatement != null) {
                	getWeightStatement.close();
                }
                if (updateRawData != null) {
                	updateRawData.close();
                }
                if (getCriteiaStatement != null) {
                	getCriteiaStatement.close();
                }
                if (getGoalStatement != null) {
                	getGoalStatement.close();
                }
                if (updateResultData != null) {
                	updateResultData.close();
                }
                if (updateMissedCalls != null) {
                	updateMissedCalls.close();
                }
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
