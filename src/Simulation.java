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
            PreparedStatement clearResultStatement = con.prepareStatement("delete from result_data");
            clearResultStatement.execute();
            PreparedStatement clearMissesStatement =con.prepareStatement("delete from misses");
            // read raw_inventory table
            PreparedStatement max_weightStatement = con.prepareStatement("select max(weight) from raw_inventory");
            rs = max_weightStatement.executeQuery();
            if (!rs.next())
            	break; // no raw data
            int max_weight = rs.getInt(1);
            PreparedStatement getDataStatement = con.prepareStatement("select basesets  from raw_inventory where weight <= ? order BY weight desc limit 1");
            PreparedStatement choseInventorySet = con.prepareStatement(
            		"select set_key_is, (goal-served_count)/goal as weight_now from result_serving where goal > served_count and ( ? | set_key_is ) = set_key_is order by weight_now desc limit 1;");
            PreparedStatement incrementServedCount = con.prepareStatement("update result_serving set served_count = served_count - 1 where set_key_is = ? and served_count > 0");
            //
            // Process raw inventory
            //
            do {
	            // create the request
	            getDataStatement.setInt(1, rand.nextInt(max_weight));
	            rs = getDataStatement.executeQuery();
	            long basesets = rs.getInt(1);
	            // select inventory set to serve
	            choseInventorySet.setLong(1, basesets);
	            rs = choseInventorySet.executeQuery();
	            if (!rs.next())
	            	continue; // inventory set was served
	            long set_key_is = rs.getLong(1);
	            // increment served_count in result_serving
	            incrementServedCount.setLong(1, set_key_is);
	            rs = incrementServedCount.executeQuery();
	            
	            
             
            
            
            
            rs.next();
            int max_weight = rs.getInt(1);
            int weight_count = rs.getInt(2);
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
            getGoalStatement = con.prepareStatement("select set_map, full_count, availability, goal from struct_data where BIT_COUNT(set_map)=1 and goal > 0 and set_map & ?");
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

            	// choose the request
            	getWeightStatement.setInt(1, rand.nextInt(max_weight+max_weight/weight_count));
	            rs=getWeightStatement.executeQuery();
	            if (!rs.next())
	            	continue; // random was too low
	            rand_weight = rs.getInt(1); 
	            if (rs.wasNull())
	            	continue; // random was too low
            	
	            // get the request out of the pool
	            updateRawData.setInt(1, rand_weight); // and decrement count
	            if (updateRawData.executeUpdate() == 0)
	            	continue; // count == 0 so try other request
	            
	            // issue the request
	            getCriteiaStatement.setInt(1, rand_weight);
	            rs = getCriteiaStatement.executeQuery();
	            if (!rs.next())
	            	continue; // shouldn't ever happen, as we checked count before
	            criteia = rs.getInt(1);
	            
	            // handle the request	        
	            getGoalStatement.setLong(1, criteia);
	            rs = getGoalStatement.executeQuery();
	            if (!rs.next()) {
	            	// not a target or no match found or goal was fulfilled 
	            	updateMissedCalls.setLong(1, criteia);
	            	updateMissedCalls.executeUpdate();
	            	continue;  
	            }
	            
	            do {
	            	// find the best candidate
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
	            if (selected_set_map > 0) {
		            updateResultData.setLong(1, selected_set_map);
		            updateResultData.executeUpdate();
	            }
	            else  {
	            	// no match found or goal was fulfilled 
	            	updateMissedCalls.setLong(1, criteia);
	            	updateMissedCalls.executeUpdate();
	            }
            } while (true);

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Simulation.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                if (weightStatement != null) {
                	weightStatement.close();
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
