/*
 * User: literakl
 * Date: Jan 24, 2002
 * Time: 3:25:09 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets;

import java.text.SimpleDateFormat;
import java.text.DateFormat;

/**
 * This interface holds all constants shared across servlets.
 */
public interface Constants {

    /** actual ids of selected categories in database */
    public static final int CAT_ARTICLES = 1;
    public static final int CAT_HARDWARE = 2;
    public static final int CAT_SOFTWARE = 3;
    public static final int CAT_ABC = 4;
    public static final int CAT_ACTUAL_ARTICLES = 8;
    public static final int CAT_386 = 10;
    public static final int CAT_POLLS = 240;
    public static final int CAT_LINKS = 14;
    public static final int CAT_DRIVERS = 13;
    public static final int CAT_AUTHORS = 247;
    public static final int CAT_REQUESTS = 256;
    public static final int CAT_FORUM = 265;

    /** actual ids of selected relations in database */
    public static final int REL_POLLS = 250;
    public static final int REL_AUTHORS = 314;
    public static final int REL_DRIVERS = 318;
    public static final int REL_LINKS =  319;
    public static final int REL_ACTUAL_ARTICLES = 8;
    public static final int REL_REQUESTS =  3500;
    public static final int REL_FORUM = 3739;

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat defaultFormat = new SimpleDateFormat("d. M. yyyy HH:mm");
    DateFormat discussionFormat = new SimpleDateFormat("d.M. HH:mm");
}
