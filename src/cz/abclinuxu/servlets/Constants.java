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

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.prefs.Preferences;

/**
 * This interface holds all constants shared across servlets.
 */
public class Constants implements Configurable {
    static {
        Constants constants = new Constants();
        ConfigurationManager.getConfigurator().configureAndRememberMe(constants);
    }
    
    //static String[] DYNAMIC_FIELDS = { "CAT_", "ITEM_", "REL_", "GROUP_", "USER_" };
    
    public static int CAT_ARTICLES;
    public static int CAT_HARDWARE;
    public static int CAT_SOFTWARE;
    public static int CAT_ABC;
    public static int CAT_386;
    public static int CAT_POLLS;
    public static int CAT_LINKS;
    public static int CAT_DRIVERS;
    public static int CAT_AUTHORS;
    public static int CAT_REQUESTS;
    public static int CAT_FORUM;
    public static int CAT_ROOT;
    public static int CAT_SYSTEM;
    public static int CAT_ARTICLES_POOL;
    public static int CAT_NEWS_POOL;
    public static int CAT_NEWS;
    public static int CAT_DICTIONARY;
    public static int CAT_TIPS_POOL;
    public static int CAT_TIPS;
    public static int CAT_DOCUMENTS;
    public static int CAT_BLOGS;
    public static int CAT_FAQ;
    public static int CAT_GUESTBOOK;
    public static int CAT_BAZAAR;
    public static int CAT_TRIVIA;
    public static int CAT_HANGMAN;
    public static int CAT_SCREENSHOTS;
    public static int CAT_SERIES;
    public static int CAT_SURVEY;
    public static int CAT_PERSONALITIES;
	public static int CAT_SUBPORTALS;
	public static int CAT_EVENTS;
    public static int CAT_VIDEOS;

    public static int ITEM_DIZ_TODO;
    /** item holding dynamic configuration */
    public static int ITEM_DYNAMIC_CONFIGURATION;

    public static int REC_DIZ_TODO;

    public static int REL_POLLS;
    public static int REL_REKLAMA;
    public static int REL_AUTHORS;
    public static int REL_ARTICLES;
    public static int REL_HARDWARE;
    public static int REL_HARDWARE_386;
    public static int REL_SOFTWARE;
    public static int REL_DRIVERS;
    public static int REL_LINKS;
    public static int REL_REQUESTS;
    public static int REL_FORUM;
    public static int REL_FORUM_APPLICATIONS;
    public static int REL_FORUM_HARDWARE;
    public static int REL_FORUM_SETTINGS;
    public static int REL_FORUM_DISTRIBUTIONS;
    public static int REL_FORUM_VARIOUS;
    public static int REL_ABC;
    public static int REL_KOMERCE;
    public static int REL_SYSTEM;
    public static int REL_ARTICLEPOOL;
    public static int REL_NEWS_POOL;
    public static int REL_NEWS;
    public static int REL_DIZ_TODO;
    public static int REL_DICTIONARY;
    public static int REL_TIPS_POOL;
    public static int REL_TIPS;
    public static int REL_DOCUMENTS;
    public static int REL_BLOGS;
    public static int REL_GUESTBOOK;
    public static int REL_FAQ;
    public static int REL_BAZAAR;
    public static int REL_GAMES;
    public static int REL_TRIVIA;
    public static int REL_HANGMAN;
    public static int REL_SCREENSHOTS;
    public static int REL_SERIES;
    public static int REL_SURVEY;
    public static int REL_PERSONALITIES;
	public static int REL_SUBPORTALS;
	public static int REL_EVENTS;
    public static int REL_VIDEOS;

    public static int GROUP_ADMINI;
    public static int GROUP_AUTORI;
    public static int GROUP_STICKFISH;
    public static int GROUP_TEAM_ABCLINUXU;

    // types for Tools.groupByType()
    public static final String TYPE_MAKE = "make";
    public static final String TYPE_ARTICLE = "article";
    public static final String TYPE_DISCUSSION = "discussion";
    public static final String TYPE_REQUEST = "request";
    public static final String TYPE_DRIVER = "driver";
    public static final String TYPE_NEWS = "news";
    public static final String TYPE_ROYALTIES = "royalties";
    public static final String TYPE_DOCUMENTS = "documents";
    public static final String TYPE_CATEGORY = "category";
    public static final String TYPE_RECORD = "record";
    public static final String TYPE_DATA = "data";
    public static final String TYPE_USER = "user";
    public static final String TYPE_POLL = "poll";
    public static final String TYPE_LINK = "link";
    public static final String TYPE_SERVER = "server";

    // more types, see EditRelated, Item and Item.dtd
    public static final String TYPE_AUTHOR = "author";
    public static final String TYPE_BAZAAR = "bazaar";
    public static final String TYPE_BLOG = "blog";
    public static final String TYPE_CONTENT = "content";
    public static final String TYPE_DICTIONARY = "dictionary";
    public static final String TYPE_EXTERNAL_DOCUMENT = "external";
    public static final String TYPE_FAQ = "faq";
    public static final String TYPE_HARDWARE = "hardware";
    public static final String TYPE_OTHER = "other";
    public static final String TYPE_PERSONALITY = "personality";
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_SECTION = "section";
    public static final String TYPE_SERIES = "series";
    public static final String TYPE_SOFTWARE = "software";
    public static final String TYPE_SCREENSHOT = "screenshot";
    public static final String TYPE_STORY = "story";
    public static final String TYPE_EVENT = "event";
    public static final String TYPE_VIDEO = "video";

