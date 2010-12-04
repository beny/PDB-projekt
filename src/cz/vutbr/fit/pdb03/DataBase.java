package cz.vutbr.fit.pdb03;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
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
import oracle.spatial.geometry.JGeometry;
//import sdoapi.jar from oraclelib.zip/sdo/ located in WIS
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
/////////Constants and variables
	/**
	 * private string to store url of database connection
	 */
	private static String connectionString = "@berta.fit.vutbr.cz:1521:stud";

	/**
	 * private variable to store database connection
	 */
	private Connection con = null;

		public static final int MAX_STRING = 25;
		public static final int MAX_LONG_STRING = 1500;
                private static final String RESOLUTION = "250 250";
                private static final String EXAMPLE_FILE = "C:/mysqlfile.sql";
                private static final String PICTURE_FOLDER = "C:/pictures/";

        /**
         * use this string for accesing to table with photos of animals
         * @see #deletePicture(int, java.lang.String)
         * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String)
         * @see #createIndex(java.lang.String)
         * @see #searchAnimal(java.lang.String, java.lang.String)
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #SEARCH_PHOTO
         */
        public final static String ANIMAL_PHOTO = "animal_photo";
        /**
         * use this string for accesing to table with photos of excrements of animals
         * @see #deletePicture(int, java.lang.String)
         * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String)
         * @see #createIndex(java.lang.String)
         * @see #searchAnimal(java.lang.String, java.lang.String)
         * @see #ANIMAL_PHOTO
         * @see #FEET_PHOTO
         * @see #SEARCH_PHOTO
         */
        public final static String EXCREMENT_PHOTO = "excrement_photo";

        /**
         * use this string for accesing to table with photos of foot of animals
         * @see #deletePicture(int, java.lang.String)
         * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String)
         * @see #createIndex(java.lang.String)
         * @see #searchAnimal(java.lang.String, java.lang.String)
         * @see #EXCREMENT_PHOTO
         * @see #ANIMAL_PHOTO
         * @see #SEARCH_PHOTO
         */
        public final static String FEET_PHOTO = "footprint";

         /**
         * use this string for accesing to table for searching according pictures
         * @see #deletePicture(int, java.lang.String)
         * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String)
         * @see #createIndex(java.lang.String)
         * @see #searchAnimal(java.lang.String, java.lang.String)
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #ANIMAL_PHOTO
         */
        public final static String SEARCH_PHOTO = "search_photo";

        /**
         * Number of maximum search results
         * @see #searchResult
         * @see #searchAnimal(java.lang.String, java.lang.String)
         * @see #searchAnimals(java.lang.String, java.lang.String)
         * @see #searchNearestAnimals(java.awt.geom.Point2D)
         */
        private final static int MAX_SEARCH_RESULTS=50;

        /**
         * Data storage after searching
         * @see #MAX_SEARCH_RESULTS
         * @see #searchAnimal(java.lang.String, java.lang.String)
         * @see #searchAnimals(java.lang.String, java.lang.String)
         * @see #searchNearestAnimals(java.awt.geom.Point2D)
         */
        public Collection<Animal> searchResult=new ArrayList<Animal>();

