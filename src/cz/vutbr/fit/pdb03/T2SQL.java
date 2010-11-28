package cz.vutbr.fit.pdb03;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Class for T2SQL support
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public final class T2SQL {
    private static Date validFrom=null;
    private static Date validTo=null;
    private static String mode="";
    private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static DateFormat dateFormatIn = new SimpleDateFormat("yyyy/MM/dd'~'HH:mm");

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
     * Conversion from T2SQL notation to SQL notation
     * Works for SELECT and INSERT
     * Verify for DELETE
     * DO for UPDATE
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
                SQLString="CALL()";
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
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= '"+dateFormat.format(day_from)+"') AND (valid_to > '"+dateFormat.format(day_to)+"' OR valid_to=NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,valid_to,");
                SQLString=SQLString.replace(" VALUES (", " VALUES ('"+dateFormat.format(day_from)+"','"+dateFormat.format(day_to)+"',");
            } else if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",'"+dateFormat.format(day_from)+"','"+dateFormat.format(day_to)+"'))";
            } else if (SQLString.startsWith("UPDATE")){
                SQLString="CALL()";
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
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= '"+dateFormat.format(day)+"') AND (valid_to > '"+dateFormat.format(day)+"' OR valid_to=NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,");
                SQLString=SQLString.replace(" VALUES (", " VALUES ('"+dateFormat.format(day)+"',");
            } else if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",'"+dateFormat.format(day)+"','"+dateFormat.format(day)+"'))";
            } else if (SQLString.startsWith("UPDATE")){
                SQLString="CALL()";
            }
        } else {
            SQLString=T2SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= sysdate) AND (valid_to > sysdate OR valid_to=NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                //no need to
            } else if (SQLString.startsWith("DELETE")){
                SQLString=SQLString.substring(SQLString.indexOf("move_id")+7);
                SQLString=(SQLString.substring(SQLString.indexOf("=")+1)).trim();
                SQLString="CALL(animal_movement_delete("+SQLString+",null,null))";
            } else if (SQLString.startsWith("UPDATE")){
                SQLString="CALL()";
            }
        }
        D.log(SQLString);
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
            SQLstring=mode+" "+dateFormat.format(validFrom)+" ";
        } else if (mode.equals("VALIDTIME PERIOD")){
            SQLstring=mode+" ["+dateFormat.format(validFrom)+" - "+dateFormat.format(validTo)+") ";
        }
        D.log(SQLstring);
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
