/*
 * User: Leos Literak
 * Date: Jan 12, 2003
 * Time: 4:30:34 PM
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.Date;
import java.util.Calendar;
import java.util.prefs.Preferences;
import java.text.ParseException;

import org.apache.log4j.Logger;

/**
 * Various methods for date manipulation
 * in template.
 */
public class DateTool implements Configurable {
    static Logger log = Logger.getLogger(DateTool.class);
    static long yesterday, today, tommorow;
    static {
        calculateTodayTimes();
        ConfigurationManager.getConfigurator().configureAndRememberMe(new DateTool());
    }

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
    /** 16:00 */
    public static final String ONLY_TIME = "TIME";
    /** Ctvrtek */
    public static final String CZ_ONLY_DAY = "CZ_DAY";

    private static final int DAY_DURATION = 24*60*60*1000;

    private static final String PREF_TODAY = "today";
    private static final String PREF_YESTERDAY = "yesterday";

    private static String textToday, textYesterday;

    /**
     * Formats given date according to selected format.
     */
    public String show(Date date, String format) {
        if ( date==null ) return null;

        if ( ISO_FORMAT.equalsIgnoreCase(format) )
            return Constants.isoFormat.format(date);
        if ( ONLY_TIME.equalsIgnoreCase(format) )
            return Constants.czTimeOnly.format(date);

        long ms = date.getTime();
        boolean dayNotText = ms<yesterday || ms>tommorow;

        if ( CZ_SHORT.equalsIgnoreCase(format) ) {
            if (dayNotText)
                return Constants.czShortFormat.format(date);
            else
                return getCzDay(ms) + Constants.czTimeOnly.format(date);
        }
        if ( CZ_FULL.equalsIgnoreCase(format) ) {
            if (dayNotText)
                return Constants.czFormat.format(date);
            else
                return getCzDay(ms) + Constants.czTimeOnly.format(date);
        }
        if ( CZ_ONLY_DATE.equalsIgnoreCase(format) ) {
            if (dayNotText)
                return Constants.czDateOnly.format(date);
            else
                return getCzDay(ms);
        }
        if ( CZ_FULL_TEXT.equalsIgnoreCase(format) )
            return Constants.czFormatTxt.format(date);
        if ( CZ_ONLY_DAY.equalsIgnoreCase(format) )
            return Constants.czDay.format(date);

        log.warn("Neznamy format data: "+format);
        return null;
    }

    /**
     * @return text representation of date, ms must belong to today or yesterday.
     */
    private String getCzDay(long ms) {
        if (ms<today)
            return textYesterday;
        else
            return textToday;
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
     * Calculates cache values used to recognize today and yesterday.
     * This method uses calendar, so it is slow, but precise. It is
     * called at start up. For proper function this method or updateTodayTimes
     * shall be called each day.
     */
    public static synchronized void calculateTodayTimes() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        today = calendar.getTime().getTime();
        tommorow = today + DAY_DURATION;
        yesterday = today - DAY_DURATION;
    }

    /**
     * Updates cached values used to recognize today and yesterday.
     * It must be called exactly once a day, it moves cached values
     * one day in forward. It is very fast.
     */
    public static synchronized void updateTodayTimes() {
        long now = System.currentTimeMillis();
        if (now<tommorow) {
            log.warn("updateTodayTimes() shall be called next day!");
            return;
        }
        if (now>tommorow+DAY_DURATION) {
            log.warn("updateTodayTimes() has not been called next day!");
            calculateTodayTimes();
            return;
        }
        yesterday = today;
        today = tommorow;
        tommorow = today + DAY_DURATION;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        textToday = prefs.get(PREF_TODAY,null);
        textYesterday = prefs.get(PREF_YESTERDAY,null);
    }
}
