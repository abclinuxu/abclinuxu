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
import cz.abclinuxu.data.Link;
import cz.abclinuxu.data.view.Desktop;
import cz.abclinuxu.data.view.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.persistence.*;
import cz.abclinuxu.persistence.extra.CompareCondition;
import cz.abclinuxu.persistence.extra.Field;
import cz.abclinuxu.persistence.extra.LimitQualifier;
import cz.abclinuxu.persistence.extra.Operation;
import cz.abclinuxu.persistence.extra.Qualifier;
import cz.abclinuxu.persistence.extra.SpecialValue;
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
    public static final String KEY_SOFTWARE = "software";
    public static final String KEY_DRIVER = "driver";
    public static final String KEY_STORY = "story";
    public static final String KEY_DIGEST_STORY = "digest.story";
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
    public static final String KEY_EVENT = "event";
    public static final String KEY_VIDEO = "video";
    public static final String KEY_SUBPORTAL_ARTICLE = "sp.article";

    public static final String PREF_DEFAULT = "default.";
    public static final String PREF_MAX = "max.";
    public static final String PREF_INDEX_FEEDS = "feeds.for.index";
    public static final String PREF_TEMPLATE_FEEDS = "feeds.for.template";
    public static final String PREF_SECTION_CACHE_FREQUENCY = "section.cache.rebuild.frequency";
    public static final String PREF_MAIN_FORUMS = "forums";

    List<Relation> freshHardware, freshSoftware, freshDrivers, freshStories, freshDigestStories, freshArticles;
    List<Relation> freshQuestions, freshFaqs, freshDictionary, freshBazaarAds, freshPersonalities, freshNews;
    List<Relation> freshTrivias, freshEvents, freshVideos, freshHPSubportalArticles;
    List<Relation> topSubportals;
    List<Map> latestSubportalChanges;
    Map<Integer, List<Relation>> freshSubportalArticles, freshForumQuestions, freshSubportalWikiPages;
    Map<Integer, Relation> nextSubportalEvent, freshSubportalEvent;

    Map<Integer, Integer> mainForums;
    List<CloudTag> freshCloudTags;
    Map<Relation, Integer> mostReadStories, mostReadArticles;
    Map<Relation, Integer> mostCommentedArticles, mostCommentedStories;
    Map<Relation, Integer> recentMostReadArticles, recentMostCommentedArticles;
    Map<Relation, Integer> recentMostCommentedStories, recentMostReadStories;
    Map<Relation, Integer> mostSeenDesktops, mostCommentedDesktops, mostPopularDesktops;
    Map<Relation, Integer> recentMostSeenDesktops, recentMostCommentedDesktops, recentMostPopularDesktops;
    Map<Relation, Integer> recentMostVisitedSoftware, recentMostPopularSoftware;
    Map<Relation, Integer> mostVisitedSoftware, mostPopularSoftware;
    Map<Relation, Integer> mostCommentedNews, recentMostCommentedNews;
    Map<Relation, Integer> mostVotedOnPolls, mostCommentedPolls, recentMostVotedOnPolls, recentMostCommentedPolls;
	Map<User, Integer> highestScoreUsers;

    List<Desktop> freshDesktops;
    String indexFeeds, templateFeeds;
    Map<String, Integer> defaultSizes, maxSizes, counter;
    Map<Relation, Map<String, Integer>> subportalCounter;
    Map<Server, List<Link>> feedLinks;
    Map<Integer, Map<Server, List<Link>>> feedSubportalLinks;
    SectionTreeCache faqTree, softwareTree, hardwareTree, articleTree;
    Relation currentPoll;
    int sectionCacheFrequency;
    HostingServer hostingServer;
    JobsCzHolder jobsCzHolderHP, jobsCzHolderPage;
    Map<String, ShopProduct> shopProducts;

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
    public List<Relation> getFreshHardware(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_HARDWARE, null);
        return getSubList(freshHardware, userLimit);
    }

    /**
     * List of the most fresh software relations according to user preference or system setting.
     */
    public List<Relation> getFreshSoftware(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_SOFTWARE, null);
        return getSubList(freshSoftware, userLimit);
    }

    /**
     * List of the most fresh driver relations according to user preference or system setting.
     */
    public List<Relation> getFreshDrivers(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_DRIVER, null);
        return getSubList(freshDrivers, userLimit);
    }

    /**
     * List of the most fresh blog story relations according to user preference or system setting.
     */
    public List<Relation> getFreshStories(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_STORY, "/data/settings/index_stories");
        return Tools.filterBannedStories(freshStories, user, userLimit, true);
    }

    /**
     * Array of Lists of the most fresh blog story relations. Returned relations count is set according to
     * user preference or system setting. The first list holds relations categorized as digest,
     * the second List holds normal relations excluding those from digest. Banned stories and blocked users
     * may be filtered out.
     */
    public List<Relation>[] getFreshDigestStories(Object user) {
        int digestCount = getObjectCountForUser(user, KEY_DIGEST_STORY, "/data/settings/index_digest_stories");
        int normalCount = getObjectCountForUser(user, KEY_STORY, "/data/settings/index_stories");
        Set blockedUsers = Collections.EMPTY_SET;
        List<Relation> resultNormal = new ArrayList<Relation>(normalCount);
        List<Relation> resultDigest = new ArrayList<Relation>(digestCount);
        boolean filterBanned = true;

        if (user != null && (user instanceof User)) {
            User someUser = (User) user;
            blockedUsers = Tools.getBlacklist(someUser, true);

            if (filterBanned)
                filterBanned = Misc.getNodeSetting(someUser.getData(), "/data/settings/hp_all_stories", true);
        }

        for (Relation relation : freshDigestStories) {
            if (! blockedUsers.isEmpty()) {
                Item story = (Item) relation.getChild();
                if (blockedUsers.contains(new Integer(story.getOwner())))
                    continue;
            }
            resultDigest.add(relation);
            if (resultDigest.size() == digestCount)
                break;
        }

        for (Relation relation : freshStories) {
            if (resultDigest.contains(relation))
                continue;

            Item story = (Item) relation.getChild();
            if (! blockedUsers.isEmpty()) {
                if (blockedUsers.contains(new Integer(story.getOwner())))
                    continue;
            }

            if (filterBanned) {
                if (story.getSingleProperty(Constants.PROPERTY_BANNED_BLOG) != null)
                    continue;
            }

            resultNormal.add(relation);
            if (resultNormal.size() == normalCount)
                break;
        }

        return new List[] {resultDigest, resultNormal};
    }

    /**
     * List of the most fresh article relations according to user preference or system setting.
     */
    public List<Relation> getFreshArticles(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_ARTICLE, "/data/settings/index_articles");
        return getSubList(freshArticles, userLimit);
    }

    /**
     * List of the subportals with highest score.
     */
    public List<Relation> getTopSubportals(int count) {
        return getSubList(topSubportals, count);
    }

    /**
     * List of the freshest subportal article relations according to user preference or system setting.
     */
    public List<Relation> getFreshHPSubportalArticles(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_SUBPORTAL_ARTICLE, null);
        return getSubList(freshHPSubportalArticles, userLimit);
    }

    /**
     * List of the most fresh article relations from a subportal
     * according to user preference or system setting.
     */
    public List<Relation> getFreshSubportalArticles(Object user, int rel) {
        int userLimit = getObjectCountForUser(user, KEY_ARTICLE, null);
        List<Relation> articles = freshSubportalArticles.get(rel);

        if (articles == null)
            return Collections.emptyList();
        else
            return getSubList(articles, userLimit);
    }

    /**
     * List of the most fresh content relations from a subportal
     * according to user preference or system setting.
     */
    public List<Relation> getFreshSubportalWikiPages(Object user, int rel) {
        int userLimit = getObjectCountForUser(user, KEY_SOFTWARE, null);
        List<Relation> articles = freshSubportalWikiPages.get(rel);

        if (articles == null)
            return Collections.emptyList();
        else
            return getSubList(articles, userLimit);
    }

    /**
     * Returns the nearest upcoming event for the subportal specified.
     * @param rel A subportal
     * @return An event, if any
     */
    public Relation getFreshSubportalEvent(int rel) {
        if (nextSubportalEvent != null)
            return nextSubportalEvent.get(rel);
        else
            return null;
    }

    public List getLatestSubportalChanges(Object user) {
        if (latestSubportalChanges == null)
            return Collections.EMPTY_LIST;

        int userLimit = /*getObjectCountForUser(user, KEY_SOFTWARE, null)*/ 10;
        return getSubList(latestSubportalChanges, userLimit);
    }

    public List getAllSubportalChanges() {
        return latestSubportalChanges;
    }

    /**
     * List of the most fresh discussion question relations from a subportal
     * according to user preference or system setting.
     */
    public List<Relation> getFreshQuestions(int count, int rid) {
        if (freshForumQuestions == null)
            return Collections.emptyList();

        List<Relation> questions = freshForumQuestions.get(rid);
        if (questions == null)
            return Collections.emptyList();
        else
            return getSubList(questions, count);
    }

    public List<Relation> getFreshQuestions(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_QUESTION, "/data/settings/index_discussions");
        return getSubList(freshQuestions, userLimit);
    }

    public List<Relation> getFreshQuestions(int count) {
        return getSubList(freshQuestions, count);
    }

    /**
     * List of the most fresh frequently asked question relations according to user preference or system setting.
     */
    public List<Relation> getFreshFaqs(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_FAQ, null);
        return getSubList(freshFaqs, userLimit);
    }

    /**
     * List of the most fresh dictionary relations according to user preference or system setting.
     */
    public List<Relation> getFreshDictionary(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_DICTIONARY, null);
        return getSubList(freshDictionary, userLimit);
    }

    /**
     * List of the most fresh personality relations according to user preference or system setting.
     */
    public List<Relation> getFreshPersonalities(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_PERSONALITIES, null);
        return getSubList(freshPersonalities, userLimit);
    }

    /**
     * List of the most fresh dictionary relations according to user preference or system setting.
     */
    public List<Relation> getFreshBazaarAds(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_BAZAAR, null);
        return getSubList(freshBazaarAds, userLimit);
    }

    /**
     * List of the most fresh news relations according to user preference or system setting.
     */
    public List<Relation> getFreshNews(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_NEWS, "/data/settings/index_news");
        return getSubList(freshNews, userLimit);
    }

    /**
     * List of the most fresh news relations according to user preference or system setting.
     */
    public List<Relation> getFreshTrivia(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_TRIVIA, null);
        return getSubList(freshTrivias, userLimit);
    }

    /**
     * List of the most fresh event relations according to user preference or system setting.
     */
    public List<Relation> getFreshEvents(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_EVENT, null);
        return getSubList(freshEvents, userLimit);
    }

    /**
     * List of the most fresh screenshot relations according to user preference or system setting.
     */
    public List<Desktop> getFreshDesktops(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_SCREENSHOT, "/data/settings/index_screenshots");
        return getSubList(freshDesktops, userLimit);
    }

    /**
     * List of the freshest video relations according to user preference or system setting.
     */
    public List<Relation> getFreshVideos(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_VIDEO, "/data/settings/index_screenshots");
        return getSubList(freshVideos, userLimit);
    }

    public List<JobsCzItem> getFreshJobsCz(Object user) {
        int userLimit = getObjectCountForUser(user, KEY_JOBSCZ, null);
        return getSubList(jobsCzHolderHP.getJobsList(), userLimit);
    }

    public List<CloudTag> getFreshCloudTags(Object user) {
    	int userLimit = getObjectCountForUser(user, KEY_TAGCLOUD, null);
        return getSubList(freshCloudTags, userLimit);
    }

    public Map<Relation,Integer> getMostReadArticles() {
        return mostReadArticles;
    }

    public Map<Relation,Integer> getMostReadStories() {
        return mostReadStories;
    }

    public Map<Relation,Integer> getRecentMostReadArticles() {
        return recentMostReadArticles;
    }

    public Map<Relation,Integer> getRecentMostReadStories() {
        return recentMostReadStories;
    }

    public Map<Relation,Integer> getMostCommentedArticles() {
        return mostCommentedArticles;
    }

    public Map<Relation,Integer> getMostCommentedStories() {
        return mostCommentedStories;
    }

    public Map<Relation,Integer> getMostCommentedNews() {
        return mostCommentedNews;
    }

    public Map<Relation,Integer> getRecentMostCommentedArticles() {
        return recentMostCommentedArticles;
    }

    public Map<Relation,Integer> getRecentMostCommentedStories() {
        return recentMostCommentedStories;
    }

    public Map<Relation,Integer> getRecentMostCommentedNews() {
        return recentMostCommentedNews;
    }

	public Map<User,Integer> getHighestScoreUsers() {
		return highestScoreUsers;
	}

    public Map<Relation,Integer> getMostCommentedPolls() {
		return mostCommentedPolls;
	}

    public Map<Relation,Integer> getMostVotedOnPolls() {
		return mostVotedOnPolls;
	}

    public Map<Relation, Integer> getMostSeenDesktops() {
        return mostSeenDesktops;
    }

    public Map<Relation, Integer> getMostCommentedDesktops() {
        return mostCommentedDesktops;
    }

    public Map<Relation, Integer> getMostPopularDesktops() {
        return mostPopularDesktops;
    }

    public Map<Relation, Integer> getRecentMostSeenDesktops() {
        return recentMostSeenDesktops;
    }

    public Map<Relation, Integer> getRecentMostCommentedDesktops() {
        return recentMostCommentedDesktops;
    }

    public Map<Relation, Integer> getRecentMostPopularDesktops() {
        return recentMostPopularDesktops;
    }

    public Map<Relation, Integer> getRecentMostVisitedSoftware() {
        return recentMostVisitedSoftware;
    }

    public Map<Relation, Integer> getRecentMostPopularSoftware() {
        return recentMostPopularSoftware;
    }

    public Map<Relation, Integer> getMostVisitedSoftware() {
        return mostVisitedSoftware;
    }

    public Map<Relation, Integer> getMostPopularSoftware() {
        return mostPopularSoftware;
    }

    /**
     * Finds list of servers and their links to be displayed for this user. If user does not want
     * to see any links, empty map is returned.
     * @param maybeUser either instance of User or other value
     * @param index true if home page is being displayed
     * @return Map where key is initialized server and value is list of Links
     */
    public Map<Server, List<Link>> getFeeds(Object maybeUser, boolean index) {
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
            return Collections.emptyMap();

        element = (Element) document.selectSingleNode("/data/settings/feeds");
        if (element == null || element.getText() == null)
            return getSelectedFeeds(defaultServers, userLimit);

        return getSelectedFeeds(element.getText(), userLimit);
    }

    public Map getSubportalFeeds(Object user, int rid) {
        if (feedSubportalLinks == null)
            return Collections.EMPTY_MAP;

        int userLimit = getObjectCountForUser(user, KEY_INDEX_LINKS, "/data/settings/index_links");

        Map<Server, List<Link>> map = feedSubportalLinks.get(rid);
        if (map == null)
            return Collections.EMPTY_MAP;

        map = new HashMap<Server, List<Link>>(map);
        for (Map.Entry<Server,List<Link>> entry : map.entrySet()) {
            List<Link> list = getSubList(entry.getValue(), userLimit);
            entry.setValue(list);
        }

        return map;
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

    /**
     * @return Jobs.cz holder
     */
     public JobsCzHolder getJobsCzHolder() {
         return jobsCzHolderPage;
     }

    /**
     * Stores shop products.
     * @param shop unused at this moment
     * @param products list of initialized products
     */
    public void setShopProducts(String shop, List<ShopProduct> products) {
        Map<String, ShopProduct> newShopProducts = new HashMap<String, ShopProduct>();
        for (ShopProduct product : products) {
            newShopProducts.put(product.getId(), product);
        }
        shopProducts = newShopProducts;
    }

    /**
     * Retreives selected product from given shop.
     * @param shop unused at this moment
     * @param id product id
     * @return instance with product information or null, if not found
     */
    public ShopProduct getProduct(String shop, String id) {
        return shopProducts.get(id);
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
        Integer defaultNumber = defaultSizes.get(key);
        if (o == null || xpath==null || !(o instanceof User))
            return defaultNumber;

        User user = (User) o;
        Node node = user.getData().selectSingleNode(xpath);
        if (node == null)
            return defaultNumber;

        int count = Misc.parseInt(node.getText(), defaultNumber);
        if (count<0)
            return defaultNumber;

        Integer maximum = maxSizes.get(key);
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
            refreshLatestSubportalChanges();
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
            refreshSubportalFeedLinks();
            refreshSectionCaches();
            refreshDesktops();
            refreshVideos();
            refreshCloudTags();
            refreshTrivia();
            refreshEvents();
            refreshTopSubportals();

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

    private void refreshSubportalFeedLinks() {
        try {
            Category subportals = new Category(Constants.CAT_SUBPORTALS);
            List<Relation> children = Tools.syncList(subportals.getChildren());
            Map<Integer, Map<Server, List<Link>>> data = new HashMap<Integer, Map<Server, List<Link>>>();

            for (Relation rel : children) {
                Map<Server, List<Link>> feeds = UpdateLinks.getFeeds(rel.getChild().getId());
                for (List links : feeds.values())
                    Sorters2.byDate(links, Sorters2.DESCENDING);

                data.put(rel.getId(), feeds);
            }

            feedSubportalLinks = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani odkazu feedu podportalu", e);
        }
    }

    public void refreshNews() {
        try {
            int maximum = maxSizes.get(KEY_NEWS);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> news = SQLTool.getInstance().findNewsRelations(qualifiers);
            Tools.syncList(news);
            freshNews = news;
        } catch (Exception e) {
            log.error("Selhalo nacitani zpravicek", e);
        }
    }

    public void refreshArticles() {
        try {
            int maximum = maxSizes.get(KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> articles = sqlTool.findIndexArticlesRelations(qualifiers);
            Tools.syncList(articles);
            freshArticles = articles;
        } catch (Exception e) {
            log.error("Selhalo nacitani clanku", e);
        }
    }

    public void refreshTopSubportals() {
        try {
            int maximum = 5; // maybe introduce custom settings
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findSubportalsOrderedByScore(qualifiers);
            Tools.syncList(list);
            topSubportals = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani nej skupin", e);
        }
    }

    public void refreshHPSubportalArticles() {
        try {
            int maximum = maxSizes.get(KEY_SUBPORTAL_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> articles = sqlTool.findHPSubportalArticles(qualifiers);
            Tools.syncList(articles);
            freshHPSubportalArticles = articles;
        } catch (Exception e) {
            log.error("Selhalo nacitani clanku ze skupin pro HP", e);
        }
    }

    public void refreshSubportalEvents(Relation where) {
        try {
            Map<Integer, Relation> map, mapUpdated;
            List<Relation> children;
            String date = Constants.isoFormat.format(new Date());

            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap<Integer, Relation>();
                mapUpdated = new HashMap<Integer, Relation>();
                children = Tools.syncList(subportals.getChildren());
            } else {
                map = nextSubportalEvent;
                mapUpdated = freshSubportalEvent;
                children = Collections.singletonList((Relation) Tools.sync(where));
            }

            // get an event for every subportal
            for (Relation rel : children) {
                int rid = Misc.parseInt(Tools.xpath(rel.getChild(), "/data/events"), 0);

                // get the next event
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

                // get the last modified event
                qualifiers = new Qualifier[] {
                    new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, date),
                    new CompareCondition(Field.UPPER, Operation.EQUAL, rid),
                    Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING,
                    new LimitQualifier(0, 1)
                };

                list = sqlTool.findItemRelationsWithType(Item.EVENT, qualifiers);

                if (!Misc.empty(list)) {
                    Relation event = list.get(0);
                    Tools.sync(event);
                    mapUpdated.put(rid, event);
                }
            }

            nextSubportalEvent = map;
            freshSubportalEvent = mapUpdated;
        } catch (Exception e) {
            log.error("Selhalo nacitani akci pro subportaly", e);
        }
    }

    public void refreshSubportalWikiPages(Relation where) {
        try {
            Map<Integer, List<Relation>> map;
            List<Relation> children;
            Persistence persistence = PersistenceFactory.getPersistence();

            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap<Integer, List<Relation>>();
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
                try {
                    Tools.sync(wikiRelation);
                } catch (Exception e) {
                    log.warn("Wiki " + rid + " nebyla nalezena");
                    continue;
                }
                changes = ContentChanges.changedContentList(wikiRelation, persistence, ContentChanges.COLUMN_DATE, true);
                result = new ArrayList<Relation>(changes.size());

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
            Map<Integer, List<Relation>> map;
            int maximum = maxSizes.get(KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};

            // get a list of subportals
            if (where == null) {
                Category subportals = new Category(Constants.CAT_SUBPORTALS);
                map = new HashMap<Integer, List<Relation>>();
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

    public void refreshLatestSubportalChanges() {
        // get the latest change for every subportal
        Map<Integer,List<Relation>> latestSubportal = new HashMap<Integer, List<Relation>>();
        List<Map> latestChanges;

        // add articles to the mix
        for (Integer rid : freshSubportalArticles.keySet()) {
            Relation relArticles = new Relation(rid);
            Tools.sync(relArticles);
            List<Relation> listObjects = latestSubportal.get(relArticles.getUpper());

            if (listObjects == null)
                listObjects = new ArrayList<Relation>();

            listObjects.addAll(freshSubportalArticles.get(rid));
            latestSubportal.put(relArticles.getUpper(), listObjects);
        }

        // add wiki pages
        for (Integer rid : freshSubportalWikiPages.keySet()) {
            Relation relArticles = new Relation(rid);
            Tools.sync(relArticles);
            List<Relation> listObjects = latestSubportal.get(relArticles.getUpper());

            if (listObjects == null)
                listObjects = new ArrayList<Relation>();

            listObjects.addAll(freshSubportalWikiPages.get(rid));
            latestSubportal.put(relArticles.getUpper(), listObjects);
        }

        // add events
        for (Integer rid : freshSubportalEvent.keySet()) {
            Relation relArticles = new Relation(rid);
            Tools.sync(relArticles);
            List<Relation> listObjects = latestSubportal.get(relArticles.getUpper());

            if (listObjects == null)
                listObjects = new ArrayList<Relation>();

            listObjects.add(freshSubportalEvent.get(rid));
            latestSubportal.put(relArticles.getUpper(), listObjects);
        }

        latestChanges = new ArrayList<Map>(latestSubportal.size());

        // now perform sorting
        Comparator comparator = new SubportalChangeComparator();
        for (Map.Entry<Integer,List<Relation>> entry : latestSubportal.entrySet()) {
            List<Relation> list = entry.getValue();
            if (list.isEmpty())
                continue;

            Collections.sort(list, comparator);
            // get the latest change
            Map map = new HashMap(2);
            map.put("relation", list.get(list.size()-1));
            map.put("subportal", Tools.createRelation(entry.getKey()));
            latestChanges.add(map);
        }

        Collections.sort(latestChanges, comparator);
        Collections.reverse(latestChanges);

        latestSubportalChanges = latestChanges;
    }

    public void refreshForumQuestions() {
        try {
            Map<Integer, List<Relation>> map;
            Category subportals = new Category(Constants.CAT_SUBPORTALS);
            int maximum = maxSizes.get(KEY_QUESTION);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};

            // get a list of subportals
            List<Relation> children = Tools.syncList(subportals.getChildren());
            map = new HashMap<Integer, List<Relation>>(children.size() + mainForums.size());

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
            refreshCombinedSubportalQuestions();
        } catch (Exception e) {
            log.error("Selhalo nacitani diskuzi pro sekce", e);
        }
    }

    public void refreshForumQuestions(int rid) {
        int maximum = maxSizes.get(KEY_QUESTION);
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
        List<Relation> dizs = sqlTool.findDiscussionRelationsWithParent(rid, qualifiers);
        Tools.syncList(dizs);

        freshForumQuestions.put(rid, dizs);
        refreshCombinedSubportalQuestions();
    }

    /**
     * This function is to be called only from refreshForumQuestions
     */
    public void refreshCombinedSubportalQuestions() {
        List<Relation> combined = new ArrayList<Relation>(40 * freshForumQuestions.size());
        int maximum = maxSizes.get(KEY_QUESTION);

        for (Map.Entry<Integer,List<Relation>> entry : freshForumQuestions.entrySet()) {
            if (mainForums.containsKey(entry.getKey()) || entry.getKey() < 0)
                continue;
            combined.addAll(entry.getValue());
        }

        Sorters2.byDate(combined, Sorters2.DESCENDING);
        freshForumQuestions.put(-1, getSubList(combined, maximum));
    }

    public void refreshQuestions() {
        try {
            int maximum = maxSizes.get(KEY_QUESTION);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> found = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.syncList(found);
            freshQuestions = found;
        } catch (Exception e) {
            log.error("Selhalo nacitani dotazu ve foru", e);
        }
    }

    public void refreshStories() {
        try {
            int maximum = maxSizes.get(KEY_DIGEST_STORY);
            Set<String> values = Collections.singleton("yes");
            CompareCondition compareCondition = new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, SpecialValue.NOW);
            Qualifier[] qualifiers = new Qualifier[]{compareCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            Map<String, Set<String>> filters = Collections.singletonMap(Constants.PROPERTY_BLOG_DIGEST, values);
            List<Relation> list = sqlTool.findItemRelationsWithTypeWithFilters(Item.BLOG, qualifiers, filters);
            synchronizeBlogRelations(list);
            freshDigestStories = list;

            maximum = maxSizes.get(KEY_STORY);
            qualifiers = new Qualifier[] {compareCondition, Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            list = sqlTool.findItemRelationsWithType(Item.BLOG, qualifiers);
            synchronizeBlogRelations(list);
            freshStories = list;

        } catch (Exception e) {
            log.error("Selhalo nacitani blogu", e);
        }
    }

    private void synchronizeBlogRelations(List<Relation> list) {
        List blogs = new ArrayList(list.size());
        for (Object aList : list) {
            Relation relation = (Relation) aList;
            blogs.add(relation.getParent());
        }
        Tools.syncList(blogs); // parent on relation must be synchronized
        Tools.syncList(list);
    }

    public void refreshHardware() {
        try {
            int maximum = maxSizes.get(KEY_HARDWARE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(list);
            freshHardware = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani hardwaru", e);
        }
    }

    public void refreshSoftware() {
        try {
            int maximum = maxSizes.get(KEY_SOFTWARE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            Tools.syncList(list);
            freshSoftware = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani softwaru", e);
        }
    }

    public void refreshDrivers() {
        try {
            int maximum = maxSizes.get(KEY_DRIVER);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.DRIVER, qualifiers);
            Tools.syncList(list);
            freshDrivers = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani ovladacu", e);
        }
    }

    public void refreshDictionary() {
        try {
            int maximum = maxSizes.get(KEY_DICTIONARY);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.DICTIONARY, qualifiers);
            Tools.syncList(data);
            freshDictionary = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani pojmu", e);
        }
    }

    public void refreshTrivia() {
        try {
            int maximum = maxSizes.get(KEY_TRIVIA);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.TRIVIA, qualifiers);
            Tools.syncList(data);
            freshTrivias = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani kvizu", e);
        }
    }

    public void refreshEvents() {
        try {
            int maximum = maxSizes.get(KEY_EVENT);
            Qualifier[] qualifiers = new Qualifier[] {
                new CompareCondition(Field.CREATED, Operation.GREATER_OR_EQUAL, SpecialValue.NOW),
                Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(0, maximum)
            };
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.EVENT, qualifiers);
            Tools.syncList(data);
            freshEvents = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani akci", e);
        }
    }

    public void refreshPersonalities() {
        try {
            int maximum = maxSizes.get(KEY_PERSONALITIES);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> data = sqlTool.findItemRelationsWithType(Item.PERSONALITY, qualifiers);
            Tools.syncList(data);
            freshPersonalities = data;
        } catch (Exception e) {
            log.error("Selhalo nacitani osobnosti", e);
        }
    }

    public void refreshFaq() {
        try {
            int maximum = maxSizes.get(KEY_FAQ);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.FAQ, qualifiers);
            Tools.syncList(list);
            freshFaqs = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani casto kladenych otazek", e);
        }
    }

    public void refreshBazaar() {
        try {
            int maximum = maxSizes.get(KEY_BAZAAR);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.BAZAAR, qualifiers);
            Tools.syncList(list);
            freshBazaarAds = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani inzeratu z bazaru", e);
        }
    }

    public void refreshDesktops() {
        try {
            int maximum = maxSizes.get(KEY_SCREENSHOT);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)};
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.DESKTOP, qualifiers);
            Tools.syncList(list);

            List<Desktop> result = new ArrayList<Desktop>(list.size());
            for (Relation relation : list) {
                result.add(new Desktop(relation));
            }
            freshDesktops = result;
        } catch (Exception e) {
            log.error("Selhalo nacitani desktopu", e);
        }
    }

    public void refreshVideos() {
        try {
            int maximum = maxSizes.get(KEY_VIDEO);
            Qualifier[] qualifiers = new Qualifier[] {
                new CompareCondition(Field.UPPER, Operation.EQUAL, Constants.REL_VIDEOS),
                Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maximum)
            };
            List<Relation> list = sqlTool.findItemRelationsWithType(Item.VIDEO, qualifiers);
            Tools.syncList(list);

            freshVideos = list;
        } catch (Exception e) {
            log.error("Selhalo nacitani videi", e);
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

    public void refreshTopStatistics() {
        try {
            Qualifier[] qualifiers = new Qualifier[]{ new LimitQualifier(0, 10) };
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            Date monthAgo = cal.getTime();

            mostReadArticles = sqlTool.getMostCountedRelations(Item.ARTICLE, Constants.COUNTER_READ, null, qualifiers);
            mostCommentedArticles = sqlTool.getMostCommentedRelations(Item.ARTICLE, null, qualifiers);
            mostReadStories = sqlTool.getMostCountedRelations(Item.BLOG, Constants.COUNTER_READ, null, qualifiers);
            mostCommentedStories = sqlTool.getMostCommentedRelations(Item.BLOG, null, qualifiers);
            mostSeenDesktops = sqlTool.getMostCountedRelations(Item.DESKTOP, Constants.COUNTER_READ, null, qualifiers);
            mostCommentedDesktops = sqlTool.getMostCommentedRelations(Item.DESKTOP, null, qualifiers);
            mostPopularDesktops = sqlTool.getMostHavingPropertyRelations(Item.DESKTOP, Constants.PROPERTY_FAVOURITED_BY, null, qualifiers);
            mostVisitedSoftware = sqlTool.getMostCountedRelations(Item.SOFTWARE, Constants.COUNTER_VISIT, null, qualifiers);
            mostPopularSoftware = sqlTool.getMostHavingPropertyRelations(Item.SOFTWARE, Constants.PROPERTY_USED_BY, null, qualifiers);
            mostCommentedNews = sqlTool.getMostCommentedRelations(Item.NEWS, null, qualifiers);
            mostCommentedPolls = sqlTool.getMostCommentedPolls(qualifiers);
            mostVotedOnPolls = sqlTool.getMostVotedPolls(qualifiers);
			highestScoreUsers = sqlTool.getHighestScoreUsers(qualifiers);

            recentMostReadArticles = sqlTool.getMostCountedRelations(Item.ARTICLE, Constants.COUNTER_READ, monthAgo, qualifiers);
            recentMostCommentedArticles = sqlTool.getMostCommentedRelations(Item.ARTICLE, monthAgo, qualifiers);
            recentMostReadStories = sqlTool.getMostCountedRelations(Item.BLOG, Constants.COUNTER_READ, monthAgo, qualifiers);
            recentMostCommentedStories = sqlTool.getMostCommentedRelations(Item.BLOG, monthAgo, qualifiers);
            recentMostSeenDesktops = sqlTool.getMostCountedRelations(Item.DESKTOP, Constants.COUNTER_READ, monthAgo, qualifiers);
            recentMostCommentedDesktops = sqlTool.getMostCommentedRelations(Item.DESKTOP, monthAgo, qualifiers);
            recentMostPopularDesktops = sqlTool.getMostHavingPropertyRelations(Item.DESKTOP, Constants.PROPERTY_FAVOURITED_BY, monthAgo, qualifiers);
            recentMostVisitedSoftware = sqlTool.getMostCountedRelations(Item.SOFTWARE, Constants.COUNTER_VISIT, monthAgo, qualifiers);
            recentMostPopularSoftware = sqlTool.getMostHavingPropertyRelations(Item.SOFTWARE, Constants.PROPERTY_USED_BY, monthAgo, qualifiers);
            recentMostCommentedNews = sqlTool.getMostCommentedRelations(Item.NEWS, monthAgo, qualifiers);
        } catch (Exception e) {
            log.error("Selhalo obnoveni top statistik portalu", e);
        }
    }

    /**
     * Constructs list of most used tags
     */
    public void refreshCloudTags() {
    	try {
    		int count = defaultSizes.get(KEY_TAGCLOUD);
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
    		log.error("Selhalo nacitani tag cloud", e);
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
                map = new HashMap<Relation, Map<String, Integer>>();
                children = Tools.syncList(subportals.getChildren());
            } else {
                map = subportalCounter;
                children = Collections.singletonList((Relation) Tools.sync(where));
            }

            double total = sqlTool.maxSubportalReads();

            for (Relation rel : children) {
                Map<String,Integer> portal = new HashMap<String, Integer>(6);

                Relation articles = Tools.createRelation(Tools.xpath(rel.getChild(), "//articles"));
                Relation wiki = Tools.createRelation(Tools.xpath(rel.getChild(), "//wiki"));
                Relation forum = Tools.createRelation(Tools.xpath(rel.getChild(), "//forum"));
                Relation events = Tools.createRelation(Tools.xpath(rel.getChild(), "//events"));
                Relation pool = Tools.createRelation(Tools.xpath(rel.getChild(), "//article_pool"));

                portal.put("ARTICLES", persistence.findChildren(articles.getChild()).size());
                portal.put("WAITING_ARTICLES", persistence.findChildren(pool.getChild()).size());

                int wikiPages = 0;
                LinkedList<Relation> stack = new LinkedList<Relation>(persistence.findChildren(wiki.getChild()));
                while (stack.size() > 0) {
                    Relation current = stack.removeFirst();

                    wikiPages++;

                    stack.addAll(0, persistence.findChildren(current.getChild()));
                }

                portal.put("WIKIS", wikiPages);
                portal.put("QUESTIONS", persistence.findChildren(forum.getChild()).size());

                int numEvents = sqlTool.countItemRelationsWithType(Item.EVENT,
                        new Qualifier[] { new CompareCondition(Field.UPPER, Operation.EQUAL, events.getId()) });

                portal.put("EVENTS", numEvents);
                portal.put("WAITING_EVENTS", persistence.findChildren(events.getChild()).size() - numEvents);

                if (where == null && total > 0) {
                    double sp = Tools.getCounterValue(rel.getChild(), Constants.COUNTER_READ);
                    double pct = 100.0/(total/sp);
                    portal.put("READPCT", (int) pct);
                }

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

            try {
                server = (Server) persistence.findById(new Server(id));
            } catch (Exception e) {
                continue;
            }

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
        freshArticles = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_DICTIONARY, 3);
        defaultSizes.put(KEY_DICTIONARY, size);
        size = prefs.getInt(PREF_MAX + KEY_DICTIONARY, 10);
        maxSizes.put(KEY_DICTIONARY, size);
        freshDictionary = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_PERSONALITIES, 3);
        defaultSizes.put(KEY_PERSONALITIES, size);
        size = prefs.getInt(PREF_MAX + KEY_PERSONALITIES, 10);
        maxSizes.put(KEY_PERSONALITIES, size);
        freshDictionary = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_DRIVER, 3);
        defaultSizes.put(KEY_DRIVER, size);
        size = prefs.getInt(PREF_MAX + KEY_DRIVER, 10);
        maxSizes.put(KEY_DRIVER, size);
        freshDrivers = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_FAQ, 3);
        defaultSizes.put(KEY_FAQ, size);
        size = prefs.getInt(PREF_MAX + KEY_FAQ, 3);
        maxSizes.put(KEY_FAQ, size);
        freshFaqs = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_HARDWARE, 3);
        defaultSizes.put(KEY_HARDWARE, size);
        size = prefs.getInt(PREF_MAX + KEY_HARDWARE, 3);
        maxSizes.put(KEY_HARDWARE, size);
        freshHardware = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_SOFTWARE, 3);
        defaultSizes.put(KEY_SOFTWARE, size);
        size = prefs.getInt(PREF_MAX + KEY_SOFTWARE, 3);
        maxSizes.put(KEY_SOFTWARE, size);
        freshSoftware = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_NEWS, 5);
        defaultSizes.put(KEY_NEWS, size);
        size = prefs.getInt(PREF_MAX + KEY_NEWS, 5);
        maxSizes.put(KEY_NEWS, size);
        freshNews = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_QUESTION, 20);
        defaultSizes.put(KEY_QUESTION, size);
        size = prefs.getInt(PREF_MAX + KEY_QUESTION, 20);
        maxSizes.put(KEY_QUESTION, size);

        size = prefs.getInt(PREF_DEFAULT + KEY_STORY, 5);
        defaultSizes.put(KEY_STORY, size);
        size = prefs.getInt(PREF_MAX + KEY_STORY, 5);
        maxSizes.put(KEY_STORY, Tools.getPreloadedStoryCount(size));
        freshStories = Collections.emptyList();
        size = prefs.getInt(PREF_DEFAULT + KEY_DIGEST_STORY, 4);
        defaultSizes.put(KEY_DIGEST_STORY, size);
        size = prefs.getInt(PREF_MAX + KEY_DIGEST_STORY, 5);
        maxSizes.put(KEY_DIGEST_STORY, Tools.getPreloadedStoryCount(size));
        freshDigestStories = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_BAZAAR, 5);
        defaultSizes.put(KEY_BAZAAR, size);
        size = prefs.getInt(PREF_MAX + KEY_BAZAAR, 5);
        maxSizes.put(KEY_BAZAAR, size);
        freshBazaarAds = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_SCREENSHOT, 3);
        defaultSizes.put(KEY_SCREENSHOT, size);
        size = prefs.getInt(PREF_MAX + KEY_SCREENSHOT, 3);
        maxSizes.put(KEY_SCREENSHOT, size);
        freshDesktops = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_VIDEO, 3);
        defaultSizes.put(KEY_VIDEO, size);
        size = prefs.getInt(PREF_MAX + KEY_VIDEO, 3);
        maxSizes.put(KEY_VIDEO, size);
        freshVideos = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_EVENT, 3);
        defaultSizes.put(KEY_EVENT, size);
        size = prefs.getInt(PREF_MAX + KEY_EVENT, 3);
        maxSizes.put(KEY_EVENT, size);
        freshEvents = Collections.emptyList();

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
        freshTrivias = Collections.emptyList();

        size = prefs.getInt(PREF_DEFAULT + KEY_SUBPORTAL_ARTICLE, 2);
        defaultSizes.put(KEY_SUBPORTAL_ARTICLE, size);
        size = prefs.getInt(PREF_MAX + KEY_SUBPORTAL_ARTICLE, 5);
        maxSizes.put(KEY_SUBPORTAL_ARTICLE, size);
        freshHPSubportalArticles = Collections.emptyList();

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

    private static class SubportalChangeComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof Relation)
                return compare((Relation) o1, (Relation) o2);
            else if (o1 instanceof Map)
                return compare((Map) o1, (Map) o2);
            else
                return 0;
        }

        public int compare(Map m1, Map m2) {
            return compare((Relation) m1.get("relation"), (Relation) m2.get("relation"));
        }

        public int compare(Relation r1, Relation r2) {
            GenericDataObject gdo1 = (GenericDataObject) r1.getChild();
            GenericDataObject gdo2 = (GenericDataObject) r2.getChild();

            return gdo1.getUpdated().compareTo(gdo2.getUpdated());
        }
    }
}