/////////PUBLIC FUNCTIONS///////////////////////////////////////////////////////
/////////Functions for creating database
	/**
	 * Function for connectin to Oracle database
	 *
	 * @param username
	 *            username for database connection
	 * @param password
	 *            password for database connection
	 * @throws SQLException
         * @see #disconnect()
         * @see #isConnected()
	 */
	public void connect(String username, String password) throws SQLException {
		con = DriverManager.getConnection("jdbc:oracle:thin:" + username + "/"
				+ password + connectionString);
		con.setAutoCommit(true);
	}

	/**
	 * Function for disconnecting from database
	 * @see #connect(java.lang.String, java.lang.String)
         * @see #isConnected()
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException {
		if (isConnected()) con.close();
	}

        /**
	 * Function for creating dabase - no need to destroy previous database
	 * objects - this function cares of that.
	 * @see #deleteDatabase()
         * @see #createIndex(java.lang.String)
         * @see #createSequences()
         * @see #createTriggersAndProcedures()
	 * @throws SQLException
	 */
	public void createDatabase() throws SQLException {
                D.log("Recreating database...");
		deleteDatabase();
                D.log("Database deleted!");
		Statement stat = con.createStatement();
		stat.execute("CREATE TABLE animals (animal_id NUMBER PRIMARY KEY, genus VARCHAR(" + MAX_STRING + "), species VARCHAR(" + MAX_STRING + "), genus_lat VARCHAR(" + MAX_STRING + "), species_lat VARCHAR(" + MAX_STRING + "), description VARCHAR(" + MAX_LONG_STRING + "))");
		stat.execute("CREATE TABLE "+ANIMAL_PHOTO+" (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.execute("CREATE TABLE "+EXCREMENT_PHOTO+" (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.execute("CREATE TABLE "+FEET_PHOTO+" (photo_id NUMBER PRIMARY KEY, animal_id NUMBER, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
                stat.execute("CREATE TABLE "+SEARCH_PHOTO+" (photo_id NUMBER PRIMARY KEY, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.execute("CREATE TABLE animal_movement (move_id NUMBER PRIMARY KEY, animal_id NUMBER, geometry MDSYS.SDO_GEOMETRY, valid_from DATE, valid_to DATE)");
                con.commit();
                try{
                  stat.execute("INSERT INTO USER_SDO_GEOM_METADATA VALUES ('animal_movement','geometry',SDO_DIM_ARRAY(SDO_DIM_ELEMENT('LONGITUDE',-180,180,100),SDO_DIM_ELEMENT('LATITUDE',-90, 90,100)),8307)");
                }catch(SQLException e){}
		stat.execute("CREATE INDEX animal_movement_sidx ON animal_movement (geometry) indextype is MDSYS.SPATIAL_INDEX");
                stat.close();
                D.log("Preparing for creating sequences");
		createSequences();
                D.log("Preparing for creating triggers and procedures");
                createTriggersAndProcedures();
	}

        /**
         * Function will recreate database and will load example data
         * @throws SQLException
         * @throws IOException
         * @see #EXAMPLE_FILE
         * @see #PICTURE_FOLDER
         */
        public void fillDatabase() throws SQLException, IOException {
            createDatabase();
            executeSQLFile(EXAMPLE_FILE);
            uploadImage(1,ANIMAL_PHOTO,PICTURE_FOLDER+"1-1.jpeg");
            uploadImage(7,ANIMAL_PHOTO,PICTURE_FOLDER+"7-1.jpg");
        }

        /**
	 * SIMPLE function if needed to determine, if system is connected.
	 * @see #connect(java.lang.String, java.lang.String)
         * @see #disconnect()
	 * @return true if connection is alive, 0 if not connected
	 */
	public boolean isConnected() {

		boolean connected = false;

		if (con == null) {
			return false;
		} else {
			try {
				if (con.isValid(0)){
					connected = true;
				}
				else {
					connected = false;
				}
			} catch (SQLException e) {
				connected = false;
			}
		}

		if(connected){
			return true;
		}
		else {
			return false;
		}
	}

/////////Functions for queriing databse
//-------SELECT functions
        /**
         * Function for searching distance of nearest animal from current position
         * @param animal_id
         *          ID of an animal
         * @param location
         *          Location of an user
         * @return Distance in km (-1 if nothing found)
         * @throws SQLException
         */
        public Double getNearestAppareance(int animal_id,Point2D location) throws SQLException{
            Statement stat = con.createStatement();
            NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
            String SQLquery = "SELECT SDO_NN_DISTANCE(1) AS distance FROM animal_movement WHERE ROWNUM <= 1 AND animal_id="+
                    Integer.toString(animal_id)+" AND SDO_NN(geometry, SDO_GEOMETRY(2001,8307,SDO_POINT_TYPE("+nf.format(location.getX())+","+
                    nf.format(location.getY()) +",NULL),NULL,NULL),'UNIT=kilometer',1)='TRUE' ORDER BY distance";
            OracleResultSet rset = null;
            rset = (OracleResultSet) stat.executeQuery(T2SQL.temporal(T2SQL.T2SQLprefix()+SQLquery));
            Double result=-1.0;
            while (rset.next()) {
                result= rset.getDouble("distance");
                break;
            }
            rset.close();
            stat.close();
	    return result;
        }

        /**
         * Function determines appareance area
         * @param animal_id
         *          ID of an animal
         * @return Appareance in km2 (-1 if nothing found)
         * @throws SQLException
         */
        public Double getAppareanceArea(int animal_id) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "SELECT SUM(SDO_GEOM.SDO_AREA(geometry,0.1,'unit=SQ_KM')) AS area FROM animal_movement WHERE animal_id="+Integer.toString(animal_id);
            OracleResultSet rset = null;
            rset = (OracleResultSet) stat.executeQuery(T2SQL.temporal(T2SQL.T2SQLprefix()+SQLquery));
            Double result=-1.0;
            while (rset.next()) {
                result=result+rset.getDouble("area");
            }
            rset.close();
            stat.close();
	    return result;
        }

        /**
         * Function using for searching animals according their photos
         * @param filename
         *          filename of picture
         * @param tablename
         *          In which tablename we want to search
         * @see #ANIMAL_PHOTO
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #MAX_SEARCH_RESULTS
         * @see #searchResult
         * @throws SQLException
         * @throws IOException
         */
        public void searchAnimal(String filename, String tablename) throws SQLException, IOException{
            Statement stat = con.createStatement();
            int nextval=uploadImage(0,SEARCH_PHOTO,filename);
            String SQLquery = "SELECT DISTINCT animal.animal_id,animal.genus,animal.species,animal.genus_lat,animal.species_lat FROM "+
                    SEARCH_PHOTO+" fp, "+tablename+" photodb, animals animal "+
                    "WHERE ROWNUM <= "+Integer.toString(MAX_SEARCH_RESULTS)+" AND ordsys.IMGSimilar(fp.photo_sig, photodb.photo_sig, 'color=0.3, texture=0.3, shape=0.3, location=0.1',100 ,123)=1 "+
                    "AND photodb.photo_id="+Integer.toString(nextval)+" AND animal.animal_id=photodb.animal_id "+
                    "ORDER BY ordsys.IMGScore(123) ASC;";
            OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
            searchResult.clear();
            while (rset.next()) {
                Animal temp = new Animal();
                temp.setId(rset.getInt("animal_id"));
                temp.setSpecies(rset.getString("species"));
                temp.setSpeciesLat(rset.getString("species_lat"));
                temp.setGenus(rset.getString("genus"));
                temp.setGenusLat(rset.getString("genus_lat"));
                searchResult.add(temp);
            }
            rset.close();
            stat.executeQuery("DELETE FROM "+SEARCH_PHOTO+" WHERE photo_id="+Integer.toString(nextval));
            stat.close();
            return;
        }

	/**
	 * Function for searching animals by their id
	 *
	 * @see #searchResult
	 * @throws SQLException
	 */
	public void searchAnimals(int id) throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals WHERE animal_id = ?";
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
		opstmt.setInt(1,id);

		OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
		searchResult.clear();
		while (rset.next()) {
			Animal temp = new Animal();
			temp.setId(rset.getInt("animal_id"));
			temp.setSpecies(rset.getString("species"));
			temp.setSpeciesLat(rset.getString("species_lat"));
			temp.setGenus(rset.getString("genus"));
			temp.setGenusLat(rset.getString("genus_lat"));
			searchResult.add(temp);
		}
		rset.close();
		opstmt.close();
		return;
	}

	/**
	 * Function for searching animals by their names - choose one notation -
	 * latin or other language - do not blend! No need to fill both params,
	 * replace blank param by null or "".
	 * @see #MAX_SEARCH_RESULTS
         * @see #searchResult
	 * @param genus
	 *            genus name of an animal
	 * @param species
	 *            species name of an animal
	 * @throws SQLException
	 */
	public void searchAnimals(String genus, String species) throws SQLException {
                OraclePreparedStatement opstmt=null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals WHERE ROWNUM <= "+Integer.toString(MAX_SEARCH_RESULTS)+" AND ";
		if (genus == null ? "" == null : genus.equals("")) {
			if (species == null ? "" == null : species.equals("")) return;
			else {
				SQLquery=SQLquery+"LOWER(species) LIKE ? OR LOWER(species_lat) LIKE ?";
                                opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
                                opstmt.setString(1, "%"+species.toLowerCase()+"%");
                                opstmt.setString(2, "%"+species.toLowerCase()+"%");
			}
		} else {
			if (species == null ? "" == null : species.equals("")) {
				SQLquery=SQLquery + "LOWER(genus) LIKE ? OR LOWER(genus_lat) LIKE ?";
                                opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
                                opstmt.setString(1, "%"+genus.toLowerCase()+"%");
                                opstmt.setString(2, "%"+genus.toLowerCase()+"%");
			} else {
				SQLquery=SQLquery+ "(LOWER(species) LIKE ? AND LOWER(genus) LIKE ?) OR (LOWER(species_lat) LIKE ? AND LOWER(genus_lat) LIKE ?)";
                                opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
                                opstmt.setString(1, "%"+species.toLowerCase()+"%");
                                opstmt.setString(2, "%"+genus.toLowerCase()+"%");
                                opstmt.setString(3, "%"+species.toLowerCase()+"%");
                                opstmt.setString(4, "%"+genus.toLowerCase()+"%");
			}
		}
                OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
                searchResult.clear();
		while (rset.next()) {
                    Animal temp=new Animal();
                    temp.setId(rset.getInt("animal_id"));
                    temp.setSpecies(rset.getString("species"));
                    temp.setSpeciesLat(rset.getString("species_lat"));
                    temp.setGenus(rset.getString("genus"));
                    temp.setGenusLat(rset.getString("genus_lat"));
                    searchResult.add(temp);
		}
                rset.close();
		opstmt.close();
		return;
	}

                /**
         * searches animals in areas of an animal appareance
         * @param animal_id
         * @throws SQLException
         */
        public void searchAnimalsOnArea(int animal_id) throws SQLException {
                String SQLquery = T2SQL.T2SQLprefix()
                        +"SELECT geometry FROM animal_movement WHERE animal_id="+Integer.toString(animal_id);
                OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(T2SQL.temporal(SQLquery));
                OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
                searchResult.clear();
		while (rset.next()) {
                    try{
                  SQLquery = T2SQL.T2SQLprefix()
                        +"SELECT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat FROM animals a, animal_movement am WHERE a.animal_id=am.animal_id AND am.animal_id<>"
                        +Integer.toString(animal_id)+" AND SDO_GEOM.SDO_INTERSECTION(am.geometry,?,1) IS NOT NULL";
                  OraclePreparedStatement opstmt1 = (OraclePreparedStatement) con.prepareStatement(T2SQL.temporal(SQLquery));
                  opstmt1.setSTRUCT(1, rset.getSTRUCT("geometry"));
                  OracleResultSet rset1 = (OracleResultSet) opstmt1.executeQuery();
		  while (rset1.next()) {
                      Animal temp=new Animal();
                      temp.setId(rset1.getInt("a.animal_id"));
                      temp.setSpecies(rset1.getString("a.species"));
                      temp.setSpeciesLat(rset1.getString("a.species_lat"));
                      temp.setGenus(rset1.getString("a.genus"));
                      temp.setGenusLat(rset1.getString("a.genus_lat"));
                      if (searchResult.contains(temp)==false) searchResult.add(temp); //correct??
		  }
                  rset1.close();
                  opstmt1.close();
                  }catch(SQLException e){D.log("LEVEL2: "+e.getMessage());}
                }
                rset.close();
                opstmt.close();
		return;
	}

        /**
         * Function finds all animals in database
         * @throws SQLException
         */
        public void searchAnimals() throws SQLException, NullPointerException {
                OraclePreparedStatement opstmt=null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals";
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
                OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
                searchResult.clear();
		while (rset.next()) {
                    Animal temp=new Animal();
                    temp.setId(rset.getInt("animal_id"));
                    temp.setSpecies(rset.getString("species"));
                    temp.setSpeciesLat(rset.getString("species_lat"));
                    temp.setGenus(rset.getString("genus"));
                    temp.setGenusLat(rset.getString("genus_lat"));
                    searchResult.add(temp);
		}
                rset.close();
		opstmt.close();
		return;
	}

        /**
         * Searches animals according area size
         * @see #searchResult
         * @see T2SQL
         * @throws SQLException
         * @throws NullPointerException
         */
        public void searchAnimalsByAreaSize() throws SQLException, NullPointerException {
                OraclePreparedStatement opstmt=null;
		String SQLquery = T2SQL.T2SQLprefix()+"SELECT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat, SUM(SDO_GEOM.SDO_AREA(am.geometry,0.1,'unit=SQ_KM')) AS area FROM animals a, animal_movement am "
                        + "WHERE ROWNUM<="+MAX_SEARCH_RESULTS+" AND a.animal_id=am.animal_id GROUP BY a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat ORDER BY area DESC";
		opstmt = (OraclePreparedStatement) con.prepareStatement(T2SQL.temporal(SQLquery));
                OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
                searchResult.clear();
		while (rset.next()) {
                    Animal temp=new Animal();
                    temp.setId(rset.getInt("animal_id"));
                    temp.setSpecies(rset.getString("species"));
                    temp.setSpeciesLat(rset.getString("species_lat"));
                    temp.setGenus(rset.getString("genus"));
                    temp.setGenusLat(rset.getString("genus_lat"));
                    searchResult.add(temp);
		}
                rset.close();
		opstmt.close();
		return;
	}

        /**
         * Searches for nearest animals - result of searching is stored in searchResult
         * @see #MAX_SEARCH_RESULTS
         * @see #searchResult
         * @see T2SQL
         * @param location
         *          Current location of user
         * @throws SQLException
         */
        public void searchNearestAnimals(Point2D location) throws SQLException {
                OraclePreparedStatement opstmt=null;
                NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		String SQLquery = T2SQL.T2SQLprefix()
                        +"SELECT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat, MIN(SDO_NN_DISTANCE(1)) AS distance FROM animals a, animal_movement am WHERE ROWNUM <= "
                        +Integer.toString(MAX_SEARCH_RESULTS)+" AND a.animal_id=am.animal_id AND SDO_NN(am.geometry,SDO_GEOMETRY(2001,8307,SDO_POINT_TYPE("+nf.format(location.getX())
                        +","+nf.format(location.getY()) +",NULL),NULL,NULL),'UNIT=kilometer',1)='TRUE' GROUP BY a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat ORDER BY distance";
                opstmt = (OraclePreparedStatement) con.prepareStatement(T2SQL.temporal(SQLquery));
                OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
                searchResult.clear();
		while (rset.next()) {
                    Animal temp=new Animal();
                    temp.setId(rset.getInt("animal_id"));
                    temp.setSpecies(rset.getString("species"));
                    temp.setSpeciesLat(rset.getString("species_lat"));
                    temp.setGenus(rset.getString("genus"));
                    temp.setGenusLat(rset.getString("genus_lat"));
                    searchResult.add(temp);
		}
                rset.close();
		opstmt.close();
		return;
	}

        /**
         * Function for determine if animal is already in database
         * @param genus
         * @param species
         * @return true if animal already exists, false if doesn't
         * @throws SQLException
         */
        public boolean animalExists(String genus, String species) throws SQLException {
                OraclePreparedStatement opstmt=null;
		String SQLquery = "SELECT COUNT(*) FROM animals WHERE ";
		SQLquery=SQLquery+ "(LOWER(species) LIKE ? AND LOWER(genus) LIKE ?) OR (LOWER(species_lat) LIKE ? AND LOWER(genus_lat) LIKE ?)";
                opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
                opstmt.setString(1, species.toLowerCase());
                opstmt.setString(2, genus.toLowerCase());
                opstmt.setString(3, species.toLowerCase());
                opstmt.setString(4, genus.toLowerCase());
                OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
                int pocet=0;
		while (rset.next()) {
                    pocet=rset.getInt("COUNT(*)");
                }
                rset.close();
		opstmt.close();
                if (pocet==0) return false;
		else return true;
	}

        /**
	 * Selects a random thumbnail or all photos of an animal, it's used for an
	 * illustrating found results
	 * @see #ANIMAL_PHOTO
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
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
	public Map<Integer,OrdImage> selectPicture(int id, boolean all, String choosen_table) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery;
		if (all) {
			SQLquery = "SELECT photo, photo_id FROM "+choosen_table+" WHERE animal_id="
					+ Integer.toString(id);
		} else {
			SQLquery = "SELECT photo, photo_id FROM "+choosen_table+" WHERE ROWNUM <= 1 AND animal_id="
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
	 * Returns points which belongs to a current animal
         * <p>
         * http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14373/oracle/spatial/geometry/JGeometry.html
         * </p>
	 * @param animal_id
	 *            id of current animal
	 * @return HashMap<Integer,JGeometry> list of points belongs to current animal
	 * @throws SQLException
         * @see T2SQL
	 */
	public Map<Integer, JGeometry> selectAppareance(int animal_id)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = T2SQL.T2SQLprefix()+"SELECT move_id, geometry FROM animal_movement "+
                        "WHERE animal_id="+ Integer.toString(animal_id);
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(T2SQL.temporal(SQLquery));
		HashMap<Integer, JGeometry> data = new HashMap<Integer, JGeometry>();
		while (rset.next()) {
			data.put(rset.getInt("move_id"), JGeometry.load((oracle.sql.STRUCT)rset.getSTRUCT("geometry")));
		}
                rset.close();
		stat.close();
		return data;
	}

//----------UPDATE functions

        /**
         * Function for updating spatial data (invalidating old data and inserting and validating new data using stored procedure)
         * @param move_id
         *          ID of geometry record
         * @param animal_id
         *          ID of an animal
         * @param j_geom
         *          JGeometry object
         * @throws SQLException
         * @see T2SQL
         */
        public void updateAppareance(int move_id, JGeometry j_geom) throws SQLException{
            String SQLquery=T2SQL.T2SQLprefix()+"UPDATE geometry=? WHERE move_id=?";
            OraclePreparedStatement opstmt=(OraclePreparedStatement)con.prepareStatement(T2SQL.temporal(SQLquery));
            opstmt.setSTRUCT(1, JGeometry.store(j_geom, con));
            opstmt.setInt(2, move_id);
            opstmt.execute();
            opstmt.close();
        }

        /**
         * Function for updating selected animal
         * @param animal_id
         * @param genus
         * @param species
         * @param genus_lat
         * @param species_lat
         * @param description
         * @throws SQLException
         */
        public void updateAnimal(int animal_id, String genus, String species, String genus_lat, String species_lat, String description) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "UPDATE animals SET (genus=?, species=?, genus=?, genus_lat=?, decsription=?) WHERE animal_id=? ";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setString(1, genus);
            opstmt.setString(2, species);
            opstmt.setString(3, genus_lat);
            opstmt.setString(4, species_lat);
            opstmt.setString(5, description);
            opstmt.setInt(6, animal_id);
            opstmt.executeUpdate();
            opstmt.close();
            stat.close();
        }

        /**
         * Function for updating an animal
         * @param anima
         * @throws SQLException
         * @see Animal
         */
        public void updateAnimal(Animal anima) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "UPDATE animals SET genus=?, species=?, genus_lat=?, species_lat=?, description=? WHERE animal_id=? ";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setString(1, anima.getGenus());
            opstmt.setString(2, anima.getSpecies());
            opstmt.setString(3, anima.getGenusLat());
            opstmt.setString(4, anima.getSpeciesLat());
            opstmt.setString(5, anima.getDescription());
            opstmt.setInt(6, anima.getId());
            opstmt.executeUpdate();
            opstmt.close();
            stat.close();
        }

