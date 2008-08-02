/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.exceptions.InvalidInputException;

import java.util.Date;
import java.util.Calendar;
import java.util.prefs.Preferences;
import java.text.ParseException;
import java.text.DateFormat;

import org.apache.log4j.Logger;

/**
 * Various methods for date manipulation
 * in template.
 */
public class DateTool implements Configurable {
    static Logger log = Logger.getLogger(DateTool.class);
    static long yesterday, today, tommorow, thisYear;
    static {
        calculateTodayTimes();
        ConfigurationManager.getConfigurator().configureAndRememberMe(new DateTool());
    }

    /** 2003-01-01 16:00 */
    public static final String ISO_FORMAT = "ISO";
    /** 2003-01-01 */
    public static final String ISO_ONLY_DATE = "ISO_DMY";
    /** 01/01/2003 16:00:00 (poradi MM/DD/YYYY) */
    public static final String US_FULL = "US_FULL";
    /** 1.1. 16:00 */
    public static final String CZ_SHORT = "CZ_SHORT";
    /** 1.1.2003 16:00 */
    public static final String CZ_FULL = "CZ_FULL";
    /** 1. leden 2003 16:00 */
    public static final String CZ_FULL_TEXT = "CZ_FULL_TXT";
    /** 1. 1. 2003 */
    public static final String CZ_DAY_MONTH_YEAR_SPACES = "CZ_DMY";
    /** 1.1.2003 */
    public static final String CZ_DAY_MONTH_YEAR = "CZ_DMY2";
    /** 1.1. */
    public static final String CZ_DAY_MONTH = "CZ_DM";
    /** 16:00 */
    public static final String ONLY_TIME = "TIME";
    /** Ctvrtek */
    public static final String CZ_ONLY_DAY = "CZ_DAY";
    /** Selects best way according to distance from now */
    public static final String CZ_SMART = "SMART";
    /** Selects best way according to distance from now, time is not included */
    public static final String CZ_SMART_DAY_MONTH_YEAR = "SMART_DMY";
    /** Selects best way according to distance from now, time is not included, month is expressed in word */
    public static final String CZ_SMART_DAY_MONTH_YEAR_TXT = "SMART_DMY_TXT";

    private static final int DAY_DURATION = 24*60*60*1000;

    private static final String PREF_TODAY = "today";
    private static final String PREF_YESTERDAY = "yesterday";

    private static String textToday, textYesterday;

    /**
     * Formats given date according to selected format.
     * Current day and yesterday will be displayed as text.
     */
    public String show(Date date, String format) {
        return show(date, format, true);
    }

