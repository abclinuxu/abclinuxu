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
    int CAT_FAQ = 758;
    int CAT_GUESTBOOK = 885;
    int CAT_BAZAAR = 1584;
    int CAT_TRIVIA = 1605;
    int CAT_HANGMAN = 1606;
    int CAT_SCREENSHOTS = 1949;
    int CAT_SERIES = 1647;
    int CAT_SURVEY = 1679;
    int CAT_PERSONALITIES = 1863;
	int CAT_SUBPORTALS = 2116;
	int CAT_EVENTS = 2117;
    int CAT_VIDEOS = 2123;

    int ITEM_DIZ_TODO = 11606;
    /** item holding dynamic configuration */
    int ITEM_DYNAMIC_CONFIGURATION = 59516;

    int REC_DIZ_TODO = 38280;

    int REL_POLLS = 250;
    int REL_REKLAMA = 308;
    int REL_AUTHORS = 314;
    int REL_ARTICLES = 315;
    int REL_HARDWARE = 316;
    int REL_HARDWARE_386 = 1;
    int REL_SOFTWARE = 317;
    int REL_DRIVERS = 318;
    int REL_LINKS =  319;
    int REL_REQUESTS =  3500;
    int REL_FORUM = 3739;
    int REL_FORUM_APPLICATIONS = 49655;
    int REL_FORUM_HARDWARE = 49488;
    int REL_FORUM_SETTINGS = 49489;
    int REL_FORUM_DISTRIBUTIONS = 49490;
    int REL_FORUM_VARIOUS = 51457;
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
    int REL_BAZAAR = 152995;
    int REL_GAMES = 156665;
    int REL_TRIVIA = 156666;
    int REL_HANGMAN = 156667;
    int REL_SCREENSHOTS = 200506;
    int REL_SERIES = 164193;
    int REL_SURVEY = 168884;
    int REL_PERSONALITIES = 190499;
	int REL_SUBPORTALS = 233273;
	int REL_EVENTS = 233274;
    int REL_VIDEOS = 234043;

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
    String TYPE_DATA = "data";
    String TYPE_USER = "user";
    String TYPE_POLL = "poll";
    String TYPE_LINK = "link";

    // more types, see EditRelated, Item and Item.dtd
    String TYPE_AUTHOR = "author";
    String TYPE_BAZAAR = "bazaar";
    String TYPE_BLOG = "blog";
    String TYPE_CONTENT = "content";
    String TYPE_DICTIONARY = "dictionary";
    String TYPE_EXTERNAL_DOCUMENT = "external";
    String TYPE_FAQ = "faq";
    String TYPE_HARDWARE = "hardware";
    String TYPE_OTHER = "other";
    String TYPE_PERSONALITY = "personality";
    String TYPE_QUESTION = "question";
    String TYPE_SECTION = "section";
    String TYPE_SERIES = "series";
    String TYPE_SOFTWARE = "software";
    String TYPE_SCREENSHOT = "screenshot";
    String TYPE_STORY = "story";
    String TYPE_EVENT = "event";
    String TYPE_VIDEO = "video";

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
    /** holds a FeedGenerator instance */
    String VAR_FEEDS = "FEEDS";
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
    String VAR_NEWS_CATEGORIES = "NEWS_CATEGORIES";
    /** holds instance of DriverCategories */
    String VAR_DRIVER_CATEGORIES = "DRIVER_CATEGORIES";
    /** holds Map where key is id of property value and value is its caption */
    String VAR_UI_PROPERTY_VALUES = "UI_PROPERTY";
    /** holds Map where key is id of property value and value is its caption */
    String VAR_LICENSE_PROPERTY_VALUES = "LICENSE_PROPERTY";
    /** name of user agent that wants to fetch the page */
    String VAR_USER_AGENT = "UA";
    /** boolean that indicates that user agent is some bot */
    String VAR_BOT_DETECTED = "BOT_DETECTED";
    /** instance of system configuration class */
    String VAR_CONFIG = "SYSTEM_CONFIG";
    /** uri (either relative or absolute) to css file to be used */
    String VAR_CSS_URI = "CSS_URI";
    /** way to override default text/html content type */
    String VAR_CONTENT_TYPE = "Content-Type";
    /** environment map */
    String VAR_ENVIRONMENT = "ENV";
    /** optional variable holding RSS url for current page */
    String VAR_RSS = "RSS";
    String VAR_ASSIGNED_TAGS = "ASSIGNED_TAGS";
    /** always present instance of RichTextEditor class */
    String VAR_RICH_TEXT_EDITOR = "RTE";

    /** parameter holding description of changes */
    String PARAM_REVISION_DESCRIPTION = "rev_descr";
    /**
     * n-th oldest object, where display from
     */
    String PARAM_FROM = "from";
    /**
     * how many object to display
     */
    String PARAM_COUNT = "count";
    /**
     * specifies attribute, by which data shall be sorted
     */
    String PARAM_ORDER_BY = "orderBy";
    /**
     * specifies direction of sort order
     */
    String PARAM_ORDER_DIR = "orderDir";

    String PARAM_RELATION = "rid";
    String PARAM_TITLE = "title";
    String PARAM_NAME = "name";
    String PARAM_DESCRIPTION = "desc";
    String PARAM_PEREX = "perex";
    String PARAM_CONTENT = "content";

    String ORDER_BY_CREATED = "create";
    String ORDER_BY_UPDATED = "update";
    String ORDER_BY_WHEN = "date";
    String ORDER_BY_RELEVANCE = "relevance";
    String ORDER_BY_ID = "id";
    String ORDER_BY_TITLE = "title";
    String ORDER_BY_COUNT = "count";
    String ORDER_DIR_ASC = "asc";
    String ORDER_DIR_DESC = "desc";

    /** error, that is not related to specific form field */
    String ERROR_GENERIC = "generic";

    String ERROR = "ERROR";

    int USER_REDAKCE = 5473;

    String PAGE_ARTICLES = "clanky";
    String PAGE_BAZAAR = "bazar";
    String PAGE_BLOGS = "blogy";
    String PAGE_DICTIONARY = "slovnik";
    String PAGE_PERSONALITIES = "kdo-je";
    String PAGE_DRIVERS = "ovladace";
    String PAGE_FAQ = "faq";
    String PAGE_FORUM = "forum";
    String PAGE_GAMES = "hry";
    String PAGE_HARDWARE = "hardware";
    String PAGE_HOSTING = "hosting";
    String PAGE_INDEX = "homepage";
    String PAGE_NEWS = "zpravicky";
    String PAGE_POLLS = "ankety";
    String PAGE_SOFTWARE = "software";
    String PAGE_SCHOOLBOOK = "ucebnice";
    String PAGE_SCREENSHOTS = "desktopy";
    String PAGE_SEARCH = "hledani";
    String PAGE_TAGS = "stitky";
    String PAGE_WAP = "wap";

    String EMAIL_FORUM = "email-forum";
    String EMAIL_WEEKLY = "email-weekly";
    String EMAIL_SCRIPT = "email-script";
    String EMAIL_USER_MESSAGE = "email-message";
    String EMAIL_MONITOR_DRIVER = "email-monitor-driver";
    String EMAIL_MONITOR_FAQ = "email-monitor-faq";
    String EMAIL_MONITOR_ITEM = "email-monitor-item";
    String EMAIL_MONITOR_DISCUSSION = "email-monitor-discussion";
    String EMAIL_FORGOTTEN_PASSWORD = "email-forgotten-password";
    String EMAIL_UNKNOWN = "email-unknown";

    /** value for subtype that marks discussion item as question */
    String SUBTYPE_QUESTION = "question";

    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    DateFormat isoFormatShort = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat isoSearchFormat = new SimpleDateFormat("yyyyMMdd");
    DateFormat isoLongFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat usFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    DateFormat czFormat = new SimpleDateFormat("d.M.yyyy HH:mm");
    DateFormat czFormatTxt = new SimpleDateFormat("d. MMMMM yyyy HH:mm");
    DateFormat czShortFormat = new SimpleDateFormat("d.M. HH:mm");
    DateFormat czDayMonthYearSpaces = new SimpleDateFormat("d. M. yyyy");
    DateFormat czDayMonthYear = new SimpleDateFormat("d.M.yyyy");
    DateFormat czDayMonthYearTxt = new SimpleDateFormat("d. MMMMM yyyy");
    DateFormat czDayMonth = new SimpleDateFormat("d.M.");
    DateFormat czDayMonthTxt = new SimpleDateFormat("d. MMMMM");
    DateFormat czTimeOnly = new SimpleDateFormat("HH:mm");
    DateFormat czDay = new SimpleDateFormat("EEEE");

    // see properties.txt for more information
    String PROPERTY_USER_INTERFACE = "ui";
    String PROPERTY_ALTERNATIVE_SOFTWARE = "alternative";
    String PROPERTY_LICENSE = "license";
    String PROPERTY_BLOG_DIGEST = "digest";
    String PROPERTY_BANNED_BLOG = "banned_blog";
    String PROPERTY_AUTHOR = "author";
    String PROPERTY_USER = "user";
    String PROPERTY_SCORE = "score";
    String PROPERTY_TICKET = "ticket";
    String PROPERTY_USED_BY = "used_by";
	String PROPERTY_MEMBER = "member";
	String PROPERTY_REGION = "region";
    String PROPERTY_FAVOURITED_BY = "favourited_by";

    // see EditBazaar
    String BAZAAR_BUY = "buy";
    String BAZAAR_SELL = "sell";
    String BAZAAR_GIVE = "give";

    // see RichTextEditor
    String INPUT_MODE_COMMENT = "comment";
    String INPUT_MODE_GENERIC = "generic";
    String INPUT_MODE_NEWS = "news";
    String INPUT_MODE_BLOG = "blog";
    String INPUT_MODE_WIKI = "wiki";

    // type of counter
    String COUNTER_READ = "read";
    String COUNTER_VISIT = "visit";
    String COUNTER_PLAY = "play";
}
