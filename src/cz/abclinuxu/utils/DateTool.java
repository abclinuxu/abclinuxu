/*
 * User: Leos Literak
 * Date: Jan 12, 2003
 * Time: 4:30:34 PM
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.servlets.Constants;

import java.util.Date;
import java.text.ParseException;

import org.apache.log4j.Logger;

/**
 * Various methods for date manipulation
 * in template.
 */
public class DateTool {
    static Logger log = Logger.getLogger(DateTool.class);

    /** 2003-01-01 16:00 */
    public static final String ISO_FORMAT = "ISO";
    /** 1.1. 16:00 */
    public static final String CZ_SHORT = "CZ_SHORT";
    /** 1.1.2003 16:00 */
    public static final String CZ_FULL = "CZ_FULL";
    /** 1. leden 2003 16:00 */
    public static final String CZ_FULL_TEXT = "CZ_FULL_TXT";
    /** 1.1.2003 */
    public static final String CZ_ONLY_DATE = "CZ_DATE";
    /** 1.1. or Dnes or Vcera */
    public static final String CZ_DAY_MONTH = "CZ_DM";
    /** 16:00 */
    public static final String CZ_ONLY_TIME = "CZ_TIME";
    /** Ctvrtek */
    public static final String CZ_ONLY_DAY = "CZ_DAY";

    /**
     * Formats given date according to selected format.
     */
    public String show(Date date, String format) {
        if ( date==null ) return null;

        if ( ISO_FORMAT.equalsIgnoreCase(format) )
            return Constants.isoFormat.format(date);
        if ( CZ_SHORT.equalsIgnoreCase(format) )
            return Constants.czShortFormat.format(date);
        if ( CZ_FULL.equalsIgnoreCase(format) )
            return Constants.czFormat.format(date);
        if ( CZ_FULL_TEXT.equalsIgnoreCase(format) )
            return Constants.czFormatTxt.format(date);
        if ( CZ_ONLY_DATE.equalsIgnoreCase(format) )
            return Constants.czDateOnly.format(date);
        if ( CZ_ONLY_TIME.equalsIgnoreCase(format) )
            return Constants.czTimeOnly.format(date);
        if ( CZ_ONLY_DAY.equalsIgnoreCase(format) )
            return Constants.czDay.format(date);
        if ( CZ_DAY_MONTH.equalsIgnoreCase(format) )
            return Constants.czDayMonth.format(date);

        log.warn("Neznamy format data: "+format);
        return null;
    }

    /**
     * Formats actual date according to selected format.
     */
    public String show(String format) {
        return show(new Date(),format);
    }

    /**
     * Parses string in ISO format and formats it according to selected format.
     */
    public String show(String date, String format) throws ParseException {
        Date d = Constants.isoFormat.parse(date);
        return show(d,format);
    }

    /**
     * @param date date to compare
     * @return true, if date is equal to today
     */
    public boolean isToday(Date date) {
//        Date now = new Date();
//        calendar.setTime(date);
//        calendar.
        return false;
    }
}
