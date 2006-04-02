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
    int CAT_GUESTBOOK = 885;

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
    int REL_GUESTBOOK = 93214;
    int REL_FAQ = 81515;

    int GROUP_ADMINI = 11246;
    int GROUP_AUTORI = 11247;
    int GROUP_STICKFISH = 12651;
    int GROUP_TEAM_ABCLINUXU = 20468;

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
    String TYPE_POLL = "poll";

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

    int USER_REDAKCE = 5473;

    String PAGE_WAP = "wap";
    String PAGE_INDEX = "homepage";
    String PAGE_ARTICLES = "clanky";
    String PAGE_NEWS = "zpravicky";
    String PAGE_FORUM = "forum";
    String PAGE_BLOGS = "blogy";
    String PAGE_FAQ = "faq";
    String PAGE_HARDWARE = "hardware";
    String PAGE_SOFTWARE = "software";
    String PAGE_SCHOOLBOOK = "ucebnice";
    String PAGE_DICTIONARY = "slovnik";
    String PAGE_POLLS = "ankety";
    String PAGE_DRIVERS = "ovladace";
    String PAGE_HOSTING = "hosting";

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat isoFormatShort = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat czFormat = new SimpleDateFormat("d.M.yyyy HH:mm");
    DateFormat czFormatTxt = new SimpleDateFormat("d. MMMMM yyyy HH:mm");
    DateFormat czShortFormat = new SimpleDateFormat("d.M. HH:mm");
    DateFormat czDayMonthYear = new SimpleDateFormat("d. M. yyyy");
    DateFormat czDayMonth = new SimpleDateFormat("d.M.");
    DateFormat czTimeOnly = new SimpleDateFormat("HH:mm");
    DateFormat czDay = new SimpleDateFormat("EEEE");
}