    // template variables

    /** holds VariableFetcher */
    public static final String VAR_FETCHER = "VARS";
    /** holds category Rubriky */
    public static final String VAR_RUBRIKY = "RUBRIKY";
    /** holds category Abclinuxu */
    public static final String VAR_ABCLINUXU = "ABCLINUXU";
    /** holds category Reklama */
    public static final String VAR_REKLAMA = "REKLAMA";
    /** holds category Links */
    public static final String VAR_LINKS = "LINKS";
    /** holds current user instance */
    public static final String VAR_USER = "USER";
    /** holds request's parameters */
    public static final String VAR_PARAMS = "PARAMS";
    /** holds url manipulation tool */
    public static final String VAR_URL_UTILS = "URL";
    /** holds a FeedGenerator instance */
    public static final String VAR_FEEDS = "FEEDS";
    /** holds map of errors for last request */
    public static final String VAR_ERRORS = "ERRORS";
    /** holds list of messages for last request */
    public static final String VAR_MESSAGES = "MESSAGES";
    /** holds request's URI */
    public static final String VAR_REQUEST_URI = "REQUEST_URI";
    /** holds Tools instance */
    public static final String VAR_TOOL = "TOOL";
    /** holds Sorters2 instance */
    public static final String VAR_SORTER = "SORT";
    /** holds DateTool instance */
    public static final String VAR_DATE_TOOL = "DATE";
    /** holds instance of NewsCategories */
    public static final String VAR_NEWS_CATEGORIES = "NEWS_CATEGORIES";
    /** holds instance of DriverCategories */
    public static final String VAR_DRIVER_CATEGORIES = "DRIVER_CATEGORIES";
    /** holds Map where key is id of property value and value is its caption */
    public static final String VAR_UI_PROPERTY_VALUES = "UI_PROPERTY";
    /** holds Map where key is id of property value and value is its caption */
    public static final String VAR_LICENSE_PROPERTY_VALUES = "LICENSE_PROPERTY";
    /** name of user agent that wants to fetch the page */
    public static final String VAR_USER_AGENT = "UA";
    /** boolean that indicates that user agent is some bot */
    public static final String VAR_BOT_DETECTED = "BOT_DETECTED";
    /** instance of system configuration class */
    public static final String VAR_CONFIG = "SYSTEM_CONFIG";
    /** uri (either relative or absolute) to css file to be used */
    public static final String VAR_CSS_URI = "CSS_URI";
    /** way to override default text/html content type */
    public static final String VAR_CONTENT_TYPE = "Content-Type";
    /** environment map */
    public static final String VAR_ENVIRONMENT = "ENV";
    /** optional variable holding RSS url for current page */
    public static final String VAR_RSS = "RSS";
    public static final String VAR_ASSIGNED_TAGS = "ASSIGNED_TAGS";
    /** always present instance of RichTextEditor class */
    public static final String VAR_RICH_TEXT_EDITOR = "RTE";

    /** parameter holding description of changes */
    public static final String PARAM_REVISION_DESCRIPTION = "rev_descr";
    /**
     * n-th oldest object, where display from
     */
    public static final String PARAM_FROM = "from";
    /**
     * how many object to display
     */
    public static final String PARAM_COUNT = "count";
    /**
     * specifies attribute, by which data shall be sorted
     */
    public static final String PARAM_ORDER_BY = "orderBy";
    /**
     * specifies direction of sort order
     */
    public static final String PARAM_ORDER_DIR = "orderDir";

    public static final String PARAM_RELATION = "rid";
    public static final String PARAM_TITLE = "title";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_PEREX = "perex";
    public static final String PARAM_CONTENT = "content";

    public static final String ORDER_BY_CREATED = "create";
    public static final String ORDER_BY_UPDATED = "update";
    public static final String ORDER_BY_WHEN = "date";
    public static final String ORDER_BY_RELEVANCE = "relevance";
    public static final String ORDER_BY_ID = "id";
    public static final String ORDER_BY_TITLE = "title";
    public static final String ORDER_BY_COUNT = "count";
    public static final String ORDER_DIR_ASC = "asc";
    public static final String ORDER_DIR_DESC = "desc";

    /** error, that is not related to specific form field */
    public static final String ERROR_GENERIC = "generic";

    public static final String ERROR = "ERROR";

    public static int USER_REDAKCE;

