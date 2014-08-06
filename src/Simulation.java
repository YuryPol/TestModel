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
        PreparedStatement updateStatement = null;
        PreparedStatement controlStatement = null;
        PreparedStatement getWeightStatement = null;
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
            int rand_weight;
            getWeightStatement = con.prepareStatement("select max(weight) from raw_data_weighted where weight <= ?");
            updateStatement = con.prepareStatement("update raw_data_weighted set count = count - 1 where weight = ? and count > 0");
            controlStatement = con.prepareStatement("select max(count) from raw_data_weighted");
            
            do {
            	rs=controlStatement.executeQuery();
            	rs.next();
            	if (rs.getInt(1) <= 0)
            		break;

            	getWeightStatement.setInt(1, rand.nextInt(max_weight+1));
	            rs=getWeightStatement.executeQuery();
	            rs.next();
	            rand_weight = rs.getInt(1);
	            // issue the request
	            // decrement count
	            updateStatement.setInt(1, rand_weight);
	            if (updateStatement.executeUpdate() == 0)
	            	continue;
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
                if (updateStatement != null) {
                	updateStatement.close();
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

}
