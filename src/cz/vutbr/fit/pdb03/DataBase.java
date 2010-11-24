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
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
         * use this string for accesing to table with photos of animals
         */
        public final static String ANIMAL_PHOTO = "animal_photo";
        /**
         * use this string for accesing to table with photos of excrements of animals
         */
        public final static String EXCREMENT_PHOTO = "excrement_photo";

        /**
         * use this string for accesing to table with photos of foot of animals
         */
        public final static String FEET_PHOTO = "footprint";

         /**
         * use this string for accesing to table for searching according pictures
         */
        public final static String SEARCH_PHOTO = "search_photo";

        /**
         * Number of maximum search results
         */
        private final static int MAX_SEARCH_RESULTS=25;

        /**
         * Data are here after searching
         */
        public Collection<AnimalObject> searchResult=new ArrayList<AnimalObject>();

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
         * Function for receiving animal description from database
         *
         * @param animal_id
         *           ID of an animal
         * @return String with description
         * @throws SQLException
         */
        public String getDescription(int animal_id) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "SELECT description FROM animals WHERE animal_id="+Integer.toString(animal_id);
            OracleResultSet rset = null;
            rset = (OracleResultSet) stat.executeQuery(SQLquery);
            SQLquery="";
            while (rset.next()) {
                SQLquery= rset.getString("description");
                break;
            }
            rset.close();
            stat.close();
	    return SQLquery;
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
		stat.executeQuery("CREATE TABLE "+ANIMAL_PHOTO+" (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.executeQuery("CREATE TABLE "+EXCREMENT_PHOTO+" (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.executeQuery("CREATE TABLE "+FEET_PHOTO+" (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
                stat.executeQuery("CREATE TABLE "+SEARCH_PHOTO+" (photo_id NUMBER PRIMARY KEY, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.executeQuery("CREATE TABLE animal_movement (move_id NUMBER PRIMARY KEY, animal_id NUMBER, move MDSYS.SDO_GEOMETRY, valid_from DATE, valid_to DATE)");
		stat.close();
		createSequences();
                createTriggersAndProcedures();
	}

	/**
	 * Function for searching animals by their names - choose one notation -
	 * latin or other language - do not blend! No need to fill both params,
	 * replace blank param by null or "".
	 *
	 * @param genus
	 *            genus name of an animal
	 * @param family
	 *            family name of an animal
	 * @throws SQLException
	 */
	public void searchAnimals(String genus, String family) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "SELECT TOP "+Integer.toString(MAX_SEARCH_RESULTS)+" animal_id,genus,family,genus_lat,family_lat FROM animals WHERE ";
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
                searchResult.clear();
		while (rset.next()) {
                    AnimalObject temp=new AnimalObject();
                    //selectPicture(rset.getInt("animal_id"), false,animalPhoto);
                    temp.animal_id=rset.getInt("animal_id");
                    temp.family=rset.getString("family");
                    temp.family_lat=rset.getString("family_lat");
                    temp.genus=rset.getString("genus");
                    temp.genus_lat=rset.getString("genus_lat");
                    searchResult.add(temp);
		}
                rset.close();
		stat.close();
		return;
	}

        /**
         * Function for updating spatial data (invalidating old data and inserting and validating new data)
         * !!!!!TO DO:treti parametr promyslet...
         * @param move_id
         * @param animal_id
         * @param data
         * @throws SQLException
         */
        public void updateSpatialData(int move_id, int animal_id, String data) throws SQLException{
            Statement stat = con.createStatement();
            stat.executeQuery("CALL(animal_movement_delete("+Integer.toString(move_id)+", "+Integer.toString(animal_id)+", "+data+"))");
            stat.close();
        }

        /**
         * Function for deleting (invalidating) spatial object
         * @param move_id
         *          ID of a spatial object
         * @throws SQLException
         */
        public void deleteSpatialData(int move_id) throws SQLException{
            Statement stat = con.createStatement();
            stat.executeQuery("CALL(animal_movement_delete("+Integer.toString(move_id)+"))");
            stat.close();

        }

	/**
	 * Uploads image into choosen table
	 *
	 * @param animal_id
	 *            ID of an animal
	 * @param choosen_table
	 * @param filename
	 *            Filename of a picture for upload
         * @return integer with new photo_id
	 * @throws SQLException
	 * @throws IOException
	 */
	public int uploadImage(int animal_id, String choosen_table, String filename)
			throws SQLException, IOException {
		con.setAutoCommit(false);
		Statement stat = con.createStatement();
		String SQLquery = ("SELECT " + choosen_table + "_seq.nextval FROM dual");
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		int nextval = rset.getInt("nextval");
                if (choosen_table.equals(SEARCH_PHOTO)){
                    SQLquery = "INSERT INTO " + choosen_table
				+ " (photo_id, photo, photo_sig) VALUES (" + nextval
				+ ", ordsys.ordimage.init(), ordsys.ordimagesignature.init())";
                } else {
                    SQLquery = "INSERT INTO " + choosen_table
				+ " (photo_id, animal_id, photo, photo_sig) VALUES (" + nextval
				+ ", " + animal_id
				+ ", ordsys.ordimage.init(), ordsys.ordimagesignature.init())";
                }
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
				+ " SET photo=?, photo_sig=? where photo_id=?";
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
                createIndex(choosen_table);
                return nextval;
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
                        rset.close();
			stat.close();
		} catch (SQLException a) {
			id = 0;
		}
		return id;
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
                rset.close();
		stat.close();
		return data;
	}

        /**
         * Function for inserting new animals into database
         * @param genus
         * @param family
         * @param genus_lat
         * @param family_lat
         * @param description
         * @throws SQLException
         */
        public void insertAnimal(String genus, String family, String genus_lat, String family_lat, String description) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "INSERT INTO animals (genus, family, genus, genus_lat, description) VALUES (?,?,?,?,?) ";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setString(1, genus);
            opstmt.setString(2, family);
            opstmt.setString(3, genus_lat);
            opstmt.setString(4, family_lat);
            opstmt.setString(5, description);
            opstmt.execute();
            opstmt.close();
            stat.close();
        }

        /**
         * Function for updating selected animal
         * TODO: jestli nedat jako vstupní parametr AnimalObject....???
         * @param animal_id
         * @param genus
         * @param family
         * @param genus_lat
         * @param family_lat
         * @param description
         * @throws SQLException
         */
        public void updateAnimal(int animal_id, String genus, String family, String genus_lat, String family_lat, String description) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "UPDATE animals SET (genus=?, family=?, genus=?, genus_lat=?, decsription=?) WHERE animal_id=? ";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setString(1, genus);
            opstmt.setString(2, family);
            opstmt.setString(3, genus_lat);
            opstmt.setString(4, family_lat);
            opstmt.setString(5, description);
            opstmt.setInt(6, animal_id);
            opstmt.execute();
            opstmt.close();
            stat.close();
        }

        /**
         * Function for deleting an animal (with it's pictures and movemets - trigger)
         * @param animal_id
         * @throws SQLException
         */
        public void deleteAnimal(int animal_id) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "DELETE FROM animals WHERE animal_id=?";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setInt(1, animal_id);
            opstmt.execute();
            opstmt.close();
            stat.close();
        }

	/**
	 * Function for deleting objects in database - important before creating a
	 * new database
	 *
	 * @throws SQLException
	 */
	private void deleteDatabase() throws SQLException {
            deleteIndex(ANIMAL_PHOTO);
            deleteIndex(FEET_PHOTO);
            deleteIndex(EXCREMENT_PHOTO);
            deleteIndex(SEARCH_PHOTO);
            Statement stat = con.createStatement();
            try {
            	stat.executeQuery("DROP TABLE animals");
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP SEQUENCE animals_seq");
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP TABLE "+ANIMAL_PHOTO);
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP SEQUENCE "+ANIMAL_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP TABLE "+EXCREMENT_PHOTO);
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP SEQUENCE "+EXCREMENT_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
            	stat.executeQuery("DROP TABLE "+FEET_PHOTO);
            } catch (SQLException e) {}
            try {
            	stat.executeQuery("DROP SEQUENCE "+FEET_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
            	stat.executeQuery("DROP TABLE animal_movement");
            } catch (SQLException e) {}
            try {
            	stat.executeQuery("DROP SEQUENCE animal_movement_seq");
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP TABLE "+SEARCH_PHOTO);
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP SEQUENCE "+SEARCH_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
            	stat.executeQuery("DROP TRIGGER animals_trigger");
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP TRIGGER animal_movement_trigger_i");
            } catch (SQLException e) {}
            try {
            	stat.executeQuery("DROP PROCEDURE animal_movement_delete");
            } catch (SQLException e) {}
            try {
		stat.executeQuery("DROP PROCEDURE animal_movement_update");
            } catch (SQLException e) {}
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
		stat.executeQuery("CREATE SEQUENCE "+FEET_PHOTO+"_seq START WITH 1 INCREMENT BY 1");
		stat.executeQuery("CREATE SEQUENCE "+EXCREMENT_PHOTO+"_seq START WITH 1 INCREMENT BY 1");
		stat.executeQuery("CREATE SEQUENCE "+ANIMAL_PHOTO+"_seq START WITH 1 INCREMENT BY 1");
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
         * @param choosen_table
         *              from which table we want pictures
         * @return HashMap<Integer photo_id,OrdImage photo>
	 * @throws SQLException
	 */
	private Map<Integer,OrdImage> selectPicture(int id, boolean all, String choosen_table) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery;
		if (all) {
			SQLquery = "SELECT photo, photo_id FROM "+choosen_table+" WHERE animal_id="
					+ Integer.toString(id);
		} else {
			SQLquery = "SELECT TOP 1 photo, photo_id FROM "+choosen_table+" WHERE animal_id="
					+ Integer.toString(id);
		}
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
                HashMap<Integer,OrdImage> result = new HashMap<Integer,OrdImage>();
		while (rset.next()) {
                    result.put(rset.getInt("photo_id"),(OrdImage) rset.getORAData("photo",OrdImage.getORADataFactory()));
		}
                rset.close();
		stat.close();
		return result;
	}

        /**
         * Function for creating stored procedures and triggers in database.
         * @throws SQLException
         */
        private void createTriggersAndProcedures() throws SQLException{
            Statement stat = con.createStatement();
            stat.executeQuery("CREATE OR REPLACE TRIGGER animals_trigger "+
                              "BEFORE INSERT OR DELETE ON animals FOR EACH ROW "+
                              "BEGIN "+
                              "  IF INSERTING THEN "+
                              "    SELECT animals_seq.nextval INTO :NEW.animal_id FROM dual; "+
                              "  ELSIF DELETING THEN "+
                              "    DELETE FROM "+ANIMAL_PHOTO+" WHERE animal_id=:old.animal_id; "+
                              "    DELETE FROM "+EXCREMENT_PHOTO+" WHERE animal_id=:old.animal_id; "+
                              "    DELETE FROM "+FEET_PHOTO+" WHERE animal_id=:old.animal_id; "+
                              "  END IF; END;");
            stat.executeQuery("CREATE OR REPLACE TRIGGER animal_movement_trigger_i "+
                              "BEFORE INSERT ON animal_movement FOR EACH ROW "+
                              "BEGIN"+
                              "  IF (:new.valid_from is NULL) THEN :NEW.valid_from:=sysdate; "+
                              "  END IF; END; ");
            stat.executeQuery("CREATE OR REPLACE PROCEDURE animal_movement_delete( "+
                              "my_move_id IN	NUMBER) AS "+
                              "BEGIN "+
                              "  UPDATE animal_movement SET valid_to=sysdate WHERE move_id=my_move_id; "+
                              "END animal_movement_delete; ");
            stat.executeQuery("CREATE OR REPLACE PROCEDURE animal_movement_update( "+
                              "my_move_id IN	NUMBER, "+
                              "my_animal_id IN	NUMBER, "+
                              "my_move IN	MDSYS.SDO_GEOMETRY) AS "+
                              "BEGIN "+
                              "  DECLARE cislo NUMBER; "+
                              "  BEGIN "+
                              "    UPDATE animal_movement SET valid_to=sysdate WHERE move_id=my_move_id; "+
                              "    SELECT animal_movement_seq.nextval INTO cislo FROM dual; "+
                              "    INSERT INTO animal_movement (move_id,animal_id,move) VALUES (cislo,my_animal_id,my_move); "+
                              "  END; "+
                              "END animal_movement_update;");
            stat.close();
        }

        /**
         * Function for creating picture index
         * @param tablename
         */
        private void createIndex(String tablename){
        try {
            Statement stat = con.createStatement();
            stat.executeQuery("CREATE INDEX " + tablename + "_idx ON " + tablename + " (photo_sig) INDEXTYPE IS ordsys.ordimageindex;");
            stat.close();
        } catch (SQLException ex) {
        }
        }

        /**
         * Function for deleting picture index
         * @param tablename
         */
        private void deleteIndex(String tablename){
        try {
            Statement stat = con.createStatement();
            stat.executeQuery("DROP INDEX " + tablename + "_idx;");
            stat.close();
        } catch (SQLException ex) {
        }
        }

        /**
         * Function using for searching animals according their photos
         * @param filename
         * @param tablename
         * @throws SQLException
         * @throws IOException
         */
        public void searchAnimal(String filename, String tablename) throws SQLException, IOException{
            Statement stat = con.createStatement();
            int nextval=uploadImage(0,SEARCH_PHOTO,filename);
            String SQLquery = "SELECT TOP "+Integer.toString(MAX_SEARCH_RESULTS)+" DISTINCT animal.animal_id,animal.genus,animal.family,animal.genus_lat,animal.family_lat FROM "+
                    SEARCH_PHOTO+" fp, "+tablename+" photodb, animals animal "+
                    "WHERE ordsys.IMGSimilar(fp.photo_sig, photodb.photo_sig, 'color=0.3, texture=0.3, shape=0.3, location=0.1',100 ,123)=1 "+
                    "AND photodb.photo_id="+Integer.toString(nextval)+" AND animal.animal_id=photodb.animal_id "+
                    "ORDER BY ordsys.IMGScore(123) ASC;";
            OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
            searchResult.clear();
            while (rset.next()) {
                AnimalObject temp=new AnimalObject();
                temp.animal_id=rset.getInt("animal_id");
                temp.family=rset.getString("family");
                temp.family_lat=rset.getString("family_lat");
                temp.genus=rset.getString("genus");
                temp.genus_lat=rset.getString("genus_lat");
                searchResult.add(temp);
            }
            rset.close();
            stat.close();
            return;
        }
}
