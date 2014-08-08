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
        PreparedStatement countStatement = null;
        PreparedStatement updateRawData = null;
        PreparedStatement controlStatement = null;
        PreparedStatement getWeightStatement = null;
        PreparedStatement getCriteiaStatement = null;
        PreparedStatement getGoalStatement = null;
        PreparedStatement updateStuctData = null;
        String url = "jdbc:mysql://localhost:3306/test_fia";
        String user = "root";
        String password = "IraAnna12";
        
        Random rand= new Random();
        try {
            con = DriverManager.getConnection(url, user, password);
            
            // read raw_data_weighted table
            countStatement = con.prepareStatement("select max(weight) from raw_data_weighted");
            rs = countStatement.executeQuery();
            rs.next();
            int max_weight = rs.getInt(1);
            int rand_weight = 0;
            long criteia = 0;
            int availability = 0;
            int goal = 0;
            float weight = 0;
            int full_count = 0;
            float selected_weight = 0;
            long selected_set_map = 0;
            getWeightStatement = con.prepareStatement("select max(weight) from raw_data_weighted where weight <= ?");
            updateRawData = con.prepareStatement("update raw_data_weighted set count = count - 1 where weight = ? and count > 0");
            controlStatement = con.prepareStatement("select max(count) from raw_data_weighted");
            getCriteiaStatement = con.prepareStatement("select criteia from raw_data_weighted where weight = ? and count > 0");
            getGoalStatement = con.prepareStatement("select set_map, full_count, availability, goal from struct_data where BIT_COUNT(set_map)=1 and set_map & ?");
            updateStuctData = con.prepareStatement("update struct_data set availability = availability - 1 where set_map = ?");
            
            do {
            	rs=controlStatement.executeQuery();
	            if (!rs.next())
	            	break;
            	if (rs.getInt(1) <= 0)
            		break;

            	// pick the request
            	getWeightStatement.setInt(1, rand.nextInt(max_weight+1));
	            rs=getWeightStatement.executeQuery();
	            if (!rs.next())
	            	break;
	            rand_weight = rs.getInt(1); 
	            updateRawData.setInt(1, rand_weight); // and decrement count
	            if (updateRawData.executeUpdate() == 0)
	            	continue; // count == 0 so try other request
	            
	            // issue the request
	            getCriteiaStatement.setInt(1, rand_weight);
	            rs = getCriteiaStatement.executeQuery();
	            if (!rs.next())
	            	break;
	            criteia = rs.getInt(1);
	            
	            // handle the request
	            getGoalStatement.setLong(1, criteia);
	            rs = getGoalStatement.executeQuery();
	            while (true) {
	            	// find best candidate
		            if (!rs.next())
		            	break;
		            full_count = rs.getInt(2);
		            availability = rs.getInt(3);
		            goal = rs.getInt(4);
		            if (goal == 0 || availability == 0 || full_count == 0)
		            	continue;
		            weight = (goal - (full_count - availability)) / goal;
		            if (weight > selected_weight) {
		            	selected_weight = weight;
		            	selected_set_map = rs.getInt(1);
		            }
	            }	            
	            
	            // issue the response
	            updateStuctData.setLong(1, selected_set_map);	            
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
                if (updateStuctData != null) {
                	updateStuctData.close();
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
