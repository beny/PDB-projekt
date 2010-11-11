package cz.vutbr.fit.pdb03;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Statement;
//import ojdbc6.jar from oraclelib.zip/oraclelib/jdbc located in WIS
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OraclePreparedStatement;
//import ordim.jar from oraclelib.zip/oraclelib/ord located in WIS
import oracle.ord.im.OrdImage;
import oracle.ord.im.OrdImageSignature;

/**
 * Knihovna pro práci s databází
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public class DataBase {

	/**
	 * private string to store url of database connection
	 */
	private static String connectionString = "@berta.fit.vutbr.cz:1521:stud";

	/**
	 * private variable to store database connection
	 */
	private Connection con = null;

	/**
	 * Function for connectin to Oracle database
	 *
	 * @param username
	 *            username for database connection
	 * @param password
	 *            password for database connection
	 * @throws Exception
	 */
	public void connect(String username, String password) throws SQLException,
			ClassNotFoundException, Exception {
		con = DriverManager.getConnection("jdbc:oracle:thin:" + username + "/"
				+ password + connectionString);
		con.setAutoCommit(true);
	}

	/**
	 * Function for disconnecting from database
	 *
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException {
		if (con != null) {
			con.close();
			// System.out.println("Disconnected");
		}
	}

	/**
	 * SIMPLE function if needed to determine, if system is connected.
	 *
	 * @return true if connection is alive, 0 if not connected
	 */
	public boolean isConnected() {
		if (con == null)
			return false;
		else
			return true;
	}

	/**
	 * Function for creating dabase - no need to destroy previous database
	 * objects - this function cares of that.
	 *
	 * @throws SQLException
	 */
	public void createDatabase() throws SQLException {
		deleteDatabase();
		Statement stat = con.createStatement();
		stat.executeQuery("CREATE TABLE animals (animal_id NUMBER PRIMARY KEY, genus VARCHAR(20), family VARCHAR(20), genus_lat VARCHAR(20), family_lat VARCHAR(20), description VARCHAR(500))");
		stat.executeQuery("CREATE TABLE animal_photo (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.executeQuery("CREATE TABLE excrement_photo (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.executeQuery("CREATE TABLE footprint (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.executeQuery("CREATE TABLE animal_movement (move_id NUMBER PRIMARY KEY, animal_id NUMBER, move MDSYS.SDO_GEOMETRY)");
		stat.close();
		createSequences();
	}

	/**
	 * Function for searching animals by their names - choose one notation -
	 * latin or other language - do not blend! No need to fill both params,
	 * replace blank param by null or "".
	 *
	 * TO DO: return gained data
	 *
	 * @param genus
	 *            genus name of an animal
	 * @param family
	 *            family name of an animal
	 * @throws SQLException
	 */
	public void searchAnimals(String genus, String family) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "SELECT animal_id,genus,family,genus_lat,family_lat FROM animals WHERE ";
		OracleResultSet rset = null;
		if (genus == null ? "" == null : genus.equals("")) {
			if (family == null ? "" == null : family.equals("")) {
				stat.close();
				return;
			} else {
				rset = (OracleResultSet) stat.executeQuery(SQLquery
						+ "family='" + family + "' or family_lat='" + family
						+ "'");
			}
		} else {
			if (family == null ? "" == null : family.equals("")) {
				rset = (OracleResultSet) stat.executeQuery(SQLquery + "genus='"
						+ genus + "' or genus_lat='" + genus + "'");
			} else {
				rset = (OracleResultSet) stat.executeQuery(SQLquery
						+ "(family='" + family + "' and genus='" + genus
						+ "') or (family_lat='" + family + "' and genus_lat='"
						+ genus + "')");
			}
		}
		while (rset.next()) {
			selectPicture(rset.getInt("animal_id"), false);
		}
		stat.close();
		return;
	}

	/**
	 * Uploads image into choosen table
	 *
	 * @param animal_id
	 *            ID of an animal
	 * @param choosen_table
	 * @param filename
	 *            Filename of a picture for upload
	 * @throws SQLException
	 * @throws IOException
	 */
	public void uploadImage(int animal_id, String choosen_table, String filename)
			throws SQLException, IOException {
		con.setAutoCommit(false);
		Statement stat = con.createStatement();
		String SQLquery = ("SELECT " + choosen_table + "_seq.nextval FROM dual");
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		int nextval = rset.getInt("nextval");
		SQLquery = "INSERT INTO " + choosen_table
				+ " (animal_id, photo_id, photo, photo_sig) VALUES (" + nextval
				+ ", " + animal_id
				+ ", ordsys.ordimage.init(), ordsys.ordimagesignature.init())";
		stat.execute(SQLquery);
		SQLquery = "SELECT image, signature FROM " + choosen_table
				+ " WHERE animal_id = " + nextval + " FOR UPDATE";
		rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		OrdImage imageProxy = (OrdImage) rset.getORAData("photo",
				OrdImage.getORADataFactory());
		OrdImageSignature signatureProxy = (OrdImageSignature) rset
				.getCustomDatum("photo_sig", OrdImageSignature.getFactory());
		rset.close();
		imageProxy.loadDataFromFile(filename);
		imageProxy.setProperties();
		signatureProxy.generateSignature(imageProxy);
		SQLquery = "UPDATE " + choosen_table
				+ " SET photo=?, photo_sig=? where animal_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
		opstmt.setCustomDatum(1, imageProxy);
		opstmt.setCustomDatum(2, signatureProxy);
		opstmt.setInt(3, nextval);
		opstmt.execute();
		opstmt.close();

		con.commit();
		con.setAutoCommit(true);
		stat.close();
	}

	/**
	 * Saves point into database
	 *
	 * @param animal_id
	 *            ID of concrete animal
	 * @param point
	 *            point for save into database
	 * @return id of a concrete point - 0 if something bad happened
	 */
	public int savePoint(int animal_id, Point2D point) {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		int id = 0;
		try {
			con.setAutoCommit(false);
			Statement stat = con.createStatement();
			String SQLquery = ("SELECT animal_movement_seq.nextval FROM dual");
			OracleResultSet rset = (OracleResultSet) stat
					.executeQuery(SQLquery);
			rset.next();
			id = rset.getInt("nextval");
			SQLquery = "INSERT INTO animal_movement (id,animal_id,point) VALUES ("
					+ Integer.toString(id)
					+ ","
					+ Integer.toString(animal_id)
					+ ",SDO_GEOMETRY(2001,"
					+ nf.format(point.getX())
					+ ","
					+ nf.format(point.getY()) + "))";
			stat.execute(SQLquery);
			con.commit();
			con.setAutoCommit(true);
			stat.close();
		} catch (SQLException a) {
			id = 0;
		}
		return id;
	}

	/**
	 * Deletes point from database
	 *
	 * @param id
	 *            id of point
	 * @return false if something bad happened :(
	 */
	public boolean deletePoint(int id) {
		try {
			Statement stat = con.createStatement();
			String SQLquery = ("DELETE FROM animal_movement WHERE id=" + Integer
					.toString(id));
			stat.executeQuery(SQLquery);
			stat.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns points which belongs to a current animal
	 *
	 * @param animal_id
	 *            id of current animal
	 * @return HashMap<Integer,Point2D> list of points belongs to current animal
	 * @throws SQLException
	 */
	public Map<Integer, Point2D> selectPoints(int animal_id)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "SELECT point.x, point.y, id FROM animal_movement WHERE animal_id="
				+ Integer.toString(animal_id);
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		HashMap<Integer, Point2D> data = new HashMap<Integer, Point2D>();
		Point2D point = new Point2D.Double();
		while (rset.next()) {
			point.setLocation(rset.getDouble("point.x"),
					rset.getDouble("point.y"));
			data.put(animal_id, point);
		}
		stat.close();
		return data;
	}

	/**
	 * Function for deleting objects in database - important before creating a
	 * new database
	 *
	 * @throws SQLException
	 */
	private void deleteDatabase() throws SQLException {
		Statement stat = con.createStatement();
		try {
			stat.executeQuery("DROP TABLE animals");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP SEQUENCE animals_seq");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP TABLE animal_photo");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP SEQUENCE animal_photo_seq");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP TABLE excrement_photo");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP SEQUENCE excrement_photo_seq");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP TABLE footprint");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP SEQUENCE footprint_seq");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP TABLE animal_movement");
		} catch (SQLException e) {
		}
		try {
			stat.executeQuery("DROP SEQUENCE animal_movement_seq");
		} catch (SQLException e) {
		}
		stat.close();
	}

	/**
	 * Function for creating sequences in database - all starts with 1 and are
	 * incremented by 1
	 *
	 * @throws SQLException
	 */
	private void createSequences() throws SQLException {
		Statement stat = con.createStatement();
		stat.executeQuery("CREATE SEQUENCE animal_movement_seq START WITH 1 INCREMENT BY 1");
		stat.executeQuery("CREATE SEQUENCE footprint_seq START WITH 1 INCREMENT BY 1");
		stat.executeQuery("CREATE SEQUENCE excrement_photo_seq START WITH 1 INCREMENT BY 1");
		stat.executeQuery("CREATE SEQUENCE animal_photo_seq START WITH 1 INCREMENT BY 1");
		stat.executeQuery("CREATE SEQUENCE animals_seq START WITH 1 INCREMENT BY 1");
		stat.close();
	}

	/**
	 * Selects a random thumbnail or all photos of an animal, it's used for an
	 * illustrating found results
	 *
	 * TO DO: return gained data
	 *
	 * @param id
	 *            ID of an animal
	 * @param all
	 *            true if return all pictures of an animal, false if return only
	 *            one thumbnail
	 * @throws SQLException
	 */
	private void selectPicture(int id, boolean all) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery;
		if (all) {
			SQLquery = "SELECT animal_photo FROM animal_photo WHERE animal_id="
					+ Integer.toString(id);
		} else {
			SQLquery = "SELECT TOP 1 animal_photo FROM animal_photo WHERE animal_id="
					+ Integer.toString(id);
		}
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		rset.getInt("animal_id");
		stat.close();
		return;
	}
}
