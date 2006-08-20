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
package cz.abclinuxu.scheduler;

import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.*;
import java.util.prefs.Preferences;

import org.dom4j.Node;
import org.dom4j.Element;
import org.dom4j.Document;

/**
 * This class is responsible for periodic fetching
 * of template and index variables from database.
 * It defines maximum for each type of object and
 * maintains list of most fresh objects.
 */
public class VariableFetcher extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VariableFetcher.class);

    static VariableFetcher instance;
    static {
        instance = new VariableFetcher();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public static final String KEY_HARDWARE = "hardware";
    public static final String KEY_DRIVER = "driver";
    public static final String KEY_STORY = "story";
    public static final String KEY_NEWS = "news";
    public static final String KEY_ARTICLE = "article";
    public static final String KEY_QUESTION = "question";
    public static final String KEY_FAQ = "faq";
    public static final String KEY_DICTIONARY = "dictionary";
    public static final String KEY_INDEX_LINKS = "links.in.index";
    public static final String KEY_TEMPLATE_LINKS = "links.in.template";

    public static final String PREF_DEFAULT = "default.";
    public static final String PREF_MAX = "max.";
    public static final String PREF_INDEX_FEEDS = "feeds.for.index";
    public static final String PREF_TEMPLATE_FEEDS = "feeds.for.template";

    List freshHardware, freshDrivers, freshStories, freshArticles, freshNews;
    List freshQuestions, freshFaqs, freshDictionary;
    String indexFeeds, templateFeeds;
    Map defaultSizes, maxSizes, counter, feedLinks;
    Relation currentPoll;

    long linksLastRun;
    SQLTool sqlTool;

    /**
     * Private constructor
     */
    private VariableFetcher() {
        sqlTool = SQLTool.getInstance();
        linksLastRun = System.currentTimeMillis();
    }

    /**
     * @return singleton of this object
     */
    public static VariableFetcher getInstance() {
        return instance;
    }

    /**
     * Map, where each service name is mapped to
     * count of objects of that service.
     */
    public Map getCounter() {
        return counter;
    }

    /**
     * Map where key is one of KEY_ constants and value is Integer
     * with default number of objects for that object.
     */
    public Map getDefaultSizes() {
        return defaultSizes;
    }

    /**
     * Map where key is one of KEY_ constants and value is Integer
     * with maximum number of objects for that object.
     */
    public Map getMaxSizes() {
        return maxSizes;
    }

    /**
     * List of the most fresh hardware relations according to user preference or system setting.
     */
    public List getFreshHardware(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_HARDWARE, null);
        List list = getSubList(freshHardware, userLimit);
        return list;
    }

    /**
     * List of the most fresh driver relations according to user preference or system setting.
     */
    public List getFreshDrivers(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_DRIVER, null);
        List list = getSubList(freshDrivers, userLimit);
        return list;
    }

    /**
     * List of the most fresh blog story relations according to user preference or system setting.
     */
    public List getFreshStories(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_STORY, "/data/settings/index_stories");
        List list = getSubList(freshStories, userLimit);
        return list;
    }

    /**
     * List of the most fresh article relations according to user preference or system setting.
     */
    public List getFreshArticles(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_ARTICLE, null);
        List list = getSubList(freshArticles, userLimit);
        return list;
    }

    /**
     * List of the most fresh discussion question relations according to user preference or system setting.
     */
    public List getFreshQuestions(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_QUESTION, "/data/settings/index_discussions");
        List list = getSubList(freshQuestions, userLimit);
        return list;
    }

    /**
     * List of the most fresh frequently asked question relations according to user preference or system setting.
     */
    public List getFreshFaqs(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_FAQ, null);
        List list = getSubList(freshFaqs, userLimit);
        return list;
    }

    /**
     * List of the most fresh dictionary relations according to user preference or system setting.
     */
    public List getFreshDictionary(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_DICTIONARY, null);
        List list = getSubList(freshDictionary, userLimit);
        return list;
    }

    /**
     * List of the most fresh news relations according to user preference or system setting.
     */
    public List getFreshNews(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_NEWS, "/data/settings/index_news");
        List list = getSubList(freshNews, userLimit);
        return list;
    }

    /**
     * Finds list of servers and their links to be displayed for this user. If user does not want
     * to see any links, empty map is returned.
     * @param maybeUser either instance of User or other value
     * @param index true if home page is being displayed
     * @return Map where key is initialized server and value is list of Links
     */
    public Map getFeeds(Object maybeUser, boolean index) {
        String defaultServers = null;
        int userLimit = 0;
        if (index) {
            userLimit = getObjectCountForUser(maybeUser, KEY_INDEX_LINKS, "/data/settings/index_links");
            defaultServers = indexFeeds;
        } else {
            userLimit = getObjectCountForUser(maybeUser, KEY_TEMPLATE_LINKS, "/data/settings/template_links");
            defaultServers = templateFeeds;
        }

        User user = null;
        if (maybeUser instanceof User)
            user = (User) maybeUser;
        if (user == null)
            return getSelectedFeeds(defaultServers, userLimit);

        Document document = user.getData();
        Element element = (Element) document.selectSingleNode("/data/settings/guidepost");
        if (element != null && "no".equals(element.getText()))
            return Collections.EMPTY_MAP;

        element = (Element) document.selectSingleNode("/data/settings/feeds");
        if (element == null || element.getText() == null)
            return getSelectedFeeds(defaultServers, userLimit);

        return getSelectedFeeds(element.getText(), userLimit);
    }

    /**
     * Current open poll relation.
     */
    public Relation getCurrentPoll() {
        if (currentPoll == null)
            return null;

        Relation relation = (Relation) PersistenceFactory.getPersistance().findById(currentPoll);
        Tools.sync(relation);
        return relation;
    }

    /**
     * Finds number of objects for given user. If o is not User or xpath is not set, then default value
     * will be returned. Otherwise user's preference will be returned (unless it is smaller than 0
     * or bigger than maximum for this object).
     * @param o instance of User, otherwise it will be ignored
     * @param key one of KEY_ constants
     * @param xpath xpath value for user, where is integer with his preference for this object
     * @return number of objects
     */
    private int getObjectCountForUser(Object o, String key, String xpath) {
        Integer defaultNumber = (Integer) defaultSizes.get(key);
        if (o == null || xpath==null || !(o instanceof User))
            return defaultNumber.intValue();

        User user = (User) o;
        Node node = ((User) user).getData().selectSingleNode(xpath);
        if (node == null)
            return defaultNumber.intValue();

        int count = Misc.parseInt(node.getText(), defaultNumber.intValue());
        if (count<0)
            return defaultNumber.intValue();

        Integer maximum = (Integer) maxSizes.get(key);
        count = Misc.limit(count, 0, maximum.intValue());
        return count;
    }

    /**
     * Returns sublist of specified list with given maximum size.
     * If subListSize is null, or list is null or empty, Collections.EMPTY_LIST
     * is returned. If list size is smaller than subListSize, the list
     * is returned. Otherwise new list with specified size is created
     * and filled with objetcs from the beginning of list.
     * @param list
     * @param subListSize
     * @return sublist of list with specified size.
     */
    private List getSubList(List list, int subListSize) {
        if (subListSize==0 || list==null || list.size()==0)
            return Collections.EMPTY_LIST;
        if (list.size()<subListSize)
            return list;
        List subList = new ArrayList(subListSize); // todo proc nepouzit list.sublist(0,subListSze)?
        for (int i=0; i<subListSize; i++)
            subList.add(list.get(i));
        return subList;
    }

    /**
     * performs lookup of fresh values.
     */
    public void run() {
        log.debug("Zacina stahovani cachovanych promennych");
        try {
            refreshArticles();
            refreshCurrentPoll();
            refreshDictionary();
            refreshDrivers();
            refreshFaq();
            refreshHardware();
            refreshNews();
            refreshQuestions();
            refreshSizes();
            refreshStories();
            refreshFeedLinks();
            log.debug("Cahovani hotovo.");
        } catch (Throwable e) {
            log.error("Selhalo cachovani!", e);
        }
    }

    private void refreshFeedLinks() {
        try {
            Map feeds = UpdateLinks.getMaintainedFeeds();
            List links;
            for (Iterator iter = feeds.values().iterator(); iter.hasNext();) {
                links = (List) iter.next();
                Sorters2.byDate(links, Sorters2.DESCENDING);
            }
            feedLinks = feeds;
        } catch (Exception e) {
            log.error("Selhalo nacitani odkazu feedu", e);
        }
    }

    public void refreshNews() {
        try {
            int maximum = ((Integer)maxSizes.get(KEY_NEWS)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List news = SQLTool.getInstance().findNewsRelations(qualifiers);
            Tools.syncList(news);
            freshNews = news;
        } catch (Exception e) {
            log.error("Selhalo nacitani zpravicek", e);
        }
    }

    public void refreshArticles() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_ARTICLE)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List articles = sqlTool.findIndexArticlesRelations(qualifiers);
            Tools.syncList(articles);
            freshArticles = articles;
        } catch (Exception e) {
            log.error("Selhalo nacitani clanku", e);
        }
    }

    public void refreshQuestions() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_QUESTION)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List found = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.syncList(found);
            freshQuestions = found;
        } catch (Exception e) {
            log.error("Selhalo nacitani dotazu ve foru", e);
        }
    }

    public void refreshStories() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_STORY)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.BLOG, qualifiers);
            List blogs = new ArrayList(list.size());
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                blogs.add(relation.getParent());
            }
            Tools.syncList(blogs); // parent on relation must be synchronized
            Tools.syncList(list);
            freshStories = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani blogu", e);
        }
    }

    public void refreshHardware() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_HARDWARE)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(list);
            freshHardware = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani hardwaru", e);
        }
    }

    public void refreshDrivers() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_DRIVER)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.DRIVER, qualifiers);
            Tools.syncList(list);
            freshDrivers = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani ovladacu", e);
        }
    }

    public void refreshDictionary() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_QUESTION)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List data = sqlTool.findRecordParentRelationsWithType(Record.DICTIONARY, qualifiers);
            Tools.syncList(data);
            freshDictionary = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani pojmu", e);
        }
    }

    public void refreshFaq() {
        try {
            int maximum = ((Integer) maxSizes.get(KEY_FAQ)).intValue();
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.FAQ, qualifiers);
            Tools.syncList(list);
            freshFaqs = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani casto kladenych otazek", e);
        }
    }

    public void refreshCurrentPoll() {
        try {
            currentPoll = sqlTool.findActivePoll();
        } catch (Exception e) {
            log.error("Selhalo nacitani aktualni ankety", e);
        }
    }

    public void refreshSizes() {
        try {
            Persistence persistence = PersistenceFactory.getPersistance();
            Category requests = (Category) persistence.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS", new Integer(requests.getChildren().size()));
            Category news = (Category) persistence.findById(new Category(Constants.CAT_NEWS_POOL));
            counter.put("WAITING_NEWS", new Integer(news.getChildren().size()));
        } catch (Exception e) {
            log.error("Selhalo nacitani velikosti", e);
        }
    }

    private Map getSelectedFeeds(String servers, int size) {
        Persistence persistence = PersistenceFactory.getPersistance();
        StringTokenizer stk = new StringTokenizer(servers, ",");
        String tmp;
        int id;
        Server server;
        Map result = new LinkedHashMap(25, 0.99f);
        List links;
        while (stk.hasMoreTokens()) {
            tmp = stk.nextToken();
            id = Misc.parseInt(tmp, -1);
            if (id == -1) {
                log.warn("Damaged list of servers: '"+servers+"'!");
                continue;
            }

            server = (Server) persistence.findById(new Server(id));
            links = (List) feedLinks.get(server);
            if (links == null) // unmaintained
                continue;

            links = getSubList(links, size);
            result.put(server, links);
        }
        return result;
    }

    /**
     * Reads maximum sizes and initializes all lists.
     *
     * @throws ConfigurationException
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        counter = new HashMap(10, 1.0f);
        defaultSizes = new HashMap(10, 1.0f);
        maxSizes = new HashMap(10, 1.0f);

        int size = prefs.getInt(PREF_DEFAULT + KEY_ARTICLE, 9);
        defaultSizes.put(KEY_ARTICLE, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_ARTICLE, 20);
        maxSizes.put(KEY_ARTICLE, new Integer(size));
        freshArticles = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_DICTIONARY, 3);
        defaultSizes.put(KEY_DICTIONARY, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_DICTIONARY, 10);
        maxSizes.put(KEY_DICTIONARY, new Integer(size));
        freshDictionary = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_DRIVER, 3);
        defaultSizes.put(KEY_DRIVER, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_DRIVER, 10);
        maxSizes.put(KEY_DRIVER, new Integer(size));
        freshDrivers = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_FAQ, 3);
        defaultSizes.put(KEY_FAQ, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_FAQ, 3);
        maxSizes.put(KEY_FAQ, new Integer(size));
        freshFaqs = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_HARDWARE, 3);
        defaultSizes.put(KEY_HARDWARE, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_HARDWARE, 3);
        maxSizes.put(KEY_HARDWARE, new Integer(size));
        freshHardware = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_NEWS, 5);
        defaultSizes.put(KEY_NEWS, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_NEWS, 5);
        maxSizes.put(KEY_NEWS, new Integer(size));
        freshNews = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_QUESTION, 20);
        defaultSizes.put(KEY_QUESTION, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_QUESTION, 20);
        maxSizes.put(KEY_QUESTION, new Integer(size));
        freshQuestions = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_STORY, 5);
        defaultSizes.put(KEY_STORY, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_STORY, 5);
        maxSizes.put(KEY_STORY, new Integer(size));
        freshStories = Collections.EMPTY_LIST;

        indexFeeds = prefs.get(PREF_INDEX_FEEDS, "");
        templateFeeds = prefs.get(PREF_TEMPLATE_FEEDS, "");
        size = prefs.getInt(PREF_DEFAULT + KEY_INDEX_LINKS, 3);
        defaultSizes.put(KEY_INDEX_LINKS, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_INDEX_LINKS, 5);
        maxSizes.put(KEY_INDEX_LINKS, new Integer(size));
        size = prefs.getInt(PREF_DEFAULT + KEY_TEMPLATE_LINKS, 3);
        defaultSizes.put(KEY_TEMPLATE_LINKS, new Integer(size));
        size = prefs.getInt(PREF_MAX + KEY_TEMPLATE_LINKS, 5);
        maxSizes.put(KEY_TEMPLATE_LINKS, new Integer(size));
    }
}
