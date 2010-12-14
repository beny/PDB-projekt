package cz.vutbr.fit.pdb03;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.ord.im.OrdImage;
import oracle.ord.im.OrdImageSignature;
import oracle.spatial.geometry.JGeometry;
import cz.vutbr.fit.pdb03.gui.JEntity;
import cz.vutbr.fit.pdb03.gui.JPicture;

/**
 * Knihovna pro práci s databází
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public class DataBase {

	/**
	 * private string to store url of database connection
	 */
	private final static String connectionString = "@berta.fit.vutbr.cz:1521:stud";
	private final static  String RESOLUTION = "400 400";
	private final static String EXAMPLE_FILE = "src" + File.separator + "cz"
			+ File.separator + "vutbr" + File.separator + "fit"
			+ File.separator + "pdb03" + File.separator + "example"
			+ File.separator + "example.sql";
	private final static String PICTURE_FOLDER = "src" + File.separator + "cz"
			+ File.separator + "vutbr" + File.separator + "fit"
			+ File.separator + "pdb03" + File.separator + "example"
			+ File.separator + "";

        /**
         * Maximální délka názvu
         */
	public final static int MAX_STRING = 25;
        /**
         * Maximální délka popisu
         */
	public final static int MAX_LONG_STRING = 1500;
        /**
         * SRID prostorových dat - geo souřadnice
         */
	public final static int SRID = 8307;
	public final static int DIMENSIONS = 2;

	/**
	 * private variable to store database connection
	 */
	private Connection con = null;

	/**
	 * Název tabulky s fotografiemi zvířat
	 *
	 * @see #deletePicture(int, java.lang.String)
	 * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String, int, java.lang.String)
	 * @see #createIndex(java.lang.String)
	 * @see #searchAnimal(java.lang.String, java.lang.String)
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #SEARCH_PHOTO
	 */
	public final static String ANIMAL_PHOTO = "animal_photo";

	/**
	 * Název tabulky s fotkami trusu
	 *
	 * @see #deletePicture(int, java.lang.String)
	 * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String, int, java.lang.String)
	 * @see #createIndex(java.lang.String)
	 * @see #ANIMAL_PHOTO
	 * @see #FEET_PHOTO
	 * @see #SEARCH_PHOTO
	 */
	public final static String EXCREMENT_PHOTO = "excrement_photo";

	/**
	 * Název tabulky s obrázkami stop
	 *
	 * @see #deletePicture(int, java.lang.String)
	 * @see #deleteIndex(java.lang.String)
         * @see #uploadImage(int, java.lang.String, java.lang.String, int, java.lang.String)
	 * @see #createIndex(java.lang.String)
	 * @see #EXCREMENT_PHOTO
	 * @see #ANIMAL_PHOTO
	 * @see #SEARCH_PHOTO
	 */
	public final static String FEET_PHOTO = "footprint";

	/**
	 * Název tabulky s fotkami, ve kterých chceme hledat
	 *
	 * @see #deletePicture(int, java.lang.String)
	 * @see #deleteIndex(java.lang.String)
	 * @see #uploadImage(int, java.lang.String, java.lang.String, int, java.lang.String)
	 * @see #createIndex(java.lang.String)
	 * @see #searchAnimal(java.lang.String, java.lang.String)
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #ANIMAL_PHOTO
	 */
	public final static String SEARCH_PHOTO = "search_photo";

	/**
	 * Number of maximum search results
	 *
	 * @see #searchResult
	 * @see #searchAnimal(java.lang.String, java.lang.String)
	 * @see #searchAnimals(java.lang.String, java.lang.String)
	 * @see #searchNearestAnimals(java.awt.geom.Point2D)
	 */
	private final static int MAX_SEARCH_RESULTS = 50;

	/**
	 * Kolekce pro uložení výsledků hledání
	 *
	 * @see #MAX_SEARCH_RESULTS
	 * @see #searchAnimals()
         * @see #searchAnimals(java.lang.String)
         * @see #searchAnimals(int)
         * @see #searchAnimalsByAreaSize()
         * @see #searchAnimalsByPicture(java.lang.String)
         * @see #searchAnimalsByPicture(java.lang.String, java.lang.String)
         * @see #searchExtinctAnimals()
         * @see #searchNearestAnimals(java.awt.geom.Point2D)
	 * @see #searchAnimals(java.lang.String, java.lang.String)
	 * @see #searchNearestAnimals(java.awt.geom.Point2D)
	 */
	public Collection<Animal> searchResult = new ArrayList<Animal>();

	/**
	 * Funkce pro připojení Oracle databáze
	 *
	 * @param username
	 *            uživatelské jméno
	 * @param password
	 *            heslo
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
	 * Funkce pro odpojení databáze
	 *
	 * @see #connect(java.lang.String, java.lang.String)
	 * @see #isConnected()
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException {
		if (isConnected())
			con.close();
		searchResult.clear();
	}

	/**
	 * Funkce pro (znovu)vytvoření databáze
	 *
	 * @see #deleteDatabase()
	 * @see #createIndex(java.lang.String)
	 * @see #createSequences()
	 * @see #createTriggersAndProcedures()
	 * @throws SQLException
	 */
	public void createDatabase() throws SQLException {
		deleteDatabase();
		Log.debug("Database deleted!");
		Statement stat = con.createStatement();
		stat.execute("CREATE TABLE animals (animal_id NUMBER PRIMARY KEY, genus VARCHAR("
				+ MAX_STRING
				+ "), species VARCHAR("
				+ MAX_STRING
				+ "), genus_lat VARCHAR("
				+ MAX_STRING
				+ "), species_lat VARCHAR("
				+ MAX_STRING
				+ "), description VARCHAR(" + MAX_LONG_STRING + "))");
		con.commit();
		stat.execute("CREATE TABLE "
				+ ANIMAL_PHOTO
				+ " (photo_id NUMBER PRIMARY KEY, animal_id NUMBER REFERENCES animals(animal_id) ON DELETE CASCADE, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature, move_id NUMBER, description VARCHAR("
				+ MAX_LONG_STRING + "))");
		stat.execute("CREATE TABLE "
				+ EXCREMENT_PHOTO
				+ " (photo_id NUMBER PRIMARY KEY, animal_id NUMBER REFERENCES animals(animal_id) ON DELETE CASCADE, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature, move_id NUMBER, description VARCHAR("
				+ MAX_LONG_STRING + "))");
		stat.execute("CREATE TABLE "
				+ FEET_PHOTO
				+ " (photo_id NUMBER PRIMARY KEY, animal_id NUMBER REFERENCES animals(animal_id) ON DELETE CASCADE, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature, move_id NUMBER, description VARCHAR("
				+ MAX_LONG_STRING + "))");
		stat.execute("CREATE TABLE "
				+ SEARCH_PHOTO
				+ " (photo_id NUMBER PRIMARY KEY, photo ORDSYS.ORDImage, photo_sig ORDSYS.ORDImageSignature)");
		stat.execute("CREATE TABLE animal_movement (move_id NUMBER PRIMARY KEY, animal_id NUMBER REFERENCES animals(animal_id) ON DELETE CASCADE, geometry MDSYS.SDO_GEOMETRY, valid_from DATE, valid_to DATE)");
		con.commit();
		try {
			stat.execute("INSERT INTO USER_SDO_GEOM_METADATA VALUES ('animal_movement','geometry',SDO_DIM_ARRAY(SDO_DIM_ELEMENT('LONGITUDE',-180,180,0.5),SDO_DIM_ELEMENT('LATITUDE',-90, 90,0.5)),8307)");
                        con.commit();
		} catch (SQLException e) {}
		stat.execute("CREATE INDEX animal_movement_sidx ON animal_movement (geometry) indextype is MDSYS.SPATIAL_INDEX");
		stat.close();
		createSequences();
		createTriggersAndProcedures();
	}

	/**
	 * Funkce pro (znovu)vytvoření databáze a nahrání vzorových dat
	 *
	 * @throws SQLException
	 * @throws IOException
	 * @see #EXAMPLE_FILE
	 * @see #PICTURE_FOLDER
	 */
	public void fillDatabase() throws SQLException, IOException {
		createDatabase();
		Log.debug("Loading sql file: " + EXAMPLE_FILE);
		executeSQLFile(EXAMPLE_FILE);
		Log.debug("Uploading example images...");
		uploadImage(1, ANIMAL_PHOTO, PICTURE_FOLDER + "1-1.jpeg", 0,
				"Obrázek žirafy");
		uploadImage(1, ANIMAL_PHOTO, PICTURE_FOLDER + "1-2.jpg", 0,
				"Obrázek žirafy");
		uploadImage(1, ANIMAL_PHOTO, PICTURE_FOLDER + "1-3.jpg", 0,
				"Obrázek žirafy");
		uploadImage(1, ANIMAL_PHOTO, PICTURE_FOLDER + "1-4.jpg", 0,
				"Obrázek žirafy");
		uploadImage(1, ANIMAL_PHOTO, PICTURE_FOLDER + "1-5.jpg", 0,
				"Obrázek žirafy");
		uploadImage(2, ANIMAL_PHOTO, PICTURE_FOLDER + "2-1.jpg", 0,
				"Obrázek zebry");
		uploadImage(2, ANIMAL_PHOTO, PICTURE_FOLDER + "2-2.jpg", 0,
				"Obrázek zebry");
		uploadImage(2, ANIMAL_PHOTO, PICTURE_FOLDER + "2-3.jpg", 0,
				"Obrázek zebry");
		uploadImage(3, ANIMAL_PHOTO, PICTURE_FOLDER + "3-1.jpg", 0,
				"Stojící lev");
		uploadImage(3, ANIMAL_PHOTO, PICTURE_FOLDER + "3-2.jpg", 0,
				"Ležící lev");
		uploadImage(3, ANIMAL_PHOTO, PICTURE_FOLDER + "3-3.jpg", 0,
				"Ležící lvice");
		uploadImage(4, ANIMAL_PHOTO, PICTURE_FOLDER + "4-1.jpg", 0,
				"Obrázek hrocha");
		uploadImage(4, ANIMAL_PHOTO, PICTURE_FOLDER + "4-2.jpg", 0,
				"Zívající hroch");
		uploadImage(4, ANIMAL_PHOTO, PICTURE_FOLDER + "4-3.jpg", 0,
				"Obrázek hrocha");
		uploadImage(5, ANIMAL_PHOTO, PICTURE_FOLDER + "5-1.jpg", 0,
				"Ležící tygr");
		uploadImage(5, ANIMAL_PHOTO, PICTURE_FOLDER + "5-2.jpg", 0,
				"Pijící tygr");
		uploadImage(5, ANIMAL_PHOTO, PICTURE_FOLDER + "5-3.jpg", 0,
				"Stojící tygr");
		uploadImage(5, ANIMAL_PHOTO, PICTURE_FOLDER + "5-4.jpg", 0,
				"Potápějící se tygr");
		uploadImage(5, ANIMAL_PHOTO, PICTURE_FOLDER + "5-5.jpg", 0,
				"Ležící tygr");
		uploadImage(6, ANIMAL_PHOTO, PICTURE_FOLDER + "6-1.jpg", 0,
				"Ilustrace smilodona");
		uploadImage(7, ANIMAL_PHOTO, PICTURE_FOLDER + "7-1.jpg", 0,
				"Sedící gibon");
		uploadImage(7, ANIMAL_PHOTO, PICTURE_FOLDER + "7-2.jpg", 0,
				"Sedící gibon");
		uploadImage(7, ANIMAL_PHOTO, PICTURE_FOLDER + "7-3.jpg", 0,
				"Sedící gibon");
		uploadImage(8, ANIMAL_PHOTO, PICTURE_FOLDER + "8-1.jpg", 0,
				"Sedící šimpanz");
		uploadImage(8, ANIMAL_PHOTO, PICTURE_FOLDER + "8-2.jpg", 0,
				"Sedící šimpanz");
		uploadImage(8, ANIMAL_PHOTO, PICTURE_FOLDER + "8-3.jpg", 0,
				"Sedící šimpanz");
		uploadImage(8, ANIMAL_PHOTO, PICTURE_FOLDER + "8-4.jpg", 0,
				"Válející se šimpanz");
		uploadImage(9, ANIMAL_PHOTO, PICTURE_FOLDER + "9-1.jpg", 0,
				"Obrázek žraloka");
		uploadImage(9, ANIMAL_PHOTO, PICTURE_FOLDER + "9-2.jpg", 0,
				"Obrázek žraloka");
		uploadImage(9, ANIMAL_PHOTO, PICTURE_FOLDER + "9-3.jpg", 0,
				"Obrázek žraloka");
		uploadImage(10, ANIMAL_PHOTO, PICTURE_FOLDER + "10-1.jpg", 0,
				"Ilustrace lososa");
		uploadImage(10, ANIMAL_PHOTO, PICTURE_FOLDER + "10-2.jpg", 0,
				"Ilustrace lososa");
		uploadImage(10, ANIMAL_PHOTO, PICTURE_FOLDER + "10-3.jpeg", 0,
				"Obrázek lososa");
		uploadImage(11, ANIMAL_PHOTO, PICTURE_FOLDER + "11-1.jpg", 0,
				"Obrázek kapra");
		uploadImage(11, ANIMAL_PHOTO, PICTURE_FOLDER + "11-2.jpg", 0,
				"Obrázek kapra");
                uploadImage(12, ANIMAL_PHOTO, PICTURE_FOLDER + "12-1.jpg", 0,
				"Ilustrace vyhynulé zebry");

		uploadImage(1, FEET_PHOTO, PICTURE_FOLDER + "1-1f.jpg", 0,
				"Stopa žirafy");
		uploadImage(1, FEET_PHOTO, PICTURE_FOLDER + "1-2f.jpg", 0,
				"Stopa žirafy");
		uploadImage(1, FEET_PHOTO, PICTURE_FOLDER + "1-3f.jpg", 0,
				"Stopa žirafy");
		uploadImage(1, FEET_PHOTO, PICTURE_FOLDER + "1-4f.jpg", 0,
				"Stopa žirafy");
		uploadImage(2, FEET_PHOTO, PICTURE_FOLDER + "2-1f.jpg", 0,
				"Stopa zebry");
                uploadImage(2, FEET_PHOTO, PICTURE_FOLDER + "2-2f.jpg", 0,
				"Stopa zebry v písku");
		uploadImage(3, FEET_PHOTO, PICTURE_FOLDER + "3-1f.jpg", 0, "Stopa lva");
		uploadImage(3, FEET_PHOTO, PICTURE_FOLDER + "3-2f.png", 0, "Stopa lva");
		uploadImage(3, FEET_PHOTO, PICTURE_FOLDER + "3-3f.jpg", 0, "Stopa lva");
		uploadImage(4, FEET_PHOTO, PICTURE_FOLDER + "4-1f.jpg", 0,
				"Stopa hrocha");
		uploadImage(4, FEET_PHOTO, PICTURE_FOLDER + "4-2f.jpg", 0,
				"Stopa hrocha");
		uploadImage(5, FEET_PHOTO, PICTURE_FOLDER + "5-1f.jpg", 0,
				"Stopa tygra");
		uploadImage(5, FEET_PHOTO, PICTURE_FOLDER + "5-2f.jpg", 0,
				"Stopa tygra");
		uploadImage(8, FEET_PHOTO, PICTURE_FOLDER + "8-1f.jpg", 0,
				"Stopa šimpanze");

		uploadImage(1, EXCREMENT_PHOTO, PICTURE_FOLDER + "1-1e.jpg", 0,
				"Trus žirafy");
		Log.debug("Example images uploaded.");

	}

	/**
	 * Funkce zjistí, zda, je připojení aktivní
	 *
	 * @see #connect(java.lang.String, java.lang.String)
	 * @see #disconnect()
	 * @return true pokud připojeno, false pokud nepřipojeno
	 */
	public boolean isConnected() {
		boolean connected = false;
		if (con == null) {
			return false;
		} else {
			try {
				if (con.isValid(0)) {
					connected = true;
				} else {
					connected = false;
				}
			} catch (SQLException e) {
				connected = false;
			}
		}
		if (connected) {
			return true;
		} else {
			return false;
		}
	}

	// Functions for queriing databse
	/**
	 * Funkce pro nalezení vzdálenosti od nejbližšího výskytu zvířete
	 *
	 * @param animal_id
	 *            ID zvířete
	 * @param location
	 *            Poloha uživatele
	 * @return Vzdálenost v km (-1 pokud výskyt není)
	 * @throws SQLException
	 */
	public Double getNearestAppareance(int animal_id, Point2D location)
			throws SQLException {
		Statement stat = con.createStatement();
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		String SQLquery = "SELECT SDO_GEOM.SDO_DISTANCE(geometry, SDO_GEOMETRY(2001,8307,SDO_POINT_TYPE("
				+ nf.format(location.getX())
				+ ","
				+ nf.format(location.getY())
				+ ",NULL),NULL,NULL),1,'UNIT=kilometer') AS distance FROM animal_movement WHERE animal_id="
				+ Integer.toString(animal_id)
                                + " ORDER BY distance ASC";
		OracleResultSet rset = null;
		rset = (OracleResultSet) stat.executeQuery(T2SQL.temporal(T2SQL
				.T2SQLprefix() + SQLquery));
		Double result = -1.0;
		while (rset.next()) {
			result = rset.getDouble("distance");
			break;
		}
		rset.close();
		stat.close();
		return result;
	}

	/**
	 * Funkce zjistí rozlohu území obývaného zvířetem
	 *
	 * @param animal_id
	 *            ID zvířete
	 * @return Rozloha v km2 (-1 pokud nic není)
	 * @throws SQLException
	 */
	public Double getAppareanceArea(int animal_id) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "SELECT SUM(SDO_GEOM.SDO_AREA(geometry,0.1,'unit=SQ_KM')) AS area FROM animal_movement WHERE animal_id="
				+ Integer.toString(animal_id);
		OracleResultSet rset = null;
		rset = (OracleResultSet) stat.executeQuery(T2SQL.temporal(T2SQL
				.T2SQLprefix() + SQLquery));
		Double result = -1.0;
		while (rset.next()) {
			result = result + rset.getDouble("area");
		}
		rset.close();
		stat.close();
                //Log.debug(Double.toString(result)+" - "+Double.toString(getIntersectionsArea(animal_id)));
		return result-getIntersectionsArea(animal_id);
	}

	/**
	 * Funkce pro nalezení zvířete dle fotky
	 *
	 * @param filename
	 *            cesta k souboru
	 * @param tablename
	 *            název tabulky ve které hledat
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #MAX_SEARCH_RESULTS
	 * @see #searchResult
	 * @throws SQLException
	 * @throws IOException
	 */
	public void searchAnimalsByPicture(String filename, String tablename)
			throws SQLException, IOException {
		Statement stat = con.createStatement();
		int nextval = uploadImage(0, SEARCH_PHOTO, filename, 0, "");
		String SQLquery = "SELECT * FROM (SELECT DISTINCT animal.animal_id,animal.genus,animal.species,animal.genus_lat,animal.species_lat,MIN(ordsys.IMGScore(123)) AS shoda FROM "
				+ SEARCH_PHOTO
				+ " fp, "
				+ tablename
				+ " photodb, animals animal "
				+ "WHERE ordsys.IMGSimilar(fp.photo_sig, photodb.photo_sig, ";
                if (tablename.equals(ANIMAL_PHOTO)) SQLquery+= "'color=0.275, texture=0.7, shape=0.025',20";
                else if (tablename.equals(FEET_PHOTO)) SQLquery+= "'color=0.2, texture=0.0, shape=0.8, location=0.0',10";
                else if (tablename.equals(EXCREMENT_PHOTO)) SQLquery+= "'color=0.3, texture=0.3, shape=0.3, location=0.1',30";
                else return;
		SQLquery=SQLquery + " ,123)=1 AND fp.photo_id="
                        + Integer.toString(nextval)
                        + " AND animal.animal_id=photodb.animal_id "
                        + "GROUP BY animal.animal_id,animal.genus,animal.species,animal.genus_lat,animal.species_lat "
                        + "ORDER BY shoda ASC) WHERE ROWNUM <= " + Integer.toString(MAX_SEARCH_RESULTS);
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		searchResult.clear();
		while (rset.next()) {
			Animal temp = new Animal();
			temp.setId(rset.getInt("animal_id"));
			temp.setSpecies(rset.getString("species"));
			temp.setSpeciesLat(rset.getString("species_lat"));
			temp.setGenus(rset.getString("genus"));
			temp.setGenusLat(rset.getString("genus_lat"));
                        Log.debug(temp.getGenus()+" - "+rset.getString("shoda"));
			searchResult.add(temp);
		}
		rset.close();
		stat.executeQuery("DELETE FROM " + SEARCH_PHOTO + " WHERE photo_id="
				+ Integer.toString(nextval));
		stat.close();
		return;
	}

	/**
	 * Funkce pro nalezení zvířete dle id
	 *
	 * @see #searchResult
	 * @throws SQLException
	 */
	public void searchAnimals(int id) throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals WHERE animal_id = ?";
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
		opstmt.setInt(1, id);

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
	 * Funkce pro nalezení zvířete dle jména. Netřeba zadávat obě políčka.
         * Nemíchat české a latinské názvy - vybrat jen jednu možnost.
	 *
	 * @see #MAX_SEARCH_RESULTS
	 * @see #searchResult
	 * @param genus
	 *            rodové jméno
	 * @param species
	 *            druhové jméno
	 * @throws SQLException
	 */
	public void searchAnimals(String genus, String species) throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals "
                        + "WHERE ROWNUM <= " + Integer.toString(MAX_SEARCH_RESULTS) + " AND ";
		if (genus == null ? "" == null : genus.equals("")) {
			if (species == null ? "" == null : species.equals(""))
				return;
			else {
				SQLquery = SQLquery
						+ "LOWER(species) LIKE ? OR LOWER(species_lat) LIKE ?";
				opstmt = (OraclePreparedStatement) con
						.prepareStatement(SQLquery);
				opstmt.setString(1, "%" + species.toLowerCase() + "%");
				opstmt.setString(2, "%" + species.toLowerCase() + "%");
			}
		} else {
			if (species == null ? "" == null : species.equals("")) {
				SQLquery = SQLquery
						+ "LOWER(genus) LIKE ? OR LOWER(genus_lat) LIKE ?";
				opstmt = (OraclePreparedStatement) con
						.prepareStatement(SQLquery);
				opstmt.setString(1, "%" + genus.toLowerCase() + "%");
				opstmt.setString(2, "%" + genus.toLowerCase() + "%");
			} else {
				SQLquery = SQLquery
						+ "(LOWER(species) LIKE ? AND LOWER(genus) LIKE ?) OR (LOWER(species_lat) LIKE ? AND LOWER(genus_lat) LIKE ?)";
				opstmt = (OraclePreparedStatement) con
						.prepareStatement(SQLquery);
				opstmt.setString(1, "%" + species.toLowerCase() + "%");
				opstmt.setString(2, "%" + genus.toLowerCase() + "%");
				opstmt.setString(3, "%" + species.toLowerCase() + "%");
				opstmt.setString(4, "%" + genus.toLowerCase() + "%");
			}
		}
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
	 * Funkce pro hledání zvířat dle popisu
	 *
	 * @see #MAX_SEARCH_RESULTS
	 * @see #searchResult
	 * @param description
	 *            řetězec, jehož část je v popisu zvířete
	 * @throws SQLException
	 */
	public void searchAnimals(String description) throws SQLException {
		if (description == null ? "" == null : description.equals("")) {
			searchResult.clear();
			return;
		}
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals WHERE ROWNUM <= "
				+ Integer.toString(MAX_SEARCH_RESULTS) + " AND (LOWER(description) LIKE ?)";
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
		opstmt.setString(1, "%" + description.toLowerCase() + "%");
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
	 * Funkce pro hledání zvířat dle popisu fotky
	 *
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #MAX_SEARCH_RESULTS
	 * @param description
	 *            řetězec, jehož část je v popisku fotky zvířete
	 * @throws SQLException
	 */
	public void searchAnimalsByPicture(String description) throws SQLException {
		if (description == null ? "" == null : description.equals("")) {
			searchResult.clear();
			return;
		}
		searchResult.clear();
		OraclePreparedStatement opstmt = null;
                String SQLquery = "SELECT * FROM (SELECT DISTINCT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat FROM animals a, "
				+ ANIMAL_PHOTO + " p1, "+ FEET_PHOTO + " p2, "+ EXCREMENT_PHOTO
                                + " p3 WHERE ((a.animal_id=p1.animal_id AND LOWER(p1.description) LIKE ?) "
                                + "OR (a.animal_id=p2.animal_id AND LOWER(p2.description) LIKE ?) "
                                + "OR (a.animal_id=p3.animal_id AND LOWER(p3.description) LIKE ?))) WHERE ROWNUM <="
                                + Integer.toString(MAX_SEARCH_RESULTS);
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
		opstmt.setString(1, "%" + description.toLowerCase() + "%");
                opstmt.setString(2, "%" + description.toLowerCase() + "%");
                opstmt.setString(3, "%" + description.toLowerCase() + "%");
		OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
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
	 * Funkce nalezne zvířata vyskytující se
         * na stejném území jako zvolené zvíře
	 *
	 * @param animal_id
         *          ID zvířete
	 * @throws SQLException
	 */
	public void searchAnimalsOnArea(int animal_id) throws SQLException {
		String SQLquery = T2SQL.T2SQLprefix()
				+ "SELECT geometry FROM animal_movement WHERE animal_id="
				+ Integer.toString(animal_id);
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(T2SQL.temporal(SQLquery));
		OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
		searchResult.clear();
                OraclePreparedStatement opstmt1;
                OracleResultSet rset1;
		while (rset.next()) {
			try {
				SQLquery = T2SQL.T2SQLprefix()
						+ "SELECT DISTINCT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat FROM animals a, animal_movement am WHERE a.animal_id=am.animal_id AND am.animal_id<>"
						+ Integer.toString(animal_id)
						+ " AND SDO_GEOM.SDO_INTERSECTION(am.geometry,?,1) IS NOT NULL";
				opstmt1 = (OraclePreparedStatement) con
						.prepareStatement(T2SQL.temporal(SQLquery));
				opstmt1.setSTRUCT(1, rset.getSTRUCT("geometry"));
				rset1 = (OracleResultSet) opstmt1.executeQuery();
				while (rset1.next()) {
					Animal temp = new Animal();
					temp.setId(rset1.getInt("animal_id"));
					temp.setSpecies(rset1.getString("species"));
					temp.setSpeciesLat(rset1.getString("species_lat"));
					temp.setGenus(rset1.getString("genus"));
					temp.setGenusLat(rset1.getString("genus_lat"));
					if (searchResult.contains(temp) == false)
						searchResult.add(temp); // correct??
				}
				rset1.close();
				opstmt1.close();
			} catch (SQLException e) {
				Log.error("Database: " + e.getMessage());
			}
		}
		rset.close();
		opstmt.close();
		return;
	}

	/**
	 * Funkce najde všechna zvířata v databázi
         *
         * @see #searchResult
	 *
	 * @throws SQLException
	 */
	public void searchAnimals() throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT animal_id,genus,species,genus_lat,species_lat FROM animals ORDER BY genus, genus_lat";
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
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
	 * Funkce najde zvířata s největší rozlohou výskytu a seřadí je
	 * TODO: ošetřit překrývající se polygony,rozloha úsečky
	 * @see #searchResult
	 * @see T2SQL
	 * @throws SQLException
	 */
	public void searchAnimalsByAreaSize() throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = T2SQL.T2SQLprefix()
				+ "SELECT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat, SUM(SDO_GEOM.SDO_AREA(am.geometry,0.1,'unit=SQ_KM')) AS area "
				+ "FROM animals a, animal_movement am "
				+ "WHERE a.animal_id=am.animal_id "
				+ "GROUP BY a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat "
				+ "ORDER BY area DESC";
		opstmt = (OraclePreparedStatement) con.prepareStatement("SELECT * FROM ("
                        +T2SQL.temporal(SQLquery)+ ") WHERE ROWNUM<="
                        + Integer.toString(MAX_SEARCH_RESULTS));
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
	 * Najde zvířata, která nemají žádný výskyt (jsou vyhynulá)
	 *
         * @see T2SQL
	 * @see #searchResult
	 * @throws SQLException
	 */
	public void searchExtinctAnimals() throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat FROM animals a WHERE ("
                        +T2SQL.temporal(T2SQL.T2SQLprefix()+"SELECT COUNT(*) FROM animal_movement am where (valid_from <= sysdate OR valid_from is NULL) AND (valid_to > sysdate OR valid_to is NULL) AND a.animal_id=am.animal_id")
                        + ")=0";
                Log.debug(SQLquery);
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
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
	 * Funkce najde nejblíže se vyskytující zvířata od pozice uživatele
	 *
	 * @see #MAX_SEARCH_RESULTS
	 * @see #searchResult
	 * @see T2SQL
	 * @param location
	 *            Pozice uživatele
	 * @throws SQLException
	 */
	public void searchNearestAnimals(Point2D location) throws SQLException {
		OraclePreparedStatement opstmt = null;
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		String SQLquery = T2SQL.T2SQLprefix()
				+ "SELECT a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat, MIN(SDO_GEOM.SDO_DISTANCE(am.geometry,SDO_GEOMETRY(2001,8307,SDO_POINT_TYPE("
                                + nf.format(location.getX())
				+ ","
				+ nf.format(location.getY())
                                + ",NULL),NULL,NULL),1,'unit=KM')) AS distance FROM animals a, animal_movement am "
                                + "WHERE a.animal_id=am.animal_id GROUP BY a.animal_id,a.genus,a.species,a.genus_lat,a.species_lat "
                                + "ORDER BY distance";
		opstmt = (OraclePreparedStatement) con.prepareStatement("SELECT * FROM ("
                        +T2SQL.temporal(SQLquery)+ ") WHERE ROWNUM<=" + Integer.toString(MAX_SEARCH_RESULTS));
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
	 * Funkce zjistí, zda není zvíře již v databázi
	 *
	 * @param genus
         *          české nebo latinské rodové jméno
	 * @param species
         *          české nebo latinské druhové jméno
	 * @return true zvíře existuje, false neexistuje
	 * @throws SQLException
	 */
	public boolean animalExists(String genus, String species)
			throws SQLException {
		OraclePreparedStatement opstmt = null;
		String SQLquery = "SELECT COUNT(*) FROM animals WHERE ";
		SQLquery = SQLquery
				+ "(LOWER(species) LIKE ? AND LOWER(genus) LIKE ?) OR (LOWER(species_lat) LIKE ? AND LOWER(genus_lat) LIKE ?)";
		opstmt = (OraclePreparedStatement) con.prepareStatement(SQLquery);
		opstmt.setString(1, species.toLowerCase());
		opstmt.setString(2, genus.toLowerCase());
		opstmt.setString(3, species.toLowerCase());
		opstmt.setString(4, genus.toLowerCase());
		OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
		int pocet = 0;
		while (rset.next()) {
			pocet = rset.getInt("COUNT(*)");
		}
		rset.close();
		opstmt.close();
		if (pocet == 0)
			return false;
		else
			return true;
	}

	/**
	 * Vrátí jednu nebo všechny fotky zvířete
	 *
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 *
	 * @param id
	 *            ID zvířete
	 * @param all
	 *            true - všechny obrázky
         *            false - pouze 1 náhodný obrázek
	 * @param choosen_table
	 *            Tabulka ze které vybrat data
	 * @return HashMap<Integer photo_id,OrdImage photo>
	 * @throws SQLException
	 */
	public List<JPicture> selectPicture(int id, boolean all,
			String choosen_table) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery;
		if (all) {
			SQLquery = "SELECT photo, photo_id FROM " + choosen_table
					+ " WHERE animal_id=" + Integer.toString(id);
		} else {
			SQLquery = "SELECT photo, photo_id FROM " + choosen_table
					+ " WHERE ROWNUM <= 1 AND animal_id="
					+ Integer.toString(id);
		}
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		List<JPicture> data = new LinkedList<JPicture>();
		while (rset.next()) {

			JPicture pic = new JPicture((OrdImage) rset.getORAData("photo",
					OrdImage.getORADataFactory()), rset.getInt("photo_id"),
					choosen_table);
			data.add(pic);
		}
		rset.close();
		stat.close();
		return data;
	}

	/**
	 * Najde fotky náležející určité geometrické entitě
	 *
	 * @param move_id
	 *            ID geometrie
	 * @return Map<Integer,OrdImage>
	 * @throws SQLException
	 */
	public Map<Integer, OrdImage> selectPicture(int move_id)
			throws SQLException {
		Statement stat = con.createStatement();
		HashMap<Integer, OrdImage> result = new HashMap<Integer, OrdImage>();
		OracleResultSet rset = (OracleResultSet) stat
				.executeQuery("SELECT photo, photo_id FROM " + ANIMAL_PHOTO
						+ " WHERE move_id=" + Integer.toString(move_id));
		while (rset.next()) {
			result.put(
					rset.getInt("photo_id"),
					(OrdImage) rset.getORAData("photo",
							OrdImage.getORADataFactory()));
		}
		rset = (OracleResultSet) stat
				.executeQuery("SELECT photo, photo_id FROM " + EXCREMENT_PHOTO
						+ " WHERE move_id=" + Integer.toString(move_id));
		while (rset.next()) {
			result.put(
					rset.getInt("photo_id"),
					(OrdImage) rset.getORAData("photo",
							OrdImage.getORADataFactory()));
		}
		rset = (OracleResultSet) stat
				.executeQuery("SELECT photo, photo_id FROM " + FEET_PHOTO
						+ " WHERE move_id=" + Integer.toString(move_id));
		while (rset.next()) {
			result.put(
					rset.getInt("photo_id"),
					(OrdImage) rset.getORAData("photo",
							OrdImage.getORADataFactory()));
		}
		rset.close();
		stat.close();
		return result;
	}

	/**
	 * Vrátí geometrii zvířete
	 * <p>
	 * http://download.oracle.com/docs/cd/B19306_01/appdev.102/b14373/oracle/
	 * spatial/geometry/JGeometry.html
	 * </p>
	 *
	 * @param animal_id
	 *            id zvířete
	 * @return LinkedList<JEntity> list of points belongs to current
	 *         animal
	 * @throws SQLException
	 * @see T2SQL
	 */
	public List<JEntity> selectAppareance(int animal_id)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = T2SQL.T2SQLprefix()
				+ "SELECT move_id, geometry FROM animal_movement "
				+ "WHERE animal_id=" + Integer.toString(animal_id);
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(T2SQL
				.temporal(SQLquery));
		LinkedList<JEntity> data = new LinkedList<JEntity>();
		while (rset.next()) {
			JEntity entity = new JEntity(JGeometry.load((oracle.sql.STRUCT) rset.getSTRUCT("geometry")), rset.getInt("move_id"));
			data.add(entity);
		}
		rset.close();
		stat.close();
		return data;
	}

	/**
	 * Vrátí geometrie výskytu pro celý rod zvířete
	 *
	 * @param genus
	 *            Rodové jméno zvířete
	 * @param genus_lat
	 *            Latinské rodové jméno
	 * @return List<JEntity>
	 * @throws SQLException
	 */
	public List<JEntity> selectAppareance(String genus,
			String genus_lat) throws SQLException {
		String SQLquery = T2SQL.T2SQLprefix()
				+ "SELECT move_id, geometry FROM animal_movement am, animals a "
				+ "WHERE a.animal_id=am.animal_id AND (LOWER(a.genus) LIKE ? OR LOWER(a.genus_lat) LIKE ?)";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(T2SQL.temporal(SQLquery));
		opstmt.setString(1, genus.toLowerCase());
		opstmt.setString(2, genus_lat.toLowerCase());
		OracleResultSet rset = (OracleResultSet) opstmt.executeQuery();
		LinkedList<JEntity> data = new LinkedList<JEntity>();
		while (rset.next()) {
			JEntity entity = new JEntity(JGeometry.load((oracle.sql.STRUCT) rset.getSTRUCT("geometry")), rset.getInt("move_id"));
			data.add(entity);
		}
		rset.close();
		opstmt.close();
		return data;
	}

	// UPDATE functions

	/**
	 * Funkce pro úpravu geometrických entit
	 *
	 * @param move_id
	 *            ID geometrie
	 * @param j_geom
	 *            JGeometry objekt
	 * @throws SQLException
	 * @see T2SQL
	 */
	public void updateAppareance(int move_id, JGeometry j_geom)
			throws SQLException {
		String SQLquery = T2SQL.T2SQLprefix()
				+ "UPDATE geometry=? WHERE move_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(T2SQL.temporal(SQLquery));
		opstmt.setSTRUCT(1, JGeometry.store(j_geom, con));
		opstmt.setInt(2, move_id);
		opstmt.execute();
		opstmt.close();
	}

	/**
	 * Funkce pro úpravu zvoleného zvířete
	 *
	 * @param animal_id
         *          ID zvířete
	 * @param genus
         *          rodové jméno
	 * @param species
         *          druhové jméno
	 * @param genus_lat
         *          latinské rodové jméno
	 * @param species_lat
         *          latinské druhové jméno
	 * @param description
         *          popis zvířete
	 * @throws SQLException
	 */
	public void updateAnimal(int animal_id, String genus, String species,
			String genus_lat, String species_lat, String description)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "UPDATE animals SET (genus=?, species=?, genus=?, genus_lat=?, decsription=?) WHERE animal_id=? ";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
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
	 * Funkce pro úpravu zvířete
	 *
	 * @param anima
	 * @throws SQLException
	 * @see Animal
	 */
	public void updateAnimal(Animal anima) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "UPDATE animals SET genus=?, species=?, genus_lat=?, species_lat=?, description=? WHERE animal_id=? ";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
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

	// DELETE functions

	/**
	 * Funkce pro smazání geometrie
	 *
	 * @param move_id
	 *            ID geometrie
	 * @throws SQLException
	 * @see T2SQL
	 */
	public void deleteSpatialData(int move_id) throws SQLException {
		Statement stat = con.createStatement();
		stat.executeQuery(T2SQL.temporal(T2SQL.T2SQLprefix()
				+ "DELETE FROM animal_movement WHERE move_id="
				+ Integer.toString(move_id)));
		stat.close();
	}

	/**
	 * Funkce pro smazání zvířete
	 *
	 * @param animal_id
         *          id zvířete
	 * @throws SQLException
	 */
	public void deleteAnimal(int animal_id) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "DELETE FROM animals WHERE animal_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
		opstmt.setInt(1, animal_id);
		opstmt.execute();
		opstmt.close();
		stat.close();
	}

	/**
	 * Funkce pro smazání fotky z databáze
	 *
	 * @param photo_id
	 *            ID fotky
	 * @param table_name
	 *            Jméno tabulky ze které chceme mazat fotku
	 * @throws SQLException
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #SEARCH_PHOTO
	 * @see #uploadImage(int, java.lang.String, java.lang.String, int, java.lang.String)
	 */
	public void deletePicture(int photo_id, String table_name)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "DELETE FROM " + table_name + " WHERE photo_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
		opstmt.setInt(1, photo_id);
		opstmt.execute();
		opstmt.close();
		stat.close();
	}

	// INSERT functions

	/**
	 * Upload fotky do zvolené tabulky
	 *
	 * @param animal_id
	 *            ID zvířete
	 * @param choosen_table
	 *            Jméno zvolené tabulky
	 * @param filename
	 *            Cesta k souboru s obrázkem
	 * @param move_id
	 *            Číslo geometrie, ke které fotka patří - 0, pokud nikam nepatří
	 * @param description
	 *            Popis fotky
	 * @return integer s novým id fotky
	 * @throws SQLException
	 * @throws IOException
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #SEARCH_PHOTO
	 * @see #deletePicture(int, java.lang.String)
	 */
	@SuppressWarnings("deprecation")
	public int uploadImage(int animal_id, String choosen_table,
			String filename, int move_id, String description)
			throws SQLException, IOException {
		con.setAutoCommit(false);
		Statement stat = con.createStatement();
		String SQLquery = ("SELECT " + choosen_table + "_seq.nextval FROM dual");
		OracleResultSet rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		int nextval = rset.getInt("nextval");
		if (choosen_table.equals(SEARCH_PHOTO)) {
			SQLquery = "INSERT INTO "
					+ choosen_table
					+ " (photo_id, photo, photo_sig) VALUES ("
					+ nextval
					+ ", ordsys.ordimage.init(), ordsys.ordimagesignature.init())";
		} else {
			SQLquery = "INSERT INTO "
					+ choosen_table
					+ " (photo_id, animal_id, photo, photo_sig, move_id, description) VALUES ("
					+ nextval
					+ ", "
					+ animal_id
					+ ", ordsys.ordimage.init(), ordsys.ordimagesignature.init(),"
					+ Integer.toString(move_id) + ",'"
					+ description.replaceAll("'", "") + "')";
		}
		stat.execute(SQLquery);
		SQLquery = "SELECT photo, photo_sig FROM " + choosen_table
				+ " WHERE photo_id = " + Integer.toString(nextval)
				+ " FOR UPDATE";
		rset = (OracleResultSet) stat.executeQuery(SQLquery);
		rset.next();
		OrdImage imageProxy = (OrdImage) rset.getORAData("photo",
				OrdImage.getORADataFactory());
		OrdImageSignature signatureProxy = (OrdImageSignature) rset
				.getCustomDatum("photo_sig", OrdImageSignature.getFactory());
		rset.close();
		imageProxy.loadDataFromFile(filename);
		imageProxy.process("maxscale=" + RESOLUTION + " fileformat=png");
		imageProxy.setProperties();
		signatureProxy.generateSignature(imageProxy);
		SQLquery = "UPDATE " + choosen_table
				+ " SET photo=?, photo_sig=? WHERE photo_id=?";
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
	 * Uloží JGeometry do databáze
	 *
	 * @param animal_id
	 *            ID zvířete
	 * @param j_geom
	 *            JGeometry objekt
	 * @throws SQLException
	 * @see T2SQL
	 */
	public void insertAppareance(int animal_id, JGeometry j_geom)
			throws SQLException {
		con.setAutoCommit(false);
		Statement stat = con.createStatement();
		String SQLquery = T2SQL.T2SQLprefix()
				+ "INSERT INTO animal_movement (animal_id,geometry) VALUES (?,?)";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(T2SQL.temporal(SQLquery));
		opstmt.setInt(1, animal_id);
		opstmt.setSTRUCT(2, JGeometry.store(j_geom, con));
		opstmt.execute();
		opstmt.close();
		con.commit();
		con.setAutoCommit(true);
		stat.close();
		return;
	}

	/**
	 * Funkce pro vložení nového zvířete do databáze
	 *
	 * @param genus
         *          rodové jméno
	 * @param species
         *          druhové jméno
	 * @param genus_lat
         *          latinské rodové jméno
	 * @param species_lat
         *          latinské druhové jméno
	 * @param description
         *          popis zvířete
	 * @throws SQLException
	 * @see #deleteAnimal(int)
	 * @see #insertAnimal(cz.vutbr.fit.pdb03.Animal)
	 * @see #updateAnimal(cz.vutbr.fit.pdb03.Animal)
	 * @see #updateAnimal(int, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertAnimal(String genus, String species, String genus_lat,
			String species_lat, String description) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "INSERT INTO animals (genus, species, genus_lat, species_lat, description) VALUES (?,?,?,?,?) ";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
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
	 * Upraví popis fotky
	 *
	 * @param photo_id
	 *            ID fotky
	 * @param tablename
	 *            Jméno zvolené tabulky
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #getPhotoDescription(int, java.lang.String)
	 * @param description
	 *            Popis fotky zvířete
	 * @throws SQLException
	 */
	public void setPhotoDescription(int photo_id, String tablename,
			String description) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "UPDATE " + tablename
				+ " SET description=? WHERE photo_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
		opstmt.setString(1, description);
		opstmt.setInt(2, photo_id);
		opstmt.execute();
		opstmt.close();
		stat.close();
	}

	/**
	 * Upraví přidělení fotky ke geometrii
	 *
	 * @param photo_id
	 *            ID fotky
	 * @param tablename
	 *            Jméno zvolené tabulky fotek
	 * @param move_id
	 *            ID nově zvolené geometrie - 0 pokud k ničemu
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @throws SQLException
	 */
	public void setPhotoGeometry(int photo_id, String tablename, int move_id)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "UPDATE " + tablename
				+ " SET move_id=? WHERE photo_id=?";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
		opstmt.setInt(1, move_id);
		opstmt.setInt(2, photo_id);
		opstmt.execute();
		opstmt.close();
		stat.close();
	}

	/**
	 * Funkce pro vložení zvířete do databáze
	 *
	 * @param animal
	 *            objekt Animal
	 * @throws SQLException
	 * @see Animal
	 * @see #insertAnimal(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 * @see #deleteAnimal(int)
	 * @see #updateAnimal(cz.vutbr.fit.pdb03.Animal)
	 * @see #updateAnimal(int, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void insertAnimal(Animal animal) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "INSERT INTO animals (genus, species, genus_lat, species_lat, description) VALUES (?,?,?,?,?) ";
		OraclePreparedStatement opstmt = (OraclePreparedStatement) con
				.prepareStatement(SQLquery);
		opstmt.setString(1, animal.getGenus());
		opstmt.setString(2, animal.getSpecies());
		opstmt.setString(3, animal.getGenusLat());
		opstmt.setString(4, animal.getSpeciesLat());
		opstmt.setString(5, animal.getDescription());
		opstmt.execute();
		opstmt.close();
		stat.close();
	}

        /**
	 * Vrátí popis fotky
	 *
	 * @param photo_id
	 *            ID fotky
	 * @param tablename
	 *            Jméno zvolené tabulky fotek
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #setPhotoDescription(int, java.lang.String, java.lang.String)
	 * @return Popisek fotky
	 * @throws SQLException
	 */
	public String getPhotoDescription(int photo_id, String tablename)
			throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "SELECT description FROM " + tablename
				+ " WHERE photo_id=" + Integer.toString(photo_id);
		OracleResultSet rset = null;
		rset = (OracleResultSet) stat.executeQuery(SQLquery);
		SQLquery = "";
		rset.next();
		SQLquery = rset.getString("description");
		rset.close();
		stat.close();
		return SQLquery;
	}

	/**
	 * Funkce pro získání popisu zvířete z databáze
	 *
	 * @param animal_id
	 *            ID zvířete
	 * @return String with description
	 * @throws SQLException
	 */
	public String getDescription(int animal_id) throws SQLException {
		Statement stat = con.createStatement();
		String SQLquery = "SELECT description FROM animals WHERE animal_id="
				+ Integer.toString(animal_id);
		OracleResultSet rset = null;
		rset = (OracleResultSet) stat.executeQuery(SQLquery);
		SQLquery = "";
		while (rset.next()) {
			SQLquery = rset.getString("description");
			break;
		}
		rset.close();
		stat.close();
		return SQLquery;
	}

        /**
         * Není třeba, pokud se volá releaseCacheOnSpatialChange
         * Zavolat vždy po změně GPS souřadnic - to samé jako Animal.positionDataChanged, ale pro všechna zvířata v searchResult
         * @see Animal#positionDataChanged()
         * @see #searchResult
         * @see #releaseCacheOnSpatialChange()
         */
        public void releaseCacheOnNewGPS(){
            Iterator<Animal> test = searchResult.iterator();
            while (test.hasNext()){
                test.next().positionDataChanged();
            }
        }

        /**
         *  Zavolat vždy po změně prostorových dat a změně temporálního nastavení
         * To samé jako Animal.spatialDataChanged, ale pro všechna zvířata v searchResult
         * @see #searchResult
         * @see Animal#spatialDataChanged()
         */
        public void releaseCacheOnSpatialChange(){
            Iterator<Animal> test = searchResult.iterator();
            while (test.hasNext()){
                test.next().spatialDataChanged();
            }
        }

	// Private functions
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
			stat.execute("DROP INDEX animal_movement_sidx FORCE");
                        con.commit();
		} catch (SQLException e) {
		}
                try {
			stat.execute("DROP SEQUENCE "+SEARCH_PHOTO+"_seq");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP SEQUENCE animals_seq");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TABLE " + ANIMAL_PHOTO);
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP SEQUENCE " + ANIMAL_PHOTO + "_seq");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TABLE " + EXCREMENT_PHOTO);
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP SEQUENCE " + EXCREMENT_PHOTO + "_seq");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TABLE " + FEET_PHOTO);
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP SEQUENCE " + FEET_PHOTO + "_seq");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TABLE animal_movement");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP SEQUENCE animal_movement_seq");
		} catch (SQLException e) {
		}
		try {
                    con.commit();
			stat.execute("DROP TABLE animals");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TABLE " + SEARCH_PHOTO);
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP SEQUENCE " + SEARCH_PHOTO + "_seq");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TRIGGER animals_trigger");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP TRIGGER animal_movement_trigger_i");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP PROCEDURE animal_movement_delete");
		} catch (SQLException e) {
		}
		try {
			stat.execute("DROP PROCEDURE animal_movement_update");
		} catch (SQLException e) {
		}
		stat.close();
		con.commit();
	}

	/**
	 * Function for creating sequences in database - all starts with 1 and are
	 * incremented by 1
	 *
	 * @see #createDatabase()
	 * @see #deleteDatabase()
	 * @throws SQLException
	 */
	private void createSequences() throws SQLException {
		Statement stat = con.createStatement();
		stat.execute("CREATE SEQUENCE animal_movement_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE " + FEET_PHOTO
				+ "_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE " + EXCREMENT_PHOTO
				+ "_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE " + ANIMAL_PHOTO
				+ "_seq START WITH 1 INCREMENT BY 1");
                stat.execute("CREATE SEQUENCE " + SEARCH_PHOTO
				+ "_seq START WITH 1 INCREMENT BY 1");
		stat.execute("CREATE SEQUENCE animals_seq START WITH 1 INCREMENT BY 1");
		stat.close();
	}

	/**
	 * Function for creating stored procedures and triggers in database.
	 *
	 * @throws SQLException
	 * @see #createDatabase()
	 * @see #deleteDatabase()
	 */
	private void createTriggersAndProcedures() throws SQLException {
		Statement stat = con.createStatement();
		stat.execute("CREATE OR REPLACE TRIGGER animals_trigger "
				+ "BEFORE INSERT ON animals FOR EACH ROW "
				+ "BEGIN "
				+ " SELECT animals_seq.nextval INTO :NEW.animal_id FROM dual; "
				+ "  END;");
		stat.execute("CREATE OR REPLACE TRIGGER animal_movement_trigger_i "
				+ "BEFORE INSERT ON animal_movement FOR EACH ROW "
				+ "BEGIN"
				+ "  SELECT animal_movement_seq.nextval INTO :NEW.move_id FROM dual; "
				+ "END; ");
		stat.execute("CREATE OR REPLACE PROCEDURE animal_movement_delete( "
                        + " old_move_id IN	NUMBER, "
                        + " new_from IN	DATE, "
                        + " new_to IN	DATE) IS "
                        + "  old_move MDSYS.SDO_GEOMETRY; "
                        + "  old_animal_id NUMBER;"
                        + "  old_from DATE;"
                        + "  old_to DATE;"
                        + "  BEGIN"
                        + "    SELECT geometry, animal_id, valid_from, valid_to INTO old_move, old_animal_id, old_from, old_to"
                        + "    FROM animal_movement WHERE move_id=old_move_id;"
                        + "    IF new_from=new_to THEN"
                        + "      IF new_from IS NULL THEN"
                        + "        DELETE FROM animal_movement WHERE move_id=old_move_id;"
                        + "      ELSIF new_from <= old_from THEN"
                        + "        DELETE FROM animal_movement WHERE move_id=old_move_id;"
                        + "      ELSIF new_to < old_to THEN"
                        + "        UPDATE animal_movement SET valid_to=new_from-1 WHERE move_id=old_move_id;"
                        + "      END IF;"
                        + "    ELSIF new_from<new_to THEN"
                        + "      IF new_from<=old_from AND new_to>old_from THEN"
                        + "        IF new_to>=old_to THEN"
                        + "          DELETE FROM animal_movement WHERE move_id=old_move_id;"
                        + "        ELSE"
                        + "          UPDATE animal_movement SET valid_from=new_to WHERE move_id=old_move_id;"
                        + "        END IF;"
                        + "      ELSIF new_to>=old_to AND new_from<old_to THEN"
                        + "        IF new_from<=old_from THEN"
                        + "          DELETE FROM animal_movement WHERE move_id=old_move_id;"
                        + "        ELSE"
                        + "	  UPDATE animal_movement SET valid_to=new_from-1 WHERE move_id=old_move_id;"
                        + "        END IF;"
                        + "      ELSE"
                        + "        INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "        VALUES (old_animal_id, new_to, old_to,old_move);"
                        + "        UPDATE animal_movement SET valid_to=new_from-1 WHERE move_id=old_move_id;"
                        + "      END IF;"
                        + "    END IF; "
                        + "END animal_movement_delete;");
		stat.execute("CREATE OR REPLACE PROCEDURE animal_movement_update("
                        + " new_move IN	MDSYS.SDO_GEOMETRY,"
                        + " old_move_id IN	NUMBER,"
                        + " new_from IN	DATE,"
                        + " new_to IN	DATE) IS"
                        + "  old_move MDSYS.SDO_GEOMETRY;"
                        + "  old_animal_id NUMBER;"
                        + "  old_from DATE;"
                        + "  old_to DATE;"
                        + "  BEGIN"
                        + "    SELECT geometry, animal_id, valid_from, valid_to INTO old_move, old_animal_id, old_from, old_to"
                        + "    FROM animal_movement WHERE move_id=old_move_id;"
                        + "    IF new_from=new_to THEN"
                        + "      IF new_from IS NULL THEN"
                        + "        UPDATE animal_movement SET valid_from=NULL, valid_to=NULL, geometry=new_move WHERE move_id=old_move_id;"
                        + "      ELSIF new_from <= old_from THEN"
                        + "        UPDATE animal_movement SET valid_from=new_from, valid_to=null, geometry=new_move WHERE move_id=old_move_id;"
                        + "      ELSIF new_to >= old_to THEN"
                        + "        INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "        VALUES (old_animal_id, new_from, null,new_move);"
                        + "      ELSE"
                        + "        UPDATE animal_movement SET valid_to=new_from-1 WHERE move_id=old_move_id;"
                        + "        INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "        VALUES (old_animal_id, new_from, null,new_move);"
                        + "      END IF;"
                        + "    ELSIF new_from<new_to THEN"
                        + "      IF new_from<=old_from AND new_to>old_from THEN"
                        + "        IF new_to>=old_to THEN"
                        + "          UPDATE animal_movement SET valid_from=new_from, valid_to=new_to, geometry=new_move WHERE move_id=old_move_id;"
                        + "        ELSE"
                        + "          UPDATE animal_movement SET valid_from=new_to WHERE move_id=old_move_id;"
                        + "          INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "          VALUES (old_animal_id, new_from, new_to,new_move);"
                        + "        END IF;"
                        + "      ELSIF new_to>=old_to AND new_from<old_to THEN"
                        + "        IF new_from<=old_from THEN"
                        + "          UPDATE animal_movement SET valid_from=new_from, valid_to=new_to, geometry=new_move WHERE move_id=old_move_id;"
                        + "        ELSE"
                        + "          UPDATE animal_movement SET valid_to=new_from-1 WHERE move_id=old_move_id;"
                        + "          INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "          VALUES (old_animal_id, new_from, new_to,new_move);"
                        + "        END IF;"
                        + "      ELSE"
                        + "        INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "        VALUES (old_animal_id, new_to, old_to,old_move);"
                        + "        UPDATE animal_movement SET valid_to=new_from-1 WHERE move_id=old_move_id;"
                        + "        INSERT INTO animal_movement (animal_id,valid_from, valid_to, geometry)"
                        + "        VALUES (old_animal_id, new_from, new_to-1,new_move);"
                        + "      END IF;"
                        + "    END IF; "
                        + "END animal_movement_update;");
		stat.close();
	}

	/**
	 * Function for creating picture index
	 *
	 * @param tablename
	 * @see #createDatabase()
	 * @see #deleteDatabase()
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #SEARCH_PHOTO
	 */
	private void createIndex(String tablename) {
		try {
			Statement stat = con.createStatement();
			stat.execute("CREATE INDEX " + tablename + "_idx ON " + tablename
					+ " (photo_sig) INDEXTYPE IS ordsys.ordimageindex;");
			stat.close();
		} catch (SQLException ex) {
		}
	}

	/**
	 * Function for deleting picture index
	 *
	 * @param tablename
	 * @see #createDatabase()
	 * @see #createIndex(java.lang.String)
	 * @see #deleteDatabase()
	 * @see #ANIMAL_PHOTO
	 * @see #EXCREMENT_PHOTO
	 * @see #FEET_PHOTO
	 * @see #SEARCH_PHOTO
	 */
	private void deleteIndex(String tablename) {
		try {
			Statement stat = con.createStatement();
			stat.execute("DROP INDEX " + tablename + "_idx FORCE");
			stat.close();
		} catch (SQLException ex) {
		}
	}

	/**
	 * Fills dabase with example data
	 *
	 * @param filename
	 *            filename of SQL file
	 * @throws SQLException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void executeSQLFile(String filename) throws SQLException,
			IOException {
		String s = new String();
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		while ((s = br.readLine()) != null) {
			sb.append(s);
		}
		br.close();
		String[] inst = sb.toString().split(";");
		Statement st = con.createStatement();
		for (int i = 0; i < inst.length; i++) {
			if (!inst[i].trim().equals("")) st.executeUpdate(inst[i]);
		}
	}

        /**
         * Funkce pro zjištění překrývající se rozlohu polygonů stejného zvířete
         * Omezení: vždy maximálně dva překrývající se polygony v jednom místě, jinak nesprávné výsledky
         * @param animal_id
         *          Id zvířete
         * @return Překrývající se rozloha (km2)
         * @throws SQLException
         */
        private Double getIntersectionsArea(int animal_id) throws SQLException{
            Statement stat = con.createStatement();
            String SQLquery = "SELECT SUM(SDO_GEOM.SDO_AREA(SDO_GEOM.SDO_INTERSECTION(a.geometry,b.geometry,0.1),0.1,'unit=SQ_KM')) AS area "
                    + "FROM animal_movement a, animal_movement b WHERE a.animal_id=" + Integer.toString(animal_id)
                    + " AND b.animal_id=" + Integer.toString(animal_id) + " AND a.move_id<b.move_id";
            if (T2SQL.getMode().equals(T2SQL.NOW)){
                SQLquery=SQLquery + " AND (((a.valid_from <= sysdate OR a.valid_from is NULL) AND "
                        + "(a.valid_to > sysdate OR a.valid_to is NULL)) OR ((a.valid_from <= sysdate OR "
                        + "a.valid_from is NULL) AND (a.valid_to > sysdate OR a.valid_to is NULL)))";
                SQLquery=SQLquery + " AND (((b.valid_from <= sysdate OR b.valid_from is NULL) AND "
                        + "(b.valid_to > sysdate OR b.valid_to is NULL)) OR ((b.valid_from <= sysdate OR "
                        + "b.valid_from is NULL) AND (b.valid_to > sysdate OR b.valid_to is NULL)))";
            } else if (T2SQL.getMode().equals(T2SQL.INTERVAL) || T2SQL.getMode().equals(T2SQL.DATETIME)){
                SQLquery=SQLquery + " AND (((a.valid_from <= DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateFrom())
                        +"' OR a.valid_from is NULL) AND (a.valid_to > DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateFrom())
                        +"' OR a.valid_to is NULL)) OR ((a.valid_from <= DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateTo())
                        +"' OR a.valid_from is NULL) AND (a.valid_to > DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateTo())
                        +"' OR a.valid_to is NULL)))";
                SQLquery=SQLquery + " AND (((b.valid_from <= DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateFrom())
                        +"' OR b.valid_from is NULL) AND (b.valid_to > DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateFrom())
                        +"' OR b.valid_to is NULL)) OR ((b.valid_from <= DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateTo())
                        +"' OR b.valid_from is NULL) AND (b.valid_to > DATE '"
                        +T2SQL.dateFormat.format(T2SQL.getValidationDateTo())
                        +"' OR b.valid_to is NULL)))";
            }
            //Log.info(SQLquery);
            OracleResultSet rset = null;
            rset = (OracleResultSet) stat.executeQuery(SQLquery);
            Double result = 0.0;
            while (rset.next()) {
            	result = result + rset.getDouble("area");
            }
            rset.close();
            stat.close();
            return result;
        }
}