//---------DELETE functions

        /**
         * Function for deleting (invalidating) spatial object
         * @param move_id
         *          ID of a spatial object
         * @throws SQLException
         * @see T2SQL
         */
        public void deleteSpatialData(int move_id) throws SQLException{
            Statement stat = con.createStatement();
            stat.executeQuery(T2SQL.temporal(T2SQL.T2SQLprefix()+"DELETE FROM animal_movement WHERE move_id="+Integer.toString(move_id)));
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
         * Function for deleting picture from database
         * @param photo_id
         *          ID of a photo
         * @param table_name
         *          Name of a table we want to delete from
         * @throws SQLException
         * @see #ANIMAL_PHOTO
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #SEARCH_PHOTO
         * @see #uploadImage(int, java.lang.String, java.lang.String)
         */
        public void deletePicture(int photo_id, String table_name) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "DELETE FROM "+table_name+" WHERE photo_id=?";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setInt(1, photo_id);
            opstmt.execute();
            opstmt.close();
            stat.close();
        }

//----------INSERT functions

	/**
	 * Uploads image into choosen table
	 *
	 * @param animal_id
	 *            ID of an animal
	 * @param choosen_table
         *          table name we want to upload in
	 * @param filename
	 *            Filename of a picture for upload
         * @return integer with new photo_id
	 * @throws SQLException
	 * @throws IOException
         * @see #ANIMAL_PHOTO
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #SEARCH_PHOTO
         * @see #deletePicture(int, java.lang.String)
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
		SQLquery = "SELECT photo, photo_sig FROM " + choosen_table + " WHERE photo_id = " + Integer.toString(nextval) + " FOR UPDATE";
		rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		OrdImage imageProxy = (OrdImage) rset.getORAData("photo",OrdImage.getORADataFactory());
		OrdImageSignature signatureProxy = (OrdImageSignature) rset.getCustomDatum("photo_sig", OrdImageSignature.getFactory());
		rset.close();
		imageProxy.loadDataFromFile(filename);
                imageProxy.process("maxscale="+RESOLUTION+" fileformat=png");
		imageProxy.setProperties();
		signatureProxy.generateSignature(imageProxy);
		SQLquery = "UPDATE " + choosen_table + " SET photo=?, photo_sig=? WHERE photo_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
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
	 * Saves JGeometry into database
	 *
	 * @param animal_id
	 *            ID of concrete animal
	 * @param j_geom
	 *            JGeometry object
         * @throws SQLException
         * @see T2SQL
	 */
	public void insertAppareance(int animal_id, JGeometry j_geom) throws SQLException {
		int id = 0;
		con.setAutoCommit(false);
		Statement stat = con.createStatement();
		String SQLquery = ("SELECT animal_movement_seq.nextval FROM dual");
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		id = rset.getInt("nextval");
                SQLquery=T2SQL.T2SQLprefix()+"INSERT INTO animal_movement (move_id,animal_id,geometry) VALUES (?,?,?)";
                OraclePreparedStatement opstmt=(OraclePreparedStatement)con.prepareStatement(T2SQL.temporal(SQLquery));
                opstmt.setInt(1, id);
                opstmt.setInt(2, animal_id);
                opstmt.setSTRUCT(3, JGeometry.store(j_geom, con));
                opstmt.execute();
                opstmt.close();
		con.commit();
		con.setAutoCommit(true);
                rset.close();
		stat.close();
		return;
	}

        /**
         * Function for inserting new animals into database
         * @param genus
         * @param species
         * @param genus_lat
         * @param species_lat
         * @param description
         * @throws SQLException
         * @see #deleteAnimal(int)
         * @see #insertAnimal(cz.vutbr.fit.pdb03.Animal)
         * @see #updateAnimal(cz.vutbr.fit.pdb03.Animal)
         * @see #updateAnimal(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        public void insertAnimal(String genus, String species, String genus_lat, String species_lat, String description) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "INSERT INTO animals (genus, species, genus_lat, species_lat, description) VALUES (?,?,?,?,?) ";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setString(1, genus);
            opstmt.setString(2, species);
            opstmt.setString(3, genus_lat);
            opstmt.setString(4, species_lat);
            opstmt.setString(5, description);
            opstmt.execute();
            opstmt.close();
            stat.close();
        }

        /**
         * Alternative function for inserting animal into database
         * @param animal
         *          object Animal
         * @throws SQLException
         * @see Animal
         * @see #insertAnimal(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         * @see #deleteAnimal(int)
         * @see #updateAnimal(cz.vutbr.fit.pdb03.Animal)
         * @see #updateAnimal(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
         */
        public void insertAnimal(Animal animal) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "INSERT INTO animals (genus, species, genus_lat, species_lat, description) VALUES (?,?,?,?,?) ";
            OraclePreparedStatement opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
            opstmt.setString(1, animal.getGenus());
            opstmt.setString(2, animal.getSpecies());
            opstmt.setString(3, animal.getGenusLat());
            opstmt.setString(4, animal.getSpeciesLat());
            opstmt.setString(5, animal.getDescription());
            opstmt.execute();
            opstmt.close();
            stat.close();
        }

