package cz.vutbr.fit.pdb03;

import java.sql.SQLException;
import java.awt.geom.Point2D;

/**
 * Object for saving informations about animal
 * 
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public class AnimalObject {
////Variables///////////////////////////////////////////////////////////////////
    //basic informations about animal
    public int animal_id=0;
    public String genus="";
    public String family="";
    public String genus_lat="";
    public String family_lat="";
    private String description="";
    private Double nearest_appareance=-1.0;
    private Double appareance_area=-1.0;
////PUBLIC FUNCTIONS////////////////////////////////////////////////////////////
    /**
     * Returns description of current animal
     * @param data
     *          Current DataBase
     * @return description of current animal
     * @throws SQLException
     */
    public String getDescription(DataBase data) throws SQLException{
        if (description.equals("")) description=data.getDescription(animal_id);
        return description;
    }
    /**
     * Returns distance of nearest appareance of current animal (km)
     * @param data
     *          current DataBase
     * @param location
     *          current user location
     * @return distance of nearest appareance of current animal (km)
     * @throws SQLException
     */
    public Double getNearestAppareance(DataBase data,Point2D location) throws SQLException{
        if (nearest_appareance==-1.0) nearest_appareance=data.getNearestAppareance(animal_id,location);
        return nearest_appareance;
    }
    /**
     * Returns appareance area of current animal (km2)
     * @param data
     *          current DataBase
     * @return appareance area of current animal (km2)
     * @throws SQLException
     */
    public Double getAppareanceArea(DataBase data) throws SQLException{
        if (appareance_area==-1.0) appareance_area=data.getAppareanceArea(animal_id);
        return appareance_area;
    }
    /**
     * Call everytime, when spatial data of current animal were changed
     */
    public void spatialDataChanged(){
        freeNearestAppareance();
        freeAppareanceArea();
    }
    /**
     * Call everytime, when actual position data were changed
     */
    public void positionDataChanged(){
        freeNearestAppareance();
    }
////PRIVATE FUNCTIONS///////////////////////////////////////////////////////////
    /**
     * Invalidates data about nearest appareance of current animal
     */
    private void freeNearestAppareance(){
        nearest_appareance=-1.0;
    }
    /**
     * Invalidates data about appareance area of current animal
     */
    private void freeAppareanceArea(){
        nearest_appareance=-1.0;
    }
}
