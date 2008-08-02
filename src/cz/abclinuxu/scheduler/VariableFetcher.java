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
import cz.abclinuxu.data.view.ChangedContent;
import cz.abclinuxu.data.view.CloudTag;
import cz.abclinuxu.data.view.SectionTreeCache;
import cz.abclinuxu.data.view.HostingServer;
import cz.abclinuxu.data.view.JobsCzHolder;
import cz.abclinuxu.data.view.JobsCzItem;
import cz.abclinuxu.data.view.Screenshot;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.servlets.html.view.ContentChanges;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.TagTool.ListOrder;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.*;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.commons.collections.map.LinkedMap;
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
    public static final String KEY_SOFTWARE = "software";
    public static final String KEY_DRIVER = "driver";
    public static final String KEY_STORY = "story";
    public static final String KEY_NEWS = "news";
    public static final String KEY_ARTICLE = "article";
    public static final String KEY_QUESTION = "question";
    public static final String KEY_FAQ = "faq";
    public static final String KEY_DICTIONARY = "dictionary";
    public static final String KEY_PERSONALITIES = "personalities";
    public static final String KEY_BAZAAR = "bazaar";
    public static final String KEY_SCREENSHOT = "screenshot";
    public static final String KEY_TRIVIA = "trivia";
    public static final String KEY_JOBSCZ = "jobscz";
    public static final String KEY_INDEX_LINKS = "links.in.index";
    public static final String KEY_TEMPLATE_LINKS = "links.in.template";
    public static final String KEY_TAGCLOUD = "tagcloud";

    public static final String PREF_DEFAULT = "default.";
    public static final String PREF_MAX = "max.";
    public static final String PREF_INDEX_FEEDS = "feeds.for.index";
    public static final String PREF_TEMPLATE_FEEDS = "feeds.for.template";
    public static final String PREF_SECTION_CACHE_FREQUENCY = "section.cache.rebuild.frequency";
    public static final String PREF_MAIN_FORUMS = "forums";

    List freshHardware, freshSoftware, freshDrivers, freshStories, freshArticles, freshNews;
    List freshQuestions, freshFaqs, freshDictionary, freshBazaarAds, freshPersonalities;
    List freshTrivias;
    Map<Integer, List> freshSubportalArticles, freshForumQuestions, freshSubportalWikiPages;
    Map<Integer, Relation> freshSubportalEvents;
    
    Map<Integer, Integer> mainForums;
    List<CloudTag> freshCloudTags;

    List<Screenshot> freshScreenshots;
    String indexFeeds, templateFeeds;
    Map<String, Integer> defaultSizes, maxSizes, counter;
    Map<Relation, Map<String, Integer>> subportalCounter;
    Map<Server, List<Link>> feedLinks;
    SectionTreeCache faqTree, softwareTree, hardwareTree, articleTree;
    Relation currentPoll;
    int sectionCacheFrequency;
    HostingServer hostingServer, offer64bit;
    JobsCzHolder jobsCzHolderHP, jobsCzHolderPage;

    SQLTool sqlTool;
    int cycle;

    /**
     * Private constructor
     */
    private VariableFetcher() {
        sqlTool = SQLTool.getInstance();
        //forumTree = new SectionTreeCache(UrlUtils.PREFIX_FORUM, Constants.CAT_FORUM);
        //forumTree.setCacheSize(90);
        //forumTree.setLoadLastItem(true);
        faqTree = new SectionTreeCache(UrlUtils.PREFIX_FAQ, Constants.CAT_FAQ);
        faqTree.setCacheSize(30);
        faqTree.setLoadLastItem(true);
        softwareTree = new SectionTreeCache(UrlUtils.PREFIX_SOFTWARE, Constants.CAT_SOFTWARE);
        softwareTree.setLoadDescriptions(false);
        softwareTree.setCacheSize(130);
        hardwareTree = new SectionTreeCache(UrlUtils.PREFIX_HARDWARE, Constants.CAT_HARDWARE);
        hardwareTree.setLoadDescriptions(false);
        hardwareTree.setCacheSize(400);
        articleTree = new SectionTreeCache(UrlUtils.PREFIX_CLANKY, Constants.CAT_ARTICLES);
        articleTree.setLoadDescriptions(false);
        articleTree.setCacheSize(20);
//        articleTree.setLoadLastItem(true); todo - konfigurace, zda posledni je zmeneno nebo vytvoreno
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
     * Returns a counter for the specified subportal
     */
    public Map getSubportalCounter(Relation portal) {
        Map map = subportalCounter.get(portal);
        if (map != null)
            return map;
        else
            return Collections.EMPTY_MAP;
    }
    
    public Map<Integer, Integer> getMainForums() {
        return mainForums;
    }

    /**
     * Map where key is one of KEY_ constants and value is Integer
     * with default number of objects for that object.
     */
    public Map<String, Integer> getDefaultSizes() {
        return defaultSizes;
    }

    /**
     * Map where key is one of KEY_ constants and value is Integer
     * with maximum number of objects for that object.
     */
    public Map<String, Integer> getMaxSizes() {
        return maxSizes;
    }

    /**
     * List of the most fresh hardware relations according to user preference or system setting.
     */
    public List getFreshHardware(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_HARDWARE, null);
        return getSubList(freshHardware, userLimit);
    }

    /**
     * List of the most fresh software relations according to user preference or system setting.
     */
    public List getFreshSoftware(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_SOFTWARE, null);
        return getSubList(freshSoftware, userLimit);
    }

    /**
     * List of the most fresh driver relations according to user preference or system setting.
     */
    public List getFreshDrivers(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_DRIVER, null);
        return getSubList(freshDrivers, userLimit);
    }

    /**
     * List of the most fresh blog story relations according to user preference or system setting.
     */
    public List getFreshStories(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_STORY, "/data/settings/index_stories");
        List unfiltered = getSubList(freshStories, Tools.getPreloadedStoryCount(userLimit) );
        return Tools.filterBannedStories(unfiltered, user, userLimit, true);
    }

    /**
     * List of the most fresh article relations according to user preference or system setting.
     */
    public List getFreshArticles(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_ARTICLE, null);
        return getSubList(freshArticles, userLimit);
    }

    /**
     * List of the most fresh article relations from a subportal
     * according to user preference or system setting.
     */
    public List getFreshSubportalArticles(Object user, int rel) {
        int userLimit = getObjectCountForUser(user, KEY_ARTICLE, null);
        List articles = freshSubportalArticles.get(rel);

        if (articles == null)
            return Collections.EMPTY_LIST;
        else
            return getSubList(articles, userLimit);
    }

    /**
     * List of the most fresh content relations from a subportal
     * according to user preference or system setting.
     */
    public List getFreshSubportalWikiPages(Object user, int rel) {
        int userLimit = getObjectCountForUser(user, KEY_SOFTWARE, null);
        List articles = freshSubportalWikiPages.get(rel);

        if (articles == null)
            return Collections.EMPTY_LIST;
        else
            return getSubList(articles, userLimit);
    }
    
    /**
     * Returns the nearest upcoming event for the subportal specified.
     * @param rel A subportal
     * @return An event, if any
     */
    public Relation getFreshSubportalEvent(int rel) {
        if (freshSubportalEvents != null)
            return freshSubportalEvents.get(rel);
        else
            return null;
    }

    /**
     * List of the most fresh discussion question relations from a subportal
     * according to user preference or system setting.
     */
    public List getFreshQuestions(int count, int rid) {
        if (freshQuestions == null)
            return Collections.EMPTY_LIST;
        
        List questions = freshForumQuestions.get(rid);
        
        if (questions == null)
            return Collections.EMPTY_LIST;
        else
            return getSubList(questions, count);
    }
    
    public List getFreshQuestions() {
        int limit = maxSizes.get(KEY_QUESTION);
        return getSubList(freshQuestions, limit);
    }

    
    /**
     * List of the most fresh frequently asked question relations according to user preference or system setting.
     */
    public List getFreshFaqs(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_FAQ, null);
        return getSubList(freshFaqs, userLimit);
    }

    /**
     * List of the most fresh dictionary relations according to user preference or system setting.
     */
    public List getFreshDictionary(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_DICTIONARY, null);
        return getSubList(freshDictionary, userLimit);
    }

    /**
     * List of the most fresh personality relations according to user preference or system setting.
     */
    public List getFreshPersonalities(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_PERSONALITIES, null);
        return getSubList(freshPersonalities, userLimit);
    }

    /**
     * List of the most fresh dictionary relations according to user preference or system setting.
     */
    public List getFreshBazaarAds(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_BAZAAR, null);
        return getSubList(freshBazaarAds, userLimit);
    }

    /**
     * List of the most fresh news relations according to user preference or system setting.
     */
    public List getFreshNews(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_NEWS, "/data/settings/index_news");
        return getSubList(freshNews, userLimit);
    }

    /**
     * List of the most fresh news relations according to user preference or system setting.
     */
    public List getFreshTrivia(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_TRIVIA, null);
        return getSubList(freshTrivias, userLimit);
    }

    /**
     * List of the most fresh screenshot relations according to user preference or system setting.
     */
    public List getFreshScreenshots(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_SCREENSHOT, "/data/settings/index_screenshots");
        return getSubList(freshScreenshots, userLimit);
    }

    public List<JobsCzItem> getFreshJobsCz(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_JOBSCZ, null);
        return getSubList(jobsCzHolderHP.getJobsList(), userLimit);
    }

    public List<CloudTag> getFreshCloudTags(Object user) {
    	int userLimit = getObjectCountForUser(user, KEY_TAGCLOUD, null);
        return getSubList(freshCloudTags, userLimit);
    }

    /**
     * Finds list of servers and their links to be displayed for this user. If user does not want
     * to see any links, empty map is returned.
     * @param maybeUser either instance of User or other value
     * @param index true if home page is being displayed
     * @return Map where key is initialized server and value is list of Links
     */
    public Map getFeeds(Object maybeUser, boolean index) {
        String defaultServers;
        int userLimit;
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
     * Gets newest links for specified server.
     * @param id id of server
     * @param linksCount maximum number of links
     * @return list of links or empty list, if the server is not found
     */
    public List<Link> getFeed(int id, int linksCount) {
        Server server = new Server(id);
        List<Link> links = feedLinks.get(server);
        if (links == null) // unmaintained
            return Collections.emptyList();

        return getSubList(links, linksCount);
    }

    /**
     * Current open poll relation.
     */
    public Relation getCurrentPoll() {
        if (currentPoll == null)
            return null;

        Relation relation = (Relation) PersistenceFactory.getPersistence().findById(currentPoll);
        Tools.sync(relation);
        return relation;
    }

    /**
     * @return cache of forum sections
     */
    //public SectionTreeCache getForumTree() {
    //    return forumTree;
    //}

    /**
     * @return cache of FAQ sections
     */
    public SectionTreeCache getFaqTree() {
        return faqTree;
    }

    /**
     * @return cache of Software sections
     */
    public SectionTreeCache getSoftwareTree() {
        return softwareTree;
    }

    /**
     * @return cache of Hardware sections
     */
    public SectionTreeCache getHardwareTree() {
        return hardwareTree;
    }

    /**
     * @return cache of Article sections
     */
    public SectionTreeCache getArticleTree() {
        return articleTree;
    }

    /**
     * @return server from abchost offering, it may be null!
     */
    public HostingServer getHostingServer() {
        return hostingServer;
    }
    
    public HostingServer getOffer64bit() {
        return offer64bit;
    }

    /**
     * @return Jobs.cz holder
     */
     public JobsCzHolder getJobsCzHolder() {
         return jobsCzHolderPage;
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
            return defaultNumber;

        User user = (User) o;
        Node node = user.getData().selectSingleNode(xpath);
        if (node == null)
            return defaultNumber;

        int count = Misc.parseInt(node.getText(), defaultNumber);
        if (count<0)
            return defaultNumber;

        Integer maximum = (Integer) maxSizes.get(key);
        count = Misc.limit(count, 0, maximum);
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
    private <T> List<T> getSubList(List<T> list, int subListSize) {
        if (subListSize == 0 || list == null || list.isEmpty())
            return Collections.emptyList();
        if (list.size() < subListSize)
            return list;
        return list.subList(0, subListSize);
    }

    /**
     * performs lookup of fresh values.
     */
    public void run() {
        log.debug("Zacina stahovani cachovanych promennych");
        try {
            refreshArticles();
            refreshSubportalArticles(null);
            refreshSubportalWikiPages(null);
            refreshSubportalEvents(null);
            refreshBazaar();
            refreshCurrentPoll();
            refreshDictionary();
            refreshPersonalities();
            refreshDrivers();
            refreshFaq();
            refreshHardware();
            refreshSoftware();
            refreshNews();
            refreshForumQuestions();
            refreshQuestions();
            refreshSizes();
            refreshSubportalSizes(null);
            refreshStories();
            refreshFeedLinks();
            refreshSectionCaches();
            refreshScreenshots();
            refreshCloudTags();
            refreshTrivia();

            // jobs are refreshed from another thread (JobsCzFetcher)
            // refreshJobsCz();

            cycle++;
            log.debug("Cachovani hotovo.");
        } catch (Throwable e) {
            log.error("Selhalo cachovani!", e);
        }
    }

    /**
     * Sets hosting server.
     * @param server server
     */
    public void setHostingServer(HostingServer server) {
        hostingServer = server;
    }

    /**
     * Sets a product from 64bit.cz
     * @param server
     */
    public void setOffer64bit(HostingServer server) {
        offer64bit = server;
    }

    private void refreshSectionCaches() {
        try {
            long start = System.currentTimeMillis();
            if (cycle % sectionCacheFrequency == 0) {
                faqTree.initialize();
                //forumTree.initialize();
                softwareTree.initialize();
                hardwareTree.initialize();
                articleTree.initialize();
            }

            faqTree.refresh();
            //forumTree.refresh();
            softwareTree.refresh();
            hardwareTree.refresh();
            articleTree.refresh();

            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("Refreshing section caches took "+(end-start)+" ms.");
            }
        } catch (Exception e) {
            log.error("Selhalo nacitani section tree cache", e);
        }
    }

    private void refreshFeedLinks() {
        try {
            Map<Server, List<Link>> feeds = UpdateLinks.getMaintainedFeeds();
            for (List links : feeds.values()) {
                Sorters2.byDate(links, Sorters2.DESCENDING);
            }
            feedLinks = feeds;
        } catch (Exception e) {
            log.error("Selhalo nacitani odkazu feedu", e);
        }
    }

    public void refreshNews() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_NEWS);
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
            int maximum = (Integer) maxSizes.get(KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List articles = sqlTool.findIndexArticlesRelations(qualifiers);
            Tools.syncList(articles);
            freshArticles = articles;
        } catch (Exception e) {
            log.error("Selhalo nacitani clanku", e);
        }
    }
    
    public void refreshSubportalEvents(Relation where) {
        try {
            Map<Integer, Relation> map;
            List<Relation> children;
            String date = Constants.isoFormat.format(new Date());
            
            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap();
                children = Tools.syncList(subportals.getChildren());
            } else {
                map =  freshSubportalEvents;
                children = Collections.singletonList((Relation) Tools.sync(where));
            }
            
            // get an event for every subportal
            for (Relation rel : children) {
                int rid = Misc.parseInt(Tools.xpath(rel.getChild(), "/data/events"), 0);
                
                Qualifier[] qualifiers = new Qualifier[] {
                    new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, date),
                    new CompareCondition(Field.UPPER, Operation.EQUAL, rid),
                    Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING,
                    new LimitQualifier(0, 1)
                };
                
                List<Relation> list = sqlTool.findItemRelationsWithType(Item.EVENT, qualifiers);
                
                if (!Misc.empty(list)) {
                    Relation event = list.get(0);
                    Tools.sync(event);
                    map.put(rid, event);
                }
            }
            
            freshSubportalEvents = map;
        } catch (Exception e) {
            log.error("Selhalo nacitani akci pro subportaly", e);
        }
    }
   
    public void refreshSubportalWikiPages(Relation where) {
        try {
            Map<Integer, List> map;
            List<Relation> children;
            Persistence persistence = PersistenceFactory.getPersistence();
            
            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap();
                children = Tools.syncList(subportals.getChildren());
            } else {
                map = freshSubportalWikiPages;
                children = Collections.singletonList((Relation) Tools.sync(where));
            }
            
            // get wikis for every subportal
            for (Relation rel : children) {
                int rid = Misc.parseInt(Tools.xpath(rel.getChild(), "/data/wiki"), 0);
                List<ChangedContent> changes;
                List<Relation> result;
                Relation wikiRelation = new Relation(rid);
                
                Tools.sync(wikiRelation);
                
                changes = ContentChanges.changedContentList(wikiRelation, persistence, ContentChanges.COLUMN_DATE, true);
                result = new ArrayList(changes.size());
                
                for (ChangedContent change : changes)
                    result.add(change.getRelation());
                
                map.put(rid, result);
            }
            
            freshSubportalWikiPages = map;
        } catch (Exception e) {
            log.error("Selhalo nacitani wiki pro subportaly", e);
        }
    }
    
    public void refreshSubportalArticles(Relation where) {
        try {
            List<Relation> children;
            Map<Integer, List> map;
            int maximum = (Integer) maxSizes.get(KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            
            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap();
                children = Tools.syncList(subportals.getChildren());
            } else {
                map = freshSubportalArticles;
                children = Collections.singletonList((Relation) Tools.sync(where));
            }
            
            // get articles for every subportal
            for (Relation rel : children) {
                // get the rid of the article section of that subportal
                int rid = Misc.parseInt(Tools.xpath(rel.getChild(), "/data/articles"), 0);
                Relation r = new Relation(rid);
                
                Tools.sync(r);
                
                // get the articles
                List<Relation> articles = sqlTool.findArticleRelations(qualifiers, r.getChild().getId());
                Tools.syncList(articles);
                
                map.put(rid, articles);
            }
            
            freshSubportalArticles = map;
        } catch (Exception e) {
            log.error("Selhalo nacitani clanku pro subportaly", e);
        }
    }
    
    public void refreshForumQuestions() {
        try {
            Map<Integer, List> map;
            Category subportals = new Category(Constants.CAT_SUBPORTALS);
            int maximum = (Integer) maxSizes.get(KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            
            // get a list of subportals
            List<Relation> children = Tools.syncList(subportals.getChildren());
            
            map = new HashMap(children.size() + mainForums.size());
            
            // get forums for every subportal
            for (Relation rel : children) {
                // get the rid of the forum of that subportal
                int rid = Misc.parseInt(Tools.xpath(rel.getChild(), "/data/forum"), 0);
                
                // get the articles
                List<Relation> dizs = sqlTool.findDiscussionRelationsWithParent(rid, qualifiers);
                Tools.syncList(dizs);
                
                map.put(rid, dizs);
            }
            
            for (Integer rel : mainForums.keySet()) {
                List<Relation> dizs = sqlTool.findDiscussionRelationsWithParent(rel, qualifiers);
                Tools.syncList(dizs);
                
                map.put(rel, dizs);
            }
            
            freshForumQuestions = map;
        } catch (Exception e) {
            log.error("Selhalo nacitani diskuzi pro sekce", e);
        }
    }
    
    public void refreshQuestions() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_QUESTION);
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
            int maximum = (Integer) maxSizes.get(KEY_STORY);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.BLOG, qualifiers);
            List blogs = new ArrayList(list.size());
            for (Object aList : list) {
                Relation relation = (Relation) aList;
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
            int maximum = (Integer) maxSizes.get(KEY_HARDWARE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(list);
            freshHardware = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani hardwaru", e);
        }
    }

    public void refreshSoftware() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_SOFTWARE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            Tools.syncList(list);
            freshSoftware = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani softwaru", e);
        }
    }

    public void refreshDrivers() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_DRIVER);
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
            int maximum = (Integer) maxSizes.get(KEY_DICTIONARY);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List data = sqlTool.findItemRelationsWithType(Item.DICTIONARY, qualifiers);
            Tools.syncList(data);
            freshDictionary = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani pojmu", e);
        }
    }

    public void refreshTrivia() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_TRIVIA);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List data = sqlTool.findItemRelationsWithType(Item.TRIVIA, qualifiers);
            Tools.syncList(data);
            freshTrivias = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani kvizu", e);
        }
    }

    public void refreshPersonalities() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_PERSONALITIES);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List data = sqlTool.findItemRelationsWithType(Item.PERSONALITY, qualifiers);
            Tools.syncList(data);
            freshPersonalities = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani osobnosti", e);
        }
    }

    public void refreshFaq() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_FAQ);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.FAQ, qualifiers);
            Tools.syncList(list);
            freshFaqs = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani casto kladenych otazek", e);
        }
    }

    public void refreshBazaar() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_BAZAAR);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List list = sqlTool.findItemRelationsWithType(Item.BAZAAR, qualifiers);
            Tools.syncList(list);
            freshBazaarAds = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani inzeratu z bazaru", e);
        }
    }

    public void refreshScreenshots() {
        try {
            int maximum = (Integer) maxSizes.get(KEY_SCREENSHOT);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.SCREENSHOT, qualifiers);
            Tools.syncList(list);

            List<Screenshot> result = new ArrayList<Screenshot>(list.size());
            for (Relation relation : list) {
                result.add(new Screenshot(relation));
            }
            freshScreenshots = result;
        } catch (Exception e) {
            log.error("Selhalo nacitani desktopu", e);
        }
    }

    /**
     * Fetches jobs from available XML file, if
     * fetching fails, do not change current holder
     * @param uriPage URI of XML to be parsed
     */
    public void refreshJobsCz(String uriPage, String uriHP) {
        try {
            JobsCzHolder newHolder = new JobsCzHolder();
            newHolder.fetch(uriPage);
            jobsCzHolderPage = newHolder;

            newHolder = new JobsCzHolder();
            newHolder.fetch(uriHP);
            jobsCzHolderHP = newHolder;
        } catch (Exception e) {
            log.error("Selhalo nacitani pracovnich pozic serveru jobs.cz", e);
        }
    }

    /**
     * Constructs list of most used tags
     */
    public void refreshCloudTags() {
    	try {
    		int count = (Integer) defaultSizes.get(KEY_TAGCLOUD);
    		List<Tag> tags = TagTool.list(0, count, ListOrder.BY_USAGE, false);

    		// get max & min
            int minOccurs = tags.get(tags.size() - 1).getUsage();
    		int maxOccurs = tags.get(0).getUsage();

    		List<CloudTag> result = new ArrayList<CloudTag>(tags.size());
            for (Tag tag : tags) {
    			CloudTag ct = new CloudTag(tag, minOccurs, maxOccurs);
    			result.add(ct);
    		}
    		// sort by name
    		Collections.sort(result);
    		freshCloudTags = result;
    	} catch(Exception e) {
    		log.error("Selhalo nacitani tag cloud");
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
            Persistence persistence = PersistenceFactory.getPersistence();
            Category requests = (Category) persistence.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS", requests.getChildren().size());

            int sleeping = 0, waiting = 0;
            Category news = (Category) persistence.findById(new Category(Constants.CAT_NEWS_POOL));
            for (Relation relation : news.getChildren()) {
                Item item = (Item) persistence.findById(relation.getChild());
                if (item.getData().selectSingleNode("/data/approved_by") == null)
                    waiting++;
                else
                    sleeping++;
            }
            counter.put("WAITING_NEWS", waiting);
            counter.put("SLEEPING_NEWS", sleeping);
            
            int numEvents = sqlTool.countItemRelationsWithType(Item.UNPUBLISHED_EVENT,
                        new Qualifier[] { new CompareCondition(Field.UPPER, Operation.EQUAL, Constants.REL_EVENTS) });
            
            counter.put("WAITING_EVENTS", numEvents);
        } catch (Exception e) {
            log.error("Selhalo nacitani velikosti", e);
        }
    }
    
    public void refreshSubportalSizes(Relation where) {
        try {
            Map<Relation, Map<String,Integer>> map;
            List<Relation> children;
            Persistence persistence = PersistenceFactory.getPersistence();

            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap();
                children = Tools.syncList(subportals.getChildren());
            } else {
                map = subportalCounter;
                children = Collections.singletonList((Relation) Tools.sync(where));
            }

            for (Relation rel : children) {
                Map<String,Integer> portal = new HashMap(6);

                Relation articles = Tools.createRelation(Tools.xpath(rel.getChild(), "//articles"));
                Relation wiki = Tools.createRelation(Tools.xpath(rel.getChild(), "//wiki"));
                Relation forum = Tools.createRelation(Tools.xpath(rel.getChild(), "//forum"));
                Relation events = Tools.createRelation(Tools.xpath(rel.getChild(), "//events"));
                Relation pool = Tools.createRelation(Tools.xpath(rel.getChild(), "//article_pool"));

                portal.put("ARTICLES", persistence.findChildren(articles.getChild()).size());
                portal.put("WAITING_ARTICLES", persistence.findChildren(pool.getChild()).size());
                portal.put("WIKIS", persistence.findChildren(wiki.getChild()).size());
                portal.put("QUESTIONS", persistence.findChildren(forum.getChild()).size());

                int numEvents = sqlTool.countItemRelationsWithType(Item.EVENT,
                        new Qualifier[] { new CompareCondition(Field.UPPER, Operation.EQUAL, events.getId()) });
                
                portal.put("EVENTS", numEvents);
                portal.put("WAITING_EVENTS", persistence.findChildren(events.getChild()).size() - numEvents);

                map.put(rel, portal);
            }

            subportalCounter = map;
        } catch (Exception e) {
            log.error("Selhalo nacitani velikosti podportalu", e);
        }
    }

    private Map<Server, List<Link>> getSelectedFeeds(String servers, int size) {
        Persistence persistence = PersistenceFactory.getPersistence();
        StringTokenizer stk = new StringTokenizer(servers, ",");
        String tmp;
        int id;
        Server server;
        Map<Server, List<Link>> result = new LinkedHashMap<Server, List<Link>>(25, 0.99f);
        List<Link> links;
        while (stk.hasMoreTokens()) {
            tmp = stk.nextToken();
            id = Misc.parseInt(tmp, -1);
            if (id == -1) {
                log.warn("Damaged list of servers: '"+servers+"'!");
                continue;
            }

            server = (Server) persistence.findById(new Server(id));
            links = feedLinks.get(server);
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
        counter = new HashMap<String, Integer>(10, 1.0f);
        defaultSizes = new HashMap<String, Integer>(17, 1.0f);
        maxSizes = new HashMap<String, Integer>(15, 1.0f);

        int size = prefs.getInt(PREF_DEFAULT + KEY_ARTICLE, 9);
        defaultSizes.put(KEY_ARTICLE, size);
        size = prefs.getInt(PREF_MAX + KEY_ARTICLE, 20);
        maxSizes.put(KEY_ARTICLE, size);
        freshArticles = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_DICTIONARY, 3);
        defaultSizes.put(KEY_DICTIONARY, size);
        size = prefs.getInt(PREF_MAX + KEY_DICTIONARY, 10);
        maxSizes.put(KEY_DICTIONARY, size);
        freshDictionary = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_PERSONALITIES, 3);
        defaultSizes.put(KEY_PERSONALITIES, size);
        size = prefs.getInt(PREF_MAX + KEY_PERSONALITIES, 10);
        maxSizes.put(KEY_PERSONALITIES, size);
        freshDictionary = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_DRIVER, 3);
        defaultSizes.put(KEY_DRIVER, size);
        size = prefs.getInt(PREF_MAX + KEY_DRIVER, 10);
        maxSizes.put(KEY_DRIVER, size);
        freshDrivers = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_FAQ, 3);
        defaultSizes.put(KEY_FAQ, size);
        size = prefs.getInt(PREF_MAX + KEY_FAQ, 3);
        maxSizes.put(KEY_FAQ, size);
        freshFaqs = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_HARDWARE, 3);
        defaultSizes.put(KEY_HARDWARE, size);
        size = prefs.getInt(PREF_MAX + KEY_HARDWARE, 3);
        maxSizes.put(KEY_HARDWARE, size);
        freshHardware = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_SOFTWARE, 3);
        defaultSizes.put(KEY_SOFTWARE, size);
        size = prefs.getInt(PREF_MAX + KEY_SOFTWARE, 3);
        maxSizes.put(KEY_SOFTWARE, size);
        freshSoftware = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_NEWS, 5);
        defaultSizes.put(KEY_NEWS, size);
        size = prefs.getInt(PREF_MAX + KEY_NEWS, 5);
        maxSizes.put(KEY_NEWS, size);
        freshNews = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_QUESTION, 20);
        defaultSizes.put(KEY_QUESTION, size);
        size = prefs.getInt(PREF_MAX + KEY_QUESTION, 20);
        maxSizes.put(KEY_QUESTION, size);

        size = prefs.getInt(PREF_DEFAULT + KEY_STORY, 5);
        defaultSizes.put(KEY_STORY, size);
        size = prefs.getInt(PREF_MAX + KEY_STORY, 5);
        maxSizes.put(KEY_STORY, Tools.getPreloadedStoryCount(size));
        freshStories = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_BAZAAR, 5);
        defaultSizes.put(KEY_BAZAAR, size);
        size = prefs.getInt(PREF_MAX + KEY_BAZAAR, 5);
        maxSizes.put(KEY_BAZAAR, size);
        freshBazaarAds = Collections.EMPTY_LIST;

        size = prefs.getInt(PREF_DEFAULT + KEY_SCREENSHOT, 3);
        defaultSizes.put(KEY_SCREENSHOT, size);
        size = prefs.getInt(PREF_MAX + KEY_SCREENSHOT, 3);
        maxSizes.put(KEY_SCREENSHOT, size);
        freshScreenshots = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_JOBSCZ, 10);
        defaultSizes.put(KEY_JOBSCZ, size);
        jobsCzHolderHP = jobsCzHolderPage = JobsCzHolder.EMPTY_HOLDER;

        size = prefs.getInt(PREF_DEFAULT + KEY_TAGCLOUD, 20);
        defaultSizes.put(KEY_TAGCLOUD, size);
        freshCloudTags = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_TRIVIA, 3);
        defaultSizes.put(KEY_TRIVIA, size);
        size = prefs.getInt(PREF_MAX + KEY_TRIVIA, 10);
        maxSizes.put(KEY_TRIVIA, size);
        freshTrivias = Collections.EMPTY_LIST;

        indexFeeds = prefs.get(PREF_INDEX_FEEDS, "");
        templateFeeds = prefs.get(PREF_TEMPLATE_FEEDS, "");
        size = prefs.getInt(PREF_DEFAULT + KEY_INDEX_LINKS, 3);
        defaultSizes.put(KEY_INDEX_LINKS, size);
        size = prefs.getInt(PREF_MAX + KEY_INDEX_LINKS, 5);
        maxSizes.put(KEY_INDEX_LINKS, size);
        size = prefs.getInt(PREF_DEFAULT + KEY_TEMPLATE_LINKS, 3);
        defaultSizes.put(KEY_TEMPLATE_LINKS, size);
        size = prefs.getInt(PREF_MAX + KEY_TEMPLATE_LINKS, 5);
        maxSizes.put(KEY_TEMPLATE_LINKS, size);

        sectionCacheFrequency = prefs.getInt(PREF_SECTION_CACHE_FREQUENCY, 6);
        
        Preferences subprefs = prefs.node(PREF_MAIN_FORUMS);
        String order = subprefs.get("order", null);

        StringTokenizer stk = new StringTokenizer(order, ",");
        mainForums = new LinkedHashMap<Integer, Integer>(stk.countTokens());

        while (stk.hasMoreTokens()) {
            String srid = stk.nextToken();
            int rid = Integer.parseInt(srid);
            int questions = Integer.parseInt(subprefs.get(srid, null));

            mainForums.put(rid, questions);
        }
    }
}
