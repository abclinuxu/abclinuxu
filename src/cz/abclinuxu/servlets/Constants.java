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
    int CAT_SYSTEM = 338;
    int CAT_ARTICLES_POOL = 339;
    int CAT_NEWS_POOL = 445;
    int CAT_NEWS = 452;
    int CAT_DICTIONARY = 569;
    int CAT_TIPS_POOL = 571;
    int CAT_TIPS = 572;
    int CAT_DOCUMENTS = 587;
    int CAT_BLOGS = 589;

    int ITEM_DIZ_TODO = 11606;
    int REC_DIZ_TODO = 38280;

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
    int REL_NEWS_POOL = 37672;
    int REL_NEWS = 42932;
    int REL_DIZ_TODO = 50795;
    int REL_DICTIONARY = 60058;
    int REL_TIPS_POOL = 61138;
    int REL_TIPS = 61139;
    int REL_DOCUMENTS = 66948;
    int REL_BLOGS = 69275;

    int GROUP_ADMINI = 11246;
    int GROUP_AUTORI = 11247;

    // types for Tools.groupByType()
    String TYPE_MAKE = "make";
    String TYPE_ARTICLE = "article";
    String TYPE_DISCUSSION = "discussion";
    String TYPE_REQUEST = "request";
    String TYPE_DRIVER = "driver";
    String TYPE_NEWS = "news";
    String TYPE_ROYALTIES = "royalties";
    String TYPE_DOCUMENTS = "documents";
    String TYPE_CATEGORY = "category";
    String TYPE_RECORD = "record";
    String TYPE_USER = "user";

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
    /** holds request's URI */
    String VAR_REQUEST_URI = "REQUEST_URI";
    /** holds Tools instance */
    String VAR_TOOL = "TOOL";
    /** holds Sorters2 instance */
    String VAR_SORTER = "SORT";
    /** holds DateTool instance */
    String VAR_DATE_TOOL = "DATE";
    /** holds instance of NewsCategories */
    String VAR_CATEGORIES = "NEWS_CATEGORIES";

    /** error, that is not related to specific form field */
    String ERROR_GENERIC = "generic";

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat isoFormatShort = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat czFormat = new SimpleDateFormat("d.M.yyyy HH:mm");
    DateFormat czFormatTxt = new SimpleDateFormat("d. MMMMM yyyy HH:mm");
    DateFormat czDateOnly = new SimpleDateFormat("d. M. yyyy");
    DateFormat czTimeOnly = new SimpleDateFormat("HH:mm");
    DateFormat czShortFormat = new SimpleDateFormat("d.M. HH:mm");
    DateFormat czDayMonth = new SimpleDateFormat("d.M.");
    DateFormat czDay = new SimpleDateFormat("EEEE");
}