    /**
     * Formats given date according to selected format.
     * If specialCurrentDayHandling is set, then text
     * representation will be rendered instead of datum.
     */
    public String show(Date date, String format, boolean specialCurrentDayHandling) {
        if ( date==null ) return null;

        if ( ISO_FORMAT.equalsIgnoreCase(format) )
            return renderDate(Constants.isoFormat, date);

        if ( ONLY_TIME.equalsIgnoreCase(format) )
            return renderDate(Constants.czTimeOnly, date);

        long timeToRender = date.getTime();
        boolean notTodayOrYesterday = true;
        if (specialCurrentDayHandling)
            notTodayOrYesterday = timeToRender < yesterday || timeToRender > tommorow;

        if ( CZ_SHORT.equalsIgnoreCase(format) ) {
            if (notTodayOrYesterday)
                return renderDate(Constants.czShortFormat, date);
            else
                return getCzDay(timeToRender) + " " + renderDate(Constants.czTimeOnly, date);
        }

        if (CZ_SMART.equalsIgnoreCase(format)) {
            if (notTodayOrYesterday) {
                if (timeToRender < thisYear)
                    return renderDate(Constants.czFormat, date);
                else
                    return renderDate(Constants.czShortFormat, date);
            } else
                return getCzDay(timeToRender) + " " + renderDate(Constants.czTimeOnly, date);
        }

        if (CZ_SMART_DAY_MONTH_YEAR.equalsIgnoreCase(format)) {
            if (notTodayOrYesterday) {
                if (timeToRender < thisYear)
                    return renderDate(Constants.czDayMonthYearSpaces, date);
                else
                    return renderDate(Constants.czDayMonth, date);
            } else
                return renderDate(Constants.czTimeOnly, date);
        }

        if (CZ_SMART_DAY_MONTH_YEAR_TXT.equalsIgnoreCase(format)) {
            if (notTodayOrYesterday) {
                if (timeToRender < thisYear)
                    return renderDate(Constants.czDayMonthYearTxt, date);
                else
                    return renderDate(Constants.czDayMonthTxt, date);
            } else
                return getCzDay(timeToRender);
        }

        if ( CZ_FULL.equalsIgnoreCase(format) ) {
            if (notTodayOrYesterday)
                return renderDate(Constants.czFormat, date);
            else
                return getCzDay(timeToRender) + " " + renderDate(Constants.czTimeOnly, date);
        }

        if ( CZ_DAY_MONTH_YEAR_SPACES.equalsIgnoreCase(format) ) {
            if (notTodayOrYesterday)
                return renderDate(Constants.czDayMonthYearSpaces, date);
            else
                return getCzDay(timeToRender);
        }

        if ( CZ_DAY_MONTH_YEAR.equalsIgnoreCase(format) ) {
            if (notTodayOrYesterday)
                return renderDate(Constants.czDayMonthYear, date);
            else
                return getCzDay(timeToRender);
        }

        if ( CZ_DAY_MONTH.equalsIgnoreCase(format) ) {
            if (notTodayOrYesterday)
                return renderDate(Constants.czDayMonth, date);
            else
                return getCzDay(timeToRender);
        }

        if ( CZ_FULL_TEXT.equalsIgnoreCase(format) )
            return renderDate(Constants.czFormatTxt, date);

        if ( CZ_ONLY_DAY.equalsIgnoreCase(format) )
            return renderDate(Constants.czDay, date);

        if ( US_FULL.equalsIgnoreCase(format) )
            return renderDate(Constants.usFormat, date);

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
     * Renders a date using given format with neccessary synchronization.
     * @param format format
     * @param date date
     * @return rendered date
     */
    private String renderDate(DateFormat format, Date date) {
        synchronized (format) {
            return format.format(date);
        }
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
        Date d;
        synchronized(Constants.isoFormat) {
            d = Constants.isoFormat.parse(date);
        }
        return show(d,format);
    }

    /**
     * Parses string in given format and formats it according to selected format.
     * @param inputFormat identifier of format used in date
     * @param outputFformat identifier of format used for output
     */
    public String show(String date, String inputFormat, String outputFformat) throws ParseException {
        Date d = null;

        if (ISO_FORMAT.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.isoFormat) {
                d = Constants.isoFormat.parse(date);
            }
        }

        if (ISO_ONLY_DATE.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.isoFormatShort) {
                d = Constants.isoFormatShort.parse(date);
            }
        }

        if (ONLY_TIME.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czTimeOnly) {
                d = Constants.czTimeOnly.parse(date);
            }
        }

        if (CZ_SHORT.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czShortFormat) {
                d = Constants.czShortFormat.parse(date);
            }
        }

        if (CZ_FULL.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czFormat) {
                d = Constants.czFormat.parse(date);
            }
        }

        if (CZ_DAY_MONTH_YEAR_SPACES.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czDayMonthYearSpaces) {
                d = Constants.czDayMonthYearSpaces.parse(date);
            }
        }

        if (CZ_DAY_MONTH.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czDayMonth) {
                d = Constants.czDayMonth.parse(date);
            }
        }

        if (CZ_FULL_TEXT.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czFormatTxt) {
                d = Constants.czFormatTxt.parse(date);
            }
        }

        if (CZ_ONLY_DAY.equalsIgnoreCase(inputFormat)) {
            synchronized (Constants.czDay) {
                d = Constants.czDay.parse(date);
            }
        }

        if (d == null)
            throw new InvalidInputException("Input format '"+inputFormat+"' not recognized!");
        return show(d, outputFformat);
    }
    
    /**
     * Returns the current date and time.
     * Useful in Freemarker templates
     */
    public Date now() {
        return new Date();
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
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.MONTH, 1);
        thisYear = calendar.getTime().getTime();
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        textToday = prefs.get(PREF_TODAY,null);
        textYesterday = prefs.get(PREF_YESTERDAY,null);
    }
}
