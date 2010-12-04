package cz.vutbr.fit.pdb03;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
// TODO overit delete a ze to vsechno dela co ma
/**
 * Class for T2SQL support
 * Defines new language T2SQL (TimeToSQL). It's something like simple TSQL2 or TimeDB
 * <p>
 * It's defined like this:
 * </p><p>
 * - VALIDTIME PERIOD [<DateTime>,<DateTime>) <SQL>
 *  transforms SQL for valid records only (which are valid in some time which is in interval)
 * </p><p>
 * - VALIDTIME DATE <DateTime> <SQL>
 *  transforms SQL for valid records only (which are valid in that time)
 * </p><p>
 * - <SQL>
 *  transforms SQL for valid records only (which are valid in current time)
 * </p><p>
 * - NONSEQUENCED VALIDTIME <SQL>
 *  prefix (NONSEQUENCED VALIDTIME ) is deleted only. SQL is for all records in database.
 * </p>
 *  <SQL> is some SQL language
 *  <DateTime> is Date and Time in format yyyy/MM/dd~HH:mm
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public final class T2SQL {
    private static Date validFrom=null;
    private static Date validTo=null;
    private static String mode="";
    private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static DateFormat dateFormatIn = new SimpleDateFormat("yyyy/MM/dd'~'HH:mm:ss");

    /**
     * Function sets nonsequenced validtime
     * @see #setCurrentTime()
     * @see #setValidationDate(java.util.Date)
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static void setNoTemporalRestrictions(){
        setMode("NONSEQUENCED VALIDTIME");
        validFrom=null;
        validTo=null;
    }

    /**
     * Function sets VALIDTIME PERIOD
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDate(java.util.Date)
     * @param from
     *          Validtime begins
     * @param to
     *          Validtime ends
     */
    public static void setValidationDates(Date from, Date to){
        validFrom=from;
        validTo=to;
        setMode("VALIDTIME PERIOD");
    }

    /**
     * Functions sets showing only valid data for actual moment
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDate(java.util.Date)
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static void setCurrentTime(){
        setValidationDates(null,null);
        setMode("");
    }

    /**
     * Function sets showing only valid data for wanted date
     * @param date
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static void setValidationDate(Date date){
        setValidationDates(date,date);
        setMode("VALIDTIME DATE");
    }

    /**
     * To find out FROM validation date
     * @return Setted validation date 'From'
     */
    public static Date getValidationDateFrom(){
        return validFrom;
    }

    /**
     * To find out TO validation date
     * @return Setted validation date 'To'
     */
    public static Date getValidationDateTo(){
        return validTo;
    }

    /**
     * To find out which mode is setted
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDates(java.util.Date, java.util.Date)
     * @see #setValidationDate(java.util.Date)
     * @return Setted mode
     */
    public static String getMode(){
        return mode;
    }

    /**
     * Conversion from T2SQL notation to SQL notation
     * Works for SELECT, INSERT, UPDATE and DELETE
     * RESTRICTIONS: simple querying, DELETE and UPDATE have format ...WHERE move_id=<number>
     * @param T2SQLString
     *          T2SQL string
     * @return SQL string
     */
    public static String temporal(String T2SQLString){
        T2SQLString=T2SQLString.trim();
        String SQLString="";
        if (T2SQLString.startsWith("NONSEQUENCED VALIDTIME ")){
            SQLString=(T2SQLString.substring(23)).trim();
            if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",null,null))";
            } else if (SQLString.startsWith("UPDATE")){
                int point1u=SQLString.indexOf("geometry");
                point1u=SQLString.indexOf('=',point1u);
                int point2u=SQLString.indexOf(" WHERE ");
                int point3u=SQLString.indexOf("move_id", point2u);
                point3u=SQLString.indexOf('=',point3u);
                String move_id=(SQLString.substring(point3u+1)).trim();
                if (move_id.indexOf(' ')>0) move_id.substring(0, move_id.indexOf(' '));
                String geometry=(SQLString.substring(point1u+1,point2u)).trim();
                SQLString="CALL(animal_movement_update("+geometry+", "+move_id+", null, null))";
            }
        } else if (T2SQLString.startsWith("VALIDTIME PERIOD [")){
            int point1=T2SQLString.indexOf('[');
            int point2=T2SQLString.indexOf('-',point1);
            int point3=T2SQLString.indexOf(')',point2);
            Date day_from = null;
            Date day_to=null;
            try{
                day_from=dateFormatIn.parse(T2SQLString.substring(point1+1, point2));
                day_to=dateFormatIn.parse(T2SQLString.substring(point2+1, point3));
            } catch(ParseException e){ }
            SQLString=T2SQLString.substring(point3+1);
            SQLString=SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE ((valid_from <= '"+dateFormat.format(day_from)+"' OR valid_from is NULL) AND (valid_to > '"+dateFormat.format(day_from)+"' OR valid_to is NULL)) OR ((valid_from <= '"+dateFormat.format(day_to)+"' OR valid_from is NULL) AND (valid_to > '"+dateFormat.format(day_to)+"' OR valid_to is NULL)) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,valid_to,");
                SQLString=SQLString.replace(" VALUES (", " VALUES ('"+dateFormat.format(day_from)+"','"+dateFormat.format(day_to)+"',");
            } else if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",'"+dateFormat.format(day_from)+"','"+dateFormat.format(day_to)+"'))";
            } else if (SQLString.startsWith("UPDATE")){
                int point1u=SQLString.indexOf("geometry");
                point1u=SQLString.indexOf('=',point1u);
                int point2u=SQLString.indexOf(" WHERE ");
                int point3u=SQLString.indexOf("move_id", point2u);
                point3u=SQLString.indexOf('=',point3u);
                String move_id=(SQLString.substring(point3u+1)).trim();
                if (move_id.indexOf(' ')>0) move_id.substring(0, move_id.indexOf(' '));
                String geometry=(SQLString.substring(point1u+1,point2u)).trim();
                SQLString="CALL(animal_movement_update("+geometry+", "+move_id+",'"+dateFormat.format(day_from)+"','"+dateFormat.format(day_to)+"'))";
            }
        } else if (T2SQLString.startsWith("VALIDTIME DATE ")){
            int point1=15;
            while (T2SQLString.charAt(point1)==' '){
                point1++;
            }
            int point2=T2SQLString.indexOf(' ',point1);
            Date day=null;
            try{
                day=dateFormatIn.parse(T2SQLString.substring(point1, point2-1));
            } catch(ParseException e){ }
            SQLString=T2SQLString.substring(point2+1);
            SQLString=SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= '"+dateFormat.format(day)+"' OR valid_from is NULL) AND (valid_to > '"+dateFormat.format(day)+"' OR valid_to is NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,");
                SQLString=SQLString.replace(" VALUES (", " VALUES ('"+dateFormat.format(day)+"',");
            } else if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",'"+dateFormat.format(day)+"','"+dateFormat.format(day)+"'))";
            } else if (SQLString.startsWith("UPDATE")){
                int point1u=SQLString.indexOf("geometry");
                point1u=SQLString.indexOf('=',point1u);
                int point2u=SQLString.indexOf(" WHERE ");
                int point3u=SQLString.indexOf("move_id", point2u);
                point3u=SQLString.indexOf('=',point3u);
                String move_id=(SQLString.substring(point3u+1)).trim();
                if (move_id.indexOf(' ')>0) move_id.substring(0, move_id.indexOf(' '));
                String geometry=(SQLString.substring(point1u+1,point2u)).trim();
                SQLString="CALL(animal_movement_update("+geometry+", "+move_id+",'"+dateFormat.format(day)+"','"+dateFormat.format(day)+"'))";
            }
        } else {
            SQLString=T2SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= sysdate OR valid_from is NULL) AND (valid_to > sysdate OR valid_to is NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                //no need to
            } else if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",sysdate,sysdate))";
            } else if (SQLString.startsWith("UPDATE")){
                int point1u=SQLString.indexOf("geometry");
                point1u=SQLString.indexOf('=',point1u);
                int point2u=SQLString.indexOf(" WHERE ");
                int point3u=SQLString.indexOf("move_id", point2u);
                point3u=SQLString.indexOf('=',point3u);
                String move_id=(SQLString.substring(point3u+1)).trim();
                if (move_id.indexOf(' ')>0) move_id.substring(0, move_id.indexOf(' '));
                String geometry=(SQLString.substring(point1u+1,point2u)).trim();
                SQLString="CALL(animal_movement_update("+geometry+", "+move_id+",sysdate,sysdate)";
            }
        }
        Log.debug("*"+SQLString);
        return SQLString;
    }

    /**
     * Generates T2SQL validation prefix
     * @return T2SQL string
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDate(java.util.Date)
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static String T2SQLprefix(){
        String SQLstring="";
        if (mode.equals("NONSEQUENCED VALIDTIME")){
            SQLstring=mode+" ";
        } else if (mode.equals("VALIDTIME DATE")){
            SQLstring=mode+" "+dateFormatIn.format(validFrom)+" ";
        } else if (mode.equals("VALIDTIME PERIOD")){
            SQLstring=mode+" ["+dateFormatIn.format(validFrom)+" - "+dateFormatIn.format(validTo)+") ";
        }
        return SQLstring;
    }

    /**
     * Function sets temporal mode
     * @param mode1
     *          String with desired mode
     */
    private static void setMode(String mode1){
        mode=mode1;
    }
}
