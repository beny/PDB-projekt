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
	private String genus_lat = "";
	private String family_lat = "";
	private String description = "";
	private Double nearest_appareance = -1.0;
	private Double appareance_area = -1.0;

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


	public String getGenus_lat() {
		return genus_lat;
	}


	public void setGenus_lat(String genus_lat) {
		this.genus_lat = genus_lat;
	}


	public String getFamily_lat() {
		return family_lat;
	}


	public void setFamily_lat(String family_lat) {
		this.family_lat = family_lat;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * Returns description of current animal
	 *
	 * @param data
	 *            Current DataBase
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
	 * @return distance of nearest appareance of current animal (km)
	 * @throws SQLException
	 */
	public Double getNearestAppareance(DataBase data, Point2D location)
			throws SQLException {
		if (nearest_appareance == -1.0)
			nearest_appareance = data.getNearestAppareance(id, location);
		return nearest_appareance;
	}

	/**
	 * Returns appareance area of current animal (km2)
	 *
	 * @param data
	 *            current DataBase
	 * @return appareance area of current animal (km2)
	 * @throws SQLException
	 */
	public Double getAppareanceArea(DataBase data) throws SQLException {
		if (appareance_area == -1.0)
			appareance_area = data.getAppareanceArea(id);
		return appareance_area;
	}

	/**
	 * Call everytime, when spatial data of current animal were changed
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
	 * Invalidates data about nearest appareance of current animal
	 */
	private void freeNearestAppareance() {
		nearest_appareance = -1.0;
	}

	/**
	 * Invalidates data about appareance area of current animal
	 */
	private void freeAppareanceArea() {
		nearest_appareance = -1.0;
	}
}
