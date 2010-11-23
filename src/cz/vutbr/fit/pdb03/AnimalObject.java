/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.vutbr.fit.pdb03;

import java.sql.SQLException;

/**
 * Object for saving informations about animal
 * 
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public class AnimalObject {
    public int animal_id=0;
    public String genus="";
    public String family="";
    public String genus_lat="";
    public String family_lat="";
    private String description="";
    public String getDescription(DataBase data) throws SQLException{
        if (description.equals("")) description=data.getDescription(animal_id);
        return description;
    }
}
