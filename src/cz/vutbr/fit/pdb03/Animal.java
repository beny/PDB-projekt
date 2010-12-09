package cz.vutbr.fit.pdb03;

import java.awt.geom.Point2D;
import java.sql.SQLException;

/**
 * Objekt pro uložení informací o zvířeti
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public class Animal {

	//základní informace o zvířeti
	private int id = 0;
        /**
         * Rodové jméno
         */
	private String genus = "";
        /**
         * Druhové jméno
         */
	private String species = "";
        /**
         * Rodové jméno latinsky
         */
	private String genusLat = "";
        /**
         * Druhové jméno latinsky
         */
	private String speciesLat = "";
        /**
         * Popis zvířete
         */
	private String description = "";
	private Double nearestAppareance = -1.0;
	private Double appareanceArea = -1.0;

        /**
         * Konstruktor
         */
	public Animal(){

	}

        /**
         * Konstruktor
         * @param genus
         *          Rodové jméno
         * @param species
         *          Druhové jméno
         * @param genusLat
         *          Rodové jméno latinsky
         * @param speciesLat
         *          Druhové jméno latinsky
         * @param description
         *          Popis zvířete
         */
	public Animal(String genus, String species, String genusLat,
			String speciesLat, String description) {
		this.genus = genus;
		this.species = species;
		this.genusLat = genusLat;
		this.speciesLat = speciesLat;
		this.description = description;
	}

	public Animal(int id, String genus, String genusLat, String species, String speciesLat, String description){
		this(genus, species, genusLat, speciesLat, description);
		setId(id);
	}

	// //////GETRS, SETRS
        /**
         * Vrátí Id zvířete
         * @return id zvířete
         */
	public int getId() {
		return id;
	}

        /**
         * Nastaví id zvířete
         * @param id
         *          ID zvířete
         */
	public void setId(int id) {
		this.id = id;
	}

        /**
         * Získá rodové jméno
         * @return rodové jméno
         */
	public String getGenus() {
		return genus;
	}

        /**
         * Nastaví rodové jméno
         * @param genus
         *          rodové jméno
         */
	public void setGenus(String genus) {
		this.genus = genus;
	}

        /**
         * Získá druhové jméno
         * @return druhové jméno
         */
	public String getSpecies() {
		return species;
	}

        /**
         * Nastaví druhové jméno
         * @param species
         *          druhové jméno
         */
	public void setSpecies(String species) {
		this.species = species;
	}

        /**
         * Zjistí latinské rodové jméno
         * @return latinské rodové jméno
         */
	public String getGenusLat() {
		return genusLat;
	}

        /**
         * Nastaví latinské rodové jméno
         * @param genusLat
         *          latinské rodové jméno
         */
	public void setGenusLat(String genusLat) {
		this.genusLat = genusLat;
	}

        /**
         * Získá latinské druhové jméno
         * @return latinské druhové jméno
         */
	public String getSpeciesLat() {
		return speciesLat;
	}

        /**
         * Nastaví latinské druhové jméno
         * @param speciesLat
         *          Latinské druhové jméno
         */
	public void setSpeciesLat(String speciesLat) {
		this.speciesLat = speciesLat;
	}

	// //////OTHER PUBLIC FUNCTIONS
	/**
         * Získá popis zvířete
	 * Používat při vkládání a modifikaci - spíše nepoužívat
	 * @return description of an animal
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Nastaví popis zvířete	 *
	 * @param description
	 *            popis zvířete
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Vrátí popis zvířete - používat při zobrazování dat (ne při vkládání a modifikaci - může dojít ke ztrátě dat)
	 * @param data
	 *            Připojená databáze
	 * @see DataBase#getDescription(int)
	 * @return Popis zvířete
	 * @throws SQLException
	 */
	public String getDescription(DataBase data) throws SQLException {
		if (description.equals(""))
			description = data.getDescription(id);
		return description;
	}

	/**
	 * Vrátí nejbližší vzdálenost k výskytu zvířete (v km)
	 *
	 * @param data
	 *            Připojená databáze
	 * @param location
	 *            Pozice uživatele
	 * @see DataBase#getNearestAppareance(int, java.awt.geom.Point2D)
	 * @return Vzdálenost k výskytu zvířete v kilometrech
	 * @throws SQLException
	 */
	public Double getNearestAppareance(DataBase data, Point2D location)
			throws SQLException {
		if (nearestAppareance == -1.0)
			nearestAppareance = data.getNearestAppareance(id, location);
		return nearestAppareance;
	}

	/**
	 * Vrátí rozlohu zvířete (v km2)
	 *
	 * @param data
	 *            current DataBase
	 * @see DataBase#getAppareanceArea(int)
	 * @return rozloha (km2)
	 * @throws SQLException
	 */
	public Double getAppareanceArea(DataBase data) throws SQLException {
		if (appareanceArea == -1.0)
			appareanceArea = data.getAppareanceArea(id);
		return appareanceArea;
	}

	/**
	 * Zavolat po každé změně prostorových dat (změna časových údajů - vložení, modifikace a smazání entit)
	 *
	 * @see DataBase#deleteSpatialData(int)
	 * @see DataBase#insertAppareance(int, oracle.spatial.geometry.JGeometry)
	 * @see DataBase#updateAppareance(int, oracle.spatial.geometry.JGeometry) 
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
	 * Zavolat pokaždé, kdy dojde ke změně GPS pozice uživatele
	 */
	public void positionDataChanged() {
		freeNearestAppareance();
	}

	@Override
	public String toString() {

		String name = "";

		if(genus != null){
			name += genus;
		}

		if(species != null){
			if(name.length() != 0){
				name += ", ";
			}
			name += species;
		}
		return name;
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
