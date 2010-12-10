package cz.vutbr.fit.pdb03;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
// TODO overit delete a ze to vsechno dela co ma
/**
 * Třída pro práci s jazykem T2SQL
 * Definuje nový jazyk T2SQL (TimeToSQL). Něco jako jednoduchý TSQL2 nebo TimeDB
 * <p>
 * Definice:
 * </p><p>
 * - VALIDTIME PERIOD [<Date>,<Date>) <SQL>
 *  transformuje SQL pouze pro platná data (objevují se někde v časovém intervalu)
 * </p><p>
 * - VALIDTIME DATE <Date> <SQL>
 *  transformuje SQL pouze pro platná data (platná v určitém čase)
 * </p><p>
 * - <SQL>
 *  transformuje SQL pouze pro platná data (platná v aktuálním okamžiku)
 * </p><p>
 * - NONSEQUENCED VALIDTIME <SQL>
 *  pouze odebrán prefix (NONSEQUENCED VALIDTIME ). SQL nad všemi daty v databázi.
 * </p>
 *  <SQL> je příkaz v jazyce SQL
 *  <Date> je čas ve formátu yyyy/MM/dd
 *
 * @author Tomáš Ižák <xizakt00@stud.fit.vutbr.cz>
 */
public final class T2SQL {
    private static Date validFrom=null;
    private static Date validTo=null;
    private static String mode="";
    /**
     * Formát data pro vložení do dotazu
     */
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");//("dd-MM-yyyy HH:mm:ss");
    private static DateFormat dateFormatIn = new SimpleDateFormat("yyyy/MM/dd");//("yyyy/MM/dd'~'HH:mm:ss");
    /**
     * Konstanta pro NONSEQUENCED VALIDTIME - bez časového omezení
     */
    public static final String NO_RESTRICTIONS = "NONSEQUENCED VALIDTIME";
    /**
     * Konstanta pro VALIDTIME PERIOD - platné v časovém intervalu
     */
    public static final String INTERVAL = "VALIDTIME PERIOD";
    /**
     * Konstanta pro VALIDTIME DATE - platné v určitém čase
     */
    public static final String DATETIME = "VALIDTIME DATE";
    /**
     * konstanta pro NOW - platné v aktuálním okamžiku
     */
    public static final String NOW = "";

    /**
     * Funkce odstraní časové omezení
     * @see #setCurrentTime()
     * @see #setValidationDate(java.util.Date)
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static void setNoTemporalRestrictions(){
        setMode(NO_RESTRICTIONS);
        validFrom=null;
        validTo=null;
    }

    /**
     * Funkce nastaví VALIDTIME PERIOD
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDate(java.util.Date)
     * @param from
     *          Začátek platnosti
     * @param to
     *          Konec platnosti
     */
    public static void setValidationDates(Date from, Date to){
        validFrom=from;
        validTo=to;
        setMode(INTERVAL);
    }

    /**
     * Funkce nastaví platnost dat pouze pro aktuální okamžik
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDate(java.util.Date)
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static void setCurrentTime(){
        setValidationDates(null,null);
        setMode(NOW);
    }

    /**
     * Funkce nastaví platnost dat vůči určitému datu
     * @param date
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static void setValidationDate(Date date){
        setValidationDates(date,date);
        setMode(DATETIME);
    }

    /**
     * Zjistí počátek platnosti dat
     * @return Datum platnosti od
     */
    public static Date getValidationDateFrom(){
        return validFrom;
    }

    /**
     * Zjištění konce platnosti
     * @return konec platnosti
     */
    public static Date getValidationDateTo(){
        return validTo;
    }

    /**
     * Zjištěné módu
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDates(java.util.Date, java.util.Date)
     * @see #setValidationDate(java.util.Date)
     * @see #DATETIME
     * @see #INTERVAL
     * @see #NOW
     * @see #NO_RESTRICTIONS
     * @return Nastavený mód
     */
    public static String getMode(){
        return mode;
    }

    /**
     * Konverze z jazyka T2SQL do SQL
     * Pracuje se SELECT, INSERT, UPDATE and DELETE
     * Omezení: jednoduché dotazy, DELETE a UPDATE mají formát ...WHERE move_id=<číslo>
     * @param T2SQLString
     *          T2SQL příkaz
     * @return SQL příkaz
     */
    public static String temporal(String T2SQLString){
        T2SQLString=T2SQLString.trim();
        String SQLString="";
        if (T2SQLString.startsWith(NO_RESTRICTIONS+" ")){
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
        } else if (T2SQLString.startsWith(INTERVAL+" [")){
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
                SQLString=SQLString.replace(" WHERE ", " WHERE (((valid_from <= DATE '"+dateFormat.format(day_from)+"' OR valid_from is NULL) AND (valid_to > DATE '"
                        +dateFormat.format(day_from)+"' OR valid_to is NULL)) OR ((valid_from <= DATE '"+dateFormat.format(day_to)
                        +"' OR valid_from is NULL) AND (valid_to > DATE '"+dateFormat.format(day_to)+"' OR valid_to is NULL))) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,valid_to,");
                SQLString=SQLString.replace(" VALUES (", " VALUES (DATE '"+dateFormat.format(day_from)+"',DATE '"+dateFormat.format(day_to)+"',");
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
        } else if (T2SQLString.startsWith(DATETIME+" ")){
            int point1=15;
            while (T2SQLString.charAt(point1)==' '){
                point1++;
            }
            int point2=T2SQLString.indexOf(' ',point1);
            Date day=null;
            try{
                day=dateFormatIn.parse(T2SQLString.substring(point1, point2));
            } catch(ParseException e){ }
            SQLString=T2SQLString.substring(point2+1);
            SQLString=SQLString.trim();
            if (SQLString.startsWith("SELECT")){
                SQLString=SQLString.replace(" WHERE ", " WHERE (valid_from <= DATE '"+dateFormat.format(day)+"' OR valid_from is NULL) AND (valid_to > DATE '"+dateFormat.format(day)+"' OR valid_to is NULL) AND ");
            } else if (SQLString.startsWith("INSERT")){
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,");
                SQLString=SQLString.replace(" VALUES (", " VALUES (DATE '"+dateFormat.format(day)+"',");
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
                SQLString=SQLString.replace("INSERT INTO animal_movement (", "INSERT INTO animal_movement (valid_from,");
                SQLString=SQLString.replace(" VALUES (", " VALUES (sysdate,");
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
     * Vytváří T2SQL prefix
     * @return T2SQL prefix
     * @see #setCurrentTime()
     * @see #setNoTemporalRestrictions()
     * @see #setValidationDate(java.util.Date)
     * @see #setValidationDates(java.util.Date, java.util.Date)
     */
    public static String T2SQLprefix(){
        String SQLstring="";
        if (mode.equals(NO_RESTRICTIONS)){
            SQLstring=mode+" ";
        } else if (mode.equals(DATETIME)){
            SQLstring=mode+" "+dateFormatIn.format(validFrom)+" ";
        } else if (mode.equals(INTERVAL)){
            SQLstring=mode+" ["+dateFormatIn.format(validFrom)+" - "+dateFormatIn.format(validTo)+") ";
        }
        return SQLstring;
    }

    /**
     * Funkce nastaví mód temporálních dat
     * @param mode1
     *          Zvolený mód
     */
    private static void setMode(String mode1){
        mode=mode1;
    }
}
