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
    int CAT_ARTICLES = 1;
    int CAT_HARDWARE = 2;
    int CAT_SOFTWARE = 3;
    int CAT_ABC = 4;
    int CAT_ACTUAL_ARTICLES = 8;
    int CAT_386 = 10;
    int CAT_POLLS = 240;
    int CAT_LINKS = 14;
    int CAT_DRIVERS = 13;
    int CAT_AUTHORS = 247;
    int CAT_REQUESTS = 256;
    int CAT_FORUM = 265;
    int CAT_ROOT = 248;
    int CAT_REKLAMA = 333;
    int CAT_SYSTEM = 338;
    int CAT_ARTICLEPOOL = 339;

    // actual ids of selected relations in database
    int REL_POLLS = 250;
    int REL_REKLAMA = 308;
    int REL_AUTHORS = 314;
    int REL_ARTICLES = 315;
    int REL_HARDWARE = 316;
    int REL_SOFTWARE = 317;
    int REL_DRIVERS = 318;
    int REL_LINKS =  319;
    int REL_ACTUAL_ARTICLES = 5;
    int REL_REQUESTS =  3500;
    int REL_FORUM = 3739;
    int REL_ABC = 5187;
    int REL_KOMERCE = 7223;
    int REL_SYSTEM = 8000;
    int REL_ARTICLEPOOL = 8082;

    // types for VelocityHelper.groupByType()
    String TYPE_MAKE = "make";
    String TYPE_ARTICLE = "article";
    String TYPE_DISCUSSION = "discussion";
    String TYPE_REQUEST = "request";
    String TYPE_DRIVER = "driver";
    String TYPE_CATEGORY = "category";
    String TYPE_RECORD = "record";

    // template variables

    /** holds VariableFetcher */
    String VAR_FETCHER = "VARS";
    /** holds category Rubriky */
    String VAR_RUBRIKY = "RUBRIKY";
    /** holds category Abclinuxu */
    String VAR_ABCLINUXU = "ABCLINUXU";
    /** holds category Reklama */
    String VAR_REKLAMA = "REKLAMA";
    /** holds category Links */
    String VAR_LINKS = "LINKS";
    /** holds current user instance */
    String VAR_USER = "USER";
    /** holds request's parameters */
    String VAR_PARAMS = "PARAMS";
    /** holds url manipulation tool */
    String VAR_URL_UTILS = "URL";
    /** holds map of errors for last request */
    String VAR_ERRORS = "ERRORS";
    /** holds list of messages for last request */
    String VAR_MESSAGES = "MESSAGES";
    /** holds Tools instance */
    String VAR_TOOL = "TOOL";
    /** holds Sorters2 instance */
    String VAR_SORTER = "SORT";
    /** holds DateTool instance */
    String VAR_DATE_TOOL = "DATE";

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat czFormat = new SimpleDateFormat("d. M. yyyy HH:mm");
    DateFormat czShortFormat = new SimpleDateFormat("d.M. HH:mm");
}
