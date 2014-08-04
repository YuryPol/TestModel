
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildStructData {

	public static void main(String[] args) {
        Connection con = null;
        Statement st = null;
        int res;
        PreparedStatement insertStatement = null;
        String url = "jdbc:mysql://localhost:3306/test_fia";
        String user = "root";
        String password = "IraAnna12";

        try {
            con = DriverManager.getConnection(url, user, password);
            
            // empty structured data
            st = con.createStatement();
            st.executeUpdate("DELETE FROM STRUCT_DATA"); 
            
            // populate table with all-level data
            insertStatement = con.prepareStatement(
               "INSERT INTO STRUCT_DATA (set_map, full_count) SELECT criteia, count FROM RAW_DATA ON DUPLICATE KEY UPDATE full_count = full_count + count");            
            res=insertStatement.executeUpdate();            
            
        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(BuildStructData.class.getName());
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
                Logger lgr = Logger.getLogger(BuildStructData.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
	}
}

