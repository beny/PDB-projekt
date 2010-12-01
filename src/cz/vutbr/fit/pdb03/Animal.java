package cz.vutbr.fit.pdb03;

import java.sql.SQLException;
import java.awt.geom.Point2D;

/**
 * Object for saving informations about animal
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public class Animal {

	// basic informations about animal
	private int id = 0;
	private String genus = "";
	private String family = "";
	private String genusLat = "";
	private String familyLat = "";
	private String description = "";
	private Double nearestAppareance = -1.0;
	private Double appareanceArea = -1.0;

	public Animal(){

	}

	public Animal(String genus, String family, String genusLat,
			String familyLat, String description) {
		this.genus = genus;
		this.family = family;
		this.genusLat = genusLat;
		this.familyLat = familyLat;
		this.description = description;
	}

	public Animal(int id, String genus, String genusLat, String family, String familyLat, String description){
		this(genus, family, genusLat, familyLat, description);
		setId(id);
	}

	// //////GETRS, SETRS
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getGenusLat() {
		return genusLat;
	}

	public void setGenusLat(String genusLat) {
		this.genusLat = genusLat;
	}

	public String getFamilyLat() {
		return familyLat;
	}

	public void setFamilyLat(String familyLat) {
		this.familyLat = familyLat;
	}

	// //////OTHER PUBLIC FUNCTIONS
	/**
	 * use this function while inserting and updating
	 *
	 * @return description of an animal
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets description of an animal
	 *
	 * @param description
	 *            description of an animal
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns description of current animal use this function only during
	 * selecting (not inserting or updating - may occurs losing data)
	 *
	 * @param data
	 *            Current DataBase
	 * @see DataBase#getDescription(int)
	 * @return description of current animal
	 * @throws SQLException
	 */
	public String getDescription(DataBase data) throws SQLException {
		if (description.equals(""))
			description = data.getDescription(id);
		return description;
	}

	/**
	 * Returns distance of nearest appareance of current animal (km)
	 *
	 * @param data
	 *            current DataBase
	 * @param location
	 *            current user location
	 * @see DataBase#getNearestAppareance(int, java.awt.geom.Point2D)
	 * @return distance of nearest appareance of current animal (km)
	 * @throws SQLException
	 */
	public Double getNearestAppareance(DataBase data, Point2D location)
			throws SQLException {
		if (nearestAppareance == -1.0)
			nearestAppareance = data.getNearestAppareance(id, location);
		return nearestAppareance;
	}

	/**
	 * Returns appareance area of current animal (km2)
	 *
	 * @param data
	 *            current DataBase
	 * @see DataBase#getAppareanceArea(int)
	 * @return appareance area of current animal (km2)
	 * @throws SQLException
	 */
	public Double getAppareanceArea(DataBase data) throws SQLException {
		if (appareanceArea == -1.0)
			appareanceArea = data.getAppareanceArea(id);
		return appareanceArea;
	}

	/**
	 * Call everytime, when spatial data of current animal were changed
	 *
	 * @see DataBase#deleteSpatialData(int)
	 * @see DataBase#insertAppareance(int, oracle.spatial.geometry.JGeometry)
	 * @see DataBase#updateAppareance(int, int,
	 *      oracle.spatial.geometry.JGeometry)
	 * @see T2SQL#setCurrentTime()
	 * @see T2SQL#setNoTemporalRestrictions()
	 * @see T2SQL#setValidationDate(java.util.Date)
	 * @see T2SQL#setValidationDates(java.util.Date, java.util.Date)
	 */
	public void spatialDataChanged() {
		freeNearestAppareance();
		freeAppareanceArea();
	}

	/**
	 * Call everytime, when actual position data were changed
	 */
	public void positionDataChanged() {
		freeNearestAppareance();
	}

	/**
	 * Converts animal object into string
	 *
	 * @return string with name of an animal
	 */
	@Override
	public String toString() {
		return genus + ", " + family;
	}

	// ///////PRIVATE FUNCTIONS
	/**
	 * Invalidates data about nearest appareance of current animal
	 */
	private void freeNearestAppareance() {
		nearestAppareance = -1.0;
	}

	/**
	 * Invalidates data about appareance area of current animal
	 */
	private void freeAppareanceArea() {
		appareanceArea = -1.0;
	}
}
