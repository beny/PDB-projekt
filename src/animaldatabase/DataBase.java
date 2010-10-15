package animaldatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OraclePreparedStatement;

/**
 * Knihovna pro práci s databází
 * @author xizakt00 Tomáš Ižák
 * 
 */
public class DataBase {
    /**
     * private variable to store database connection
     */
    private Connection con=null;
    /**
     * private string to store url of database connection
     */
    private static String connectionString="@berta.fit.vutbr.cz:1521:stud";
    /**
     * Function for connectin to Oracle database
     * @param username username for database connection
     * @param password password for database connection
     */
    public void connect(String username,String password){
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
	} catch(java.lang.ClassNotFoundException e) {
            System.err.print("ClassNotFoundException: ");
            System.err.println(e.getMessage());
	}

        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:" + username + "/" + password +
connectionString);
	} catch(SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
	}

        if(con!= null){
	       System.out.println("Got Connection.");
	       /*DatabaseMetaData meta = con.getMetaData();
	       System.out.println("Driver Name : "+meta.getDriverName());
	       System.out.println("Driver Version : "+meta.getDriverVersion());*/

	    }else{
		    System.out.println("Could not connect");
	    }
    }
    /**
     * Function for disconnecting from database
     */
    public void disconnect(){
        if (con!=null) {
            try{
                con.close();
            } catch(SQLException ex) {
                System.out.println("Could not disconnect");
            }
            System.out.println("Disconnected");
        }
    }
}
