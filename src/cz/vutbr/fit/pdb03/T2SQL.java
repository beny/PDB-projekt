package cz.vutbr.fit.pdb03;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public final class T2SQL {
    public static String temporal(String T2SQLString){
        String SQLString="";
        DateFormat dateFormat = new SimpleDateFormat("'dd-MM-yyyy'");
        if (T2SQLString.startsWith("NONSEQUENCED VALIDTIME ")){
            SQLString=T2SQLString.substring(23);
        } else if (T2SQLString.startsWith("VALIDTIME PERIOD [")){
            //extract dates, delete prefix
            Date day_from=new Date();
            Date day_to=new Date();
            SQLString=(T2SQLString.substring(0)).replace(" WHERE ", " WHERE (valid_from <= "+dateFormat.format(day_from)+") AND (valid_to > "+dateFormat.format(day_to)+" OR valid_to=NULL) AND ");
        } else if (T2SQLString.startsWith("VALIDTIME DATE ")){
            //extract date, delete prefix
            Date day=new Date();
            SQLString=(T2SQLString.substring(0)).replace(" WHERE ", " WHERE (valid_from <= "+dateFormat.format(day)+") AND (valid_to >= "+dateFormat.format(day)+" OR valid_to=NULL) AND ");
        } else {
            SQLString=T2SQLString.replace(" WHERE ", " WHERE (valid_from <= sysdate) AND (valid_to >= sysdate OR valid_to=NULL) AND ");
        }
        return SQLString;
    }
}