    public static final String PAGE_ARTICLES = "clanky";
    public static final String PAGE_BAZAAR = "bazar";
    public static final String PAGE_BLOGS = "blogy";
    public static final String PAGE_DICTIONARY = "slovnik";
    public static final String PAGE_DRIVERS = "ovladace";
    public static final String PAGE_EVENTS = "udalosti";
    public static final String PAGE_FAQ = "faq";
    public static final String PAGE_FORUM = "forum";
    public static final String PAGE_GAMES = "hry";
    public static final String PAGE_GROUPS = "skupiny";
    public static final String PAGE_HARDWARE = "hardware";
    public static final String PAGE_HOSTING = "hosting";
    public static final String PAGE_INDEX = "homepage";
    public static final String PAGE_NEWS = "zpravicky";
    public static final String PAGE_PERSONALITIES = "kdo-je";
    public static final String PAGE_POLLS = "ankety";
    public static final String PAGE_SOFTWARE = "software";
    public static final String PAGE_SCHOOLBOOK = "ucebnice";
    public static final String PAGE_SCREENSHOTS = "desktopy";
    public static final String PAGE_SEARCH = "hledani";
    public static final String PAGE_TAGS = "stitky";
    public static final String PAGE_TOP = "nej";
    public static final String PAGE_UNKNOWN = "neznamy";
    public static final String PAGE_VIDEOS = "videos";
    public static final String PAGE_WAP = "wap";

    public static final String EMAIL_FORUM = "email-forum";
    public static final String EMAIL_WEEKLY = "email-weekly";
    public static final String EMAIL_SCRIPT = "email-script";
    public static final String EMAIL_USER_MESSAGE = "email-message";
    public static final String EMAIL_MONITOR_DRIVER = "email-monitor-driver";
    public static final String EMAIL_MONITOR_FAQ = "email-monitor-faq";
    public static final String EMAIL_MONITOR_ITEM = "email-monitor-item";
    public static final String EMAIL_MONITOR_DISCUSSION = "email-monitor-discussion";
    public static final String EMAIL_FORGOTTEN_PASSWORD = "email-forgotten-password";
    public static final String EMAIL_UNKNOWN = "email-unknown";

    /** value for subtype that marks discussion item as question */
    public static final String SUBTYPE_QUESTION = "question";

    public static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final DateFormat isoFormatShort = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat isoSearchFormat = new SimpleDateFormat("yyyyMMdd");
    public static final DateFormat isoLongFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat usFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static final DateFormat czFormat = new SimpleDateFormat("d.M.yyyy HH:mm");
    public static final DateFormat czFormatTxt = new SimpleDateFormat("d. MMMMM yyyy HH:mm");
    public static final DateFormat czShortFormat = new SimpleDateFormat("d.M. HH:mm");
    public static final DateFormat czDayMonthYearSpaces = new SimpleDateFormat("d. M. yyyy");
    public static final DateFormat czDayMonthYear = new SimpleDateFormat("d.M.yyyy");
    public static final DateFormat czDayMonthYearTxt = new SimpleDateFormat("d. MMMMM yyyy");
    public static final DateFormat czDayMonth = new SimpleDateFormat("d.M.");
    public static final DateFormat czDayMonthTxt = new SimpleDateFormat("d. MMMMM");
    public static final DateFormat czTimeOnly = new SimpleDateFormat("HH:mm");
    public static final DateFormat czDay = new SimpleDateFormat("EEEE");

    // see properties.txt for more information
    public static final String PROPERTY_USER_INTERFACE = "ui";
    public static final String PROPERTY_ALTERNATIVE_SOFTWARE = "alternative";
    public static final String PROPERTY_LICENSE = "license";
    public static final String PROPERTY_BLOG_DIGEST = "digest";
    public static final String PROPERTY_BANNED_BLOG = "banned_blog";
    public static final String PROPERTY_AUTHOR = "author";
    public static final String PROPERTY_SCORE = "score";
    public static final String PROPERTY_TICKET = "ticket";
    public static final String PROPERTY_USED_BY = "used_by";
	public static final String PROPERTY_MEMBER = "member";
    public static final String PROPERTY_FAVOURITED_BY = "favourited_by";

    // see EditBazaar
    public static final String BAZAAR_BUY = "buy";
    public static final String BAZAAR_SELL = "sell";
    public static final String BAZAAR_GIVE = "give";

    // see RichTextEditor
    public static final String INPUT_MODE_COMMENT = "comment";
    public static final String INPUT_MODE_GENERIC = "generic";
    public static final String INPUT_MODE_NEWS = "news";
    public static final String INPUT_MODE_BLOG = "blog";
    public static final String INPUT_MODE_WIKI = "wiki";

    // type of counter
    public static final String COUNTER_READ = "read";
    public static final String COUNTER_VISIT = "visit";
    public static final String COUNTER_PLAY = "play";
    
    public void configure(Preferences prefs) throws ConfigurationException {
        Field[] fields = Constants.class.getDeclaredFields();
        
        try {
            for (int i = 0; i < fields.length; i++) {
                /*boolean found = false;
                for (int j = 0; j < DYNAMIC_FIELDS.length; j++) {
                    if (fields[i].getName().startsWith(DYNAMIC_FIELDS[i])) {
                        found = true;
                        break;
                    }
                }

                if (!found)
                    continue;
                */
                
                if (!fields[i].getType().equals(int.class))
                    continue;
                
                int value = prefs.getInt(fields[i].getName(), -1);
                fields[i].setInt(null, value);
            }
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
}