/////////PROTECTED FUNCTIONS////////////////////////////////////////////////////
        /**
         * Function for receiving animal description from database
         *
         * @param animal_id
         *           ID of an animal
         * @return String with description
         * @throws SQLException
         */
        protected String getDescription(int animal_id) throws SQLException{
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
/////////PRIVATE FUNCTIONS//////////////////////////////////////////////////////
	/**
	 * Function for deleting objects in database - important before creating a
	 * new database
	 *
	 * @throws SQLException
         * @see #createDatabase()
	 */
	private void deleteDatabase() throws SQLException {
            deleteIndex(ANIMAL_PHOTO);
            deleteIndex(FEET_PHOTO);
            deleteIndex(EXCREMENT_PHOTO);
            deleteIndex(SEARCH_PHOTO);
            Statement stat = con.createStatement();
            try {
            	stat.execute("DROP INDEX animal_movement_sidx FORCE;");
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP TABLE animals");
            } catch (SQLException e) {}
            try {
		stat.execute("DROP SEQUENCE animals_seq");
            } catch (SQLException e) {}
            try {
		stat.execute("DROP TABLE "+ANIMAL_PHOTO);
            } catch (SQLException e) {}
            try {
		stat.execute("DROP SEQUENCE "+ANIMAL_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
		stat.execute("DROP TABLE "+EXCREMENT_PHOTO);
            } catch (SQLException e) {}
            try {
		stat.execute("DROP SEQUENCE "+EXCREMENT_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP TABLE "+FEET_PHOTO);
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP SEQUENCE "+FEET_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP TABLE animal_movement");
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP SEQUENCE animal_movement_seq");
            } catch (SQLException e) {}
            try {
		stat.execute("DROP TABLE "+SEARCH_PHOTO);
            } catch (SQLException e) {}
            try {
		stat.execute("DROP SEQUENCE "+SEARCH_PHOTO+"_seq");
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP TRIGGER animals_trigger");
            } catch (SQLException e) {}
            try {
		stat.execute("DROP TRIGGER animal_movement_trigger_i");
            } catch (SQLException e) {}
            try {
            	stat.execute("DROP PROCEDURE animal_movement_delete");
            } catch (SQLException e) {}
            try {
		stat.execute("DROP PROCEDURE animal_movement_update");
            } catch (SQLException e) {}
            stat.close();
            con.commit();
	}

	/**
	 * Function for creating sequences in database - all starts with 1 and are
	 * incremented by 1
	 * @see #createDatabase()
         * @see #deleteDatabase()
	 * @throws SQLException
	 */
	private void createSequences() throws SQLException {
		Statement stat = con.createStatement();
		stat.execute("CREATE SEQUENCE animal_movement_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE "+FEET_PHOTO+"_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE "+EXCREMENT_PHOTO+"_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE "+ANIMAL_PHOTO+"_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE animals_seq START WITH 1 INCREMENT BY 1");
		stat.close();
	}

        /**
         * Function for creating stored procedures and triggers in database.
         * @throws SQLException
         * @see #createDatabase()
         * @see #deleteDatabase()
         */
        private void createTriggersAndProcedures() throws SQLException{
            Statement stat = con.createStatement();
            stat.execute("CREATE OR REPLACE TRIGGER animals_trigger "+
                              "BEFORE INSERT OR DELETE ON animals FOR EACH ROW "+
                              "BEGIN "+
                              "  IF INSERTING THEN "+
                              "    SELECT animals_seq.nextval INTO :NEW.animal_id FROM dual; "+
                              "  ELSIF DELETING THEN "+
                              "    DELETE FROM "+ANIMAL_PHOTO+" WHERE animal_id=:old.animal_id; "+
                              "    DELETE FROM "+EXCREMENT_PHOTO+" WHERE animal_id=:old.animal_id; "+
                              "    DELETE FROM "+FEET_PHOTO+" WHERE animal_id=:old.animal_id; "+
                              "    DELETE FROM animal_movement WHERE animal_id=:old.animal_id; "+
                              "  END IF; END;");
            stat.execute("CREATE OR REPLACE TRIGGER animal_movement_trigger_i "+
                              "BEFORE INSERT ON animal_movement FOR EACH ROW "+
                              "BEGIN"+
                              "  IF (:new.valid_from is NULL) THEN :NEW.valid_from:=sysdate; "+
                              "  END IF; END; ");
            stat.execute("CREATE OR REPLACE PROCEDURE animal_movement_delete( "+
                              "my_move_id IN	NUMBER, date_to IN  DATE, date_from IN  DATE) IS "+
                              "temp_date DATE; temp_geometry MDSYS.SDO_GEOMETRY;"+
                              "BEGIN "+
                              "  IF date_to=null AND date_from=null THEN UPDATE animal_movement SET valid_to=sysdate WHERE move_id=my_move_id; "+
                              "  ELSIF date_to=date_from THEN UPDATE animal_movement SET valid_to=date_to WHERE move_id=my_move_id; "+
                              "  ELSIF date_to>date_from THEN "+
                              "   SELECT date_to, geometry INTO temp_date, temp_geometry FROM animal_movement WHERE move_id=my_move_id;"+
                              "   IF date_to<temp_date THEN "+
                              "      INSERT INTO animal_movement (valid_from, valid_to, geometry) VALUES (date_to,temp_date,temp_geometry); "+
                              "    END IF; "+
                              "    UPDATE animal_movement SET valid_to=date_from WHERE move_id=my_move_id; "+
                              "  END IF; "+
                              "END animal_movement_delete; ");
            stat.execute("CREATE OR REPLACE PROCEDURE animal_movement_update("+
                    "new_move IN	MDSYS.SDO_GEOMETRY, "+
                    "old_move_id IN	NUMBER, "+
                    "new_from IN	DATE, "+
                    "new_to IN	DATE) IS "+
                    " new_move_id NUMBER; "+
                    " old_move MDSYS.SDO_GEOMETRY; "+
                    " old_animal_id NUMBER; "+
                    " old_from DATE; "+
                    " old_to DATE; "+
                    "BEGIN"+
                    "    SELECT geometry, animal_id, valid_from, valid_to INTO old_move, old_animal_id, old_from, old_to"+
                    "    FROM animal_movement WHERE move_id=old_move_id;"+
                    "    IF new_from=new_to THEN"+
                    "      IF new_from=NULL THEN"+
                    "        UPDATE animal_movement SET valid_from=NULL, valid_to=NULL, geometry=new_move WHERE move_id=old_move_id;"+
                    "      ELSIF new_from <= old_from THEN"+
                    "        UPDATE animal_movement SET valid_from=new_from, valid_to=null, geometry=new_move WHERE move_id=old_move_id;"+
                    "      ELSIF new_to >= old_to THEN"+
                    "        SELECT animal_movement_seq.nextval INTO new_move_id FROM dual;"+
                    "        INSERT INTO animal_movement (move_id,animal_id,valid_from, valid_to, geometry)"+
                    "        VALUES (new_move_id,old_animal_id, new_from, null,new_move);"+
                    "      ELSE"+
                    "        UPDATE animal_movement SET valid_to=new_from WHERE move_id=old_move_id;"+
                    "        SELECT animal_movement_seq.nextval INTO new_move_id FROM dual;"+
                    "        INSERT INTO animal_movement (move_id,animal_id,valid_from, valid_to, geometry)"+
                    "        VALUES (new_move_id,old_animal_id, new_from, null,new_move);"+
                    "      END IF;"+
                    "    ELSIF new_from<new_to THEN"+
                    "     IF new_from<=old_from AND new_to>old_from THEN"+
                    "        IF new_to>=old_to THEN"+
                    "          UPDATE animal_movement SET valid_from=new_from, valid_to=new_to, geometry=new_move WHERE move_id=old_move_id;"+
                    "        ELSE"+
                    "          UPDATE animal_movement SET valid_from=new_to WHERE move_id=old_move_id;"+
                    "          SELECT animal_movement_seq.nextval INTO new_move_id FROM dual;"+
                    "          INSERT INTO animal_movement (move_id,animal_id,valid_from, valid_to, geometry)"+
                    "          VALUES (new_move_id,old_animal_id, new_from, new_to,new_move);"+
                    "        END IF;"+
                    "      ELSIF new_to>=old_to AND new_from<old_to THEN"+
                    "        IF new_from<=old_to THEN"+
                    "          UPDATE animal_movement SET valid_from=new_from, valid_to=new_to, geometry=new_move WHERE move_id=old_move_id;"+
                    "        ELSE UPDATE animal_movement SET valid_to=new_from WHERE move_id=old_move_id;"+
                    "          SELECT animal_movement_seq.nextval INTO new_move_id FROM dual;"+
                    "          INSERT INTO animal_movement (move_id,animal_id,valid_from, valid_to, geometry)"+
                    "          VALUES (new_move_id,old_animal_id, new_from, new_to,new_move);"+
                    "        END IF;"+
                    "      ELSE"+
                    "        SELECT animal_movement_seq.nextval INTO new_move_id FROM dual;"+
                    "        INSERT INTO animal_movement (move_id,animal_id,valid_from, valid_to, geometry)"+
                    "        VALUES (new_move_id,old_animal_id, new_to, old_to,old_move);"+
                    "        UPDATE animal_movement SET valid_to=new_from WHERE move_id=old_move_id;"+
                    "        SELECT animal_movement_seq.nextval INTO new_move_id FROM dual;"+
                    "        INSERT INTO animal_movement (move_id,animal_id,valid_from, valid_to, geometry)"+
                    "        VALUES (new_move_id,old_animal_id, new_from, new_to,new_move);"+
                    "      END IF;"+
                    "    END IF; "+
                    "END animal_movement_update;");
            stat.close();
        }

        /**
         * Function for creating picture index
         * @param tablename
         * @see #createDatabase()
         * @see #deleteDatabase()
         * @see #ANIMAL_PHOTO
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #SEARCH_PHOTO
         */
        private void createIndex(String tablename){
        try {
            Statement stat = con.createStatement();
            stat.execute("CREATE INDEX " + tablename + "_idx ON " + tablename + " (photo_sig) INDEXTYPE IS ordsys.ordimageindex;");
            stat.close();
        } catch (SQLException ex) {
        }
        }

        /**
         * Function for deleting picture index
         * @param tablename
         * @see #createDatabase()
         * @see #createIndex(java.lang.String)
         * @see #deleteDatabase()
         * @see #ANIMAL_PHOTO
         * @see #EXCREMENT_PHOTO
         * @see #FEET_PHOTO
         * @see #SEARCH_PHOTO
         */
        private void deleteIndex(String tablename){
        try {
            Statement stat = con.createStatement();
            stat.execute("DROP INDEX " + tablename + "_idx FORCE;");
            stat.close();
        } catch (SQLException ex) {}
        }
        
        /**
         * Fills dabase with example data
         * @param filename
         *          filename of SQL file
         * @throws SQLException
         * @throws FileNotFoundException
         * @throws IOException
         * @throws UnsupportedEncodingException
         */
        private void executeSQLFile(String filename) throws SQLException, IOException {
          String s = new String();
          StringBuilder sb = new StringBuilder();
          createDatabase();
          BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "windows-1250"));
          while((s = br.readLine()) != null){ sb.append(s); }
          br.close();
          String[] inst = sb.toString().split(";");
          Statement st = con.createStatement();
          for(int i = 0; i<inst.length; i++){
            if(!inst[i].trim().equals("")) {
              st.executeUpdate(inst[i]);
              //System.out.println(">>"+inst[i]); //TODO delete
            }
          }
        }


}
