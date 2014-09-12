
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

// import com.mysql.jdbc.PreparedStatement;

	public class GenInput {
		public static final long criteria_bitmask=0x0000007F;
		public static final int max_volume = 100;

	    public static void main(String[] args) {

	        Connection con = null;
	        Statement st = null;
	        PreparedStatement insertStatement = null;
	        String url = "jdbc:mysql://localhost:3306/test_fia";
	        String user = "root";
	        String password = "IraAnna12";
	        
	        Random rand= new Random();        

	        try {
	            con = DriverManager.getConnection(url, user, password);
	            
	            // empty raw data
	            st = con.createStatement();
	            st.executeUpdate("DELETE FROM RAW_DATA"); 
	            
	            // populate table
	            insertStatement = con.prepareStatement("INSERT IGNORE INTO RAW_DATA SET criteia = ?, count = ?");
	            for (int criteia = 0; criteia <= criteria_bitmask; criteia++) {
	            	insertStatement.setLong(1, criteia);
		            insertStatement.setInt(2, rand.nextInt(max_volume));
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
	}
	
