/*
 * User: Leos Literak
 * Date: Jan 12, 2003
 * Time: 4:30:34 PM
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.servlets.Constants;

import java.util.Date;
import java.text.ParseException;

/**
 * Various methods for date manipulation
 * in template.
 */
public class DateTool {
    /** 2003-01-12 16:00 */
    public static final String ISO_FORMAT = "ISO";
    /** 1.1. 16:00 */
    public static final String CZ_SHORT = "CZ_SHORT";
    /** 1.1.2003 16:00 */
    public static final String CZ_FULL = "CZ_FULL";

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
}
