package cz.vutbr.fit.pdb03;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public final class T2SQL {
    public static String temporal(String T2SQLString){
        String SQLString="";
        DateFormat dateFormat = new SimpleDateFormat("'dd-MM-yyyy HH24:MI:SS'");
        if (T2SQLString.startsWith("NONSEQUENCED VALIDTIME ")){
            SQLString=T2SQLString.substring(23);
        } else if (T2SQLString.startsWith("VALIDTIME PERIOD [")){
            int point1=T2SQLString.indexOf('[');
            int point2=T2SQLString.indexOf('-',point1);
            int point3=T2SQLString.indexOf(')',point2);
            Date day_from = null;
            Date day_to=null;
            try{
                day_from=dateFormat.parse(T2SQLString.substring(point1+1, point2-1));
                day_to=dateFormat.parse(T2SQLString.substring(point2+1, point3-1));
            } catch(ParseException e){ }
            SQLString=T2SQLString.substring(point2+1);
            SQLString=SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= "+dateFormat.format(day_from)+") AND (valid_to > "+dateFormat.format(day_to)+" OR valid_to=NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,valid_to,");
                SQLString=SQLString.replace(" VALUES (", " VALUES ("+dateFormat.format(day_from)+","+dateFormat.format(day_to)+",");
            }
        } else if (T2SQLString.startsWith("VALIDTIME DATE ")){
            int point1=15;
            while (T2SQLString.charAt(point1)==' '){
                point1++;
            }
            int point2=T2SQLString.indexOf(' ',point1);
            Date day=null;
            try{
                day=dateFormat.parse(T2SQLString.substring(point1, point2-1));
            } catch(ParseException e){ }
            SQLString=T2SQLString.substring(point2+1);
            SQLString=SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= "+dateFormat.format(day)+") AND (valid_to > "+dateFormat.format(day)+" OR valid_to=NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,");
                SQLString=SQLString.replace(" VALUES (", " VALUES ("+dateFormat.format(day)+",");
            }
        } else {
            SQLString=SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= sysdate) AND (valid_to > sysdate OR valid_to=NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                
            }
        }
        D.log(SQLString);
        return SQLString;
    }
}
