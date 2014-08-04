
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

// import com.mysql.jdbc.PreparedStatement;

	public class GenInput {
		public static final int bitmask = 0x000F;

	    public static void main(String[] args) {

	        Connection con = null;
	        Statement st = null;
	        ResultSet rs = null;
	        PreparedStatement insertStatement = null;
	        PreparedStatement controlStatement = null;
	        String url = "jdbc:mysql://localhost:3306/test_fia";
	        String user = "root";
	        String password = "IraAnna12";
	        
	        Random rand= new Random();
	        int count_bitmask=0x000F;
	        long criteria_bitmask=0x0000000F;
	        

	        try {
	            con = DriverManager.getConnection(url, user, password);
	            
	            // empty raw data
	            st = con.createStatement();
	            st.executeUpdate("DELETE FROM RAW_DATA"); 
	            
	            // populate table
	            controlStatement = con.prepareStatement("SELECT COUNT(*) FROM RAW_DATA");
	            insertStatement = con.prepareStatement("INSERT INTO RAW_DATA SET criteia = ?, count = ? ON DUPLICATE KEY UPDATE count = ?");
	            do {
	            	insertStatement.setLong(1, rand.nextLong()&criteria_bitmask);
		            insertStatement.setInt(2, rand.nextInt()&count_bitmask);
		            insertStatement.setInt(3, rand.nextInt()&count_bitmask);
		            insertStatement.execute();
		            rs=controlStatement.executeQuery();
	            } while (rs.next() && rs.getInt(1) <= criteria_bitmask);

	        } catch (SQLException ex) {
	            Logger lgr = Logger.getLogger(GenInput.class.getName());
	            lgr.log(Level.SEVERE, ex.getMessage(), ex);

	        } finally {
	            try {
	                if (st != null) {
	                    st.close();
	                }
	                if (controlStatement != null) {
	                	controlStatement.close();
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
	}
	
