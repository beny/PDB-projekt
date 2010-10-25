package cz.vutbr.fit.pdb03;

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
     * 
     * @return true if connection is alive, 0 if not connected
     */
    public boolean isConnected()
    {
        if (con==null) return false;
        else return true;
    }
    /**
     * Function for connectin to Oracle database
     * @param username username for database connection
     * @param password password for database connection
     */
    public void connect(String username,String password) throws Exception{
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
	} catch(java.lang.ClassNotFoundException e) {
            throw new Exception("ClassNotFoundException: "+ e.getMessage());
	}

        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:" + username + "/" + password +
connectionString);
	} catch(SQLException ex) {
            throw new Exception("SQLException: " + ex.getMessage());
	}

        if(! isConnected()){
            throw new Exception("Could not connect");
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
                //System.out.println("Could not disconnect");
            }
            //System.out.println("Disconnected");
        }
    }
}
