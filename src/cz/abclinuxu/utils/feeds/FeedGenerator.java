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
package cz.abclinuxu.utils.feeds;

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.scheduler.VariableFetcher;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.*;
import java.io.*;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedOutput;
import org.apache.log4j.Logger;
import org.dom4j.Node;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * This class generates various feeds for objects in the portal.
 * User: literakl
 * Date: 16.1.2005
 * TODO calls from servlets must be asynchronous, non-blocking for current user!
 */
public class FeedGenerator implements Configurable {
    private static Logger log = Logger.getLogger(FeedGenerator.class);

    public static final String TYPE_RSS_1_0 = "rss_1.0";

    static final String PREF_DISCUSSIONS = "diskuse";
    static final String PREF_ARTICLES = "clanky";
    static final String PREF_DRIVERS = "ovladace";
    static final String PREF_HARDWARE = "hardware";
    static final String PREF_SOFTWARE = "software";
    static final String PREF_DESKTOPS = "desktops";
    static final String PREF_BLOG = "blog";
    static final String PREF_BLOGS = "blogs";
    static final String PREF_BLOG_DIGEST = "blog.digest";
    static final String PREF_TRAFIKA = "trafika";
    static final String PREF_NEWS = "zpravicky";
    static final String PREF_POLLS = "ankety";
    static final String PREF_FAQ = "faq";
    static final String PREF_BAZAAR = "bazaar";
    static final String PREF_DICTIONARY = "dictionary";
    static final String PREF_PERSONALITIES = "personalities";
    static final String PREF_NEWS_WORD_LIMIT = "news.word.limit";
    static final String PREF_ARTICLE_SERIES = "article.series";
    static final String PREF_FORUMS = "forums";

    static String fileArticles, fileDrivers, fileHardware, fileBlog, dirBlogs, fileBlogDigest;
    static String fileNews, fileFaq, filePolls, fileTrafika, fileSoftware, fileBazaar, fileDictionary;
    static String fileDesktops, filePersonalities, fileDiscussions;
    static Map<Integer, String> filesArticleSeries;
    static Map<Integer, String> filesForums;
    static int feedLength = 10, highFrequencyFeedLength = 25, newsWordLimit;

    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new FeedGenerator());
    }

    /**
     * Generates RSS feed for all discussion forums
     */
    public static void updateForum() {
        findSubportalForums();
        
        for (Map.Entry<Integer, String> forum : filesForums.entrySet()) {
            updateForum(forum.getKey(), forum.getValue());
        }
        
        updateForumAll();
    }
    
    public static void findSubportalForums() {
        try {
            Category subportals = new Category(Constants.CAT_SUBPORTALS);
            List<Relation> children;
            
            children = Tools.syncList(subportals.getChildren());
            
            for (Relation sportal : children) {
                Category cat = (Category) sportal.getChild();
                int forum = Integer.parseInt(Tools.xpath(cat, "//forum"));
                
                filesForums.put(forum, "auto/skupiny"+sportal.getUrl()+"/poradna.rss");
            }
        } catch (Exception e) {
            log.error("Chyba pri nacitani poraden v podportalech", e);
        }
    }
    
    /**
     * Generates a RSS feed combinig all forums
     */
    public static void updateForumAll() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - aktuální diskuse", "/poradna",
                    "Seznam aktuálních diskusí v poradnách na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{new CompareCondition(Field.SUBTYPE, Operation.EQUAL, "question"),
                    Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)};
            List<Relation> list = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.syncList(list);
            
            for (Relation relation : list) {
                SyndEntry entry = createQuestionEntry(relation);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(getForumFeedUrl().substring(1));
            File dir = new File(path).getParentFile();
            if (!dir.exists())
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro poradny", e);
        }
    }

    /**
     * Generates RSS feed for a discussion forum
     */
    public static void updateForum(int relation) {
        updateForum(relation, filesForums.get(relation));
    }
    
    /**
     * Generates RSS feed for a discussion forum
     */
    public static void updateForum(int relationId, String file) {
        try {
            Relation forum = new Relation(relationId);
            Tools.sync(forum);
            Category cat = (Category) forum.getChild();

            SyndFeed feed = createSyndFeed("abclinuxu - " + cat.getTitle(), forum.getUrl(),
                    "Seznam aktuálních diskusí v \"" + cat.getTitle() + "\" na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{new CompareCondition(Field.UPPER, Operation.EQUAL, relationId),
                    Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)};
            List<Relation> list = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.syncList(list);

            for (Relation relation : list) {
                SyndEntry entry = createQuestionEntry(relation);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(file);
            File dir = new File(path).getParentFile();
            if (!dir.exists())
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro poradnu", e);
        }
    }

    /**
     * Generates RSS feed for driver database
     */
    public static void updateDrivers() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - databáze ovladačů", "/ovladace",
                    "Seznam čerstvých záznamů do databáze linuxových ovladačů na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.DRIVER, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), null, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileDrivers);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro ovladace", e);
        }
    }

    /**
     * Generates RSS feed for dictionary
     */
    public static void updateDictionary() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - výkladový slovník", "/slovnik",
                    "Výkladový slovník na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.DICTIONARY, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), null, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileDictionary);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro slovnik", e);
        }
    }

    /**
     * Generates RSS feed for personalities
     */
    public static void updatePersonalities() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - osobnosti", "/kdo-je",
                    "Osobnosti na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.PERSONALITY, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), null, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(filePersonalities);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro osobnosti", e);
        }
    }

    /**
     * Generates RSS feed for hardware database
     */
    public static void updateHardware() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - databáze hardwaru", "/hardware",
                    "Seznam čerstvých záznamů do databáze hardwarových poznatků na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), null, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileHardware);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro hardware",e);
        }
    }

    /**
     * Generates RSS feed for software catalog
     */
    public static void updateSoftware() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - katalog softwaru", "/software",
                    "Seznam čerstvých záznamů do softwarového katalogu na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndContent description = null;
            Node node;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                node = item.getData().selectSingleNode("/data/intro");
                if (node != null) {
                    description = new SyndContentImpl();
                    description.setType("text/plain");
                    description.setValue(node.getText());
                }
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), description, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileSoftware);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro software",e);
        }
    }

    /**
     * Generates RSS feed for desktops
     */
    public static void updateDesktops() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - desktopy", "/desktopy",
                    "Seznam čerstvých desktopů na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.DESKTOP, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), null, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileDesktops);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro desktopy",e);
        }
    }

    /**
     * Generates RSS feeds for article series and sections
     */
    public static void updateSeries() {
        try {
            for (Map.Entry<Integer, String> e : filesArticleSeries.entrySet()) {
                Relation relation = new Relation(e.getKey());
                Tools.sync(relation);
                GenericDataObject series = (GenericDataObject) relation.getChild();
                String url = "http://" + AbcConfig.getHostname() + relation.getUrl();

                SyndFeed feed = createSyndFeed("abclinuxu - " + series.getTitle(), url, null);

                if (series instanceof Category)
                    createSectionEntries(relation, feed, feedLength);
                else if (series instanceof Item)
                    createSeriesEntries((Item) series, feed, feedLength);
                else
                    throw new Exception("Neznamy typ objektu " + series.getId());

                String path = AbcConfig.calculateDeployedPath(e.getValue());
                Writer writer = getWriter(path);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feed, writer);
                writer.close();
            }
        } catch(Exception e) {
            log.error("Chyba pri generovani RSS pro serialy", e);
        }
    }

    /**
     * Create feed entries for article section
     * @param cat Category to create feed entries for
     * @param feed RSS feed
     * @param maxArticles Maximum amount of RSS entries
     */
    public static void createSectionEntries(Relation categoryRelation, SyndFeed feed, int maxArticles) {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maxArticles)};
        List<Relation> articles = SQLTool.getInstance().findArticleRelations(qualifiers, categoryRelation.getId());
        Category cat = (Category) categoryRelation.getChild();

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        feed.setDescription(Tools.xpath(cat, "/data/note"));
        feed.setEntries(entries);

        SyndEntry entry;
        for (Relation relation : articles) {
            Tools.sync(relation);
            entry = createArticleEntry(relation);
            entries.add(entry);
        }
    }

    /**
     * Create feed entries for article series
     * @param serie Serie to create feed entries for
     * @param feed RSS feed
     * @param maxArticles Maximum amount of RSS entries
     */
    public static void createSeriesEntries(Item serie, SyndFeed feed, int maxArticles) {
        List articlesElements = serie.getData().getRootElement().elements("article");
        int first = articlesElements.size()-maxArticles;
        articlesElements = articlesElements.subList(Math.max(first, 0), articlesElements.size());
        articlesElements = new ArrayList<Element>(articlesElements);
        Collections.reverse(articlesElements);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        feed.setDescription(Tools.xpath(serie, "/data/description"));
        feed.setEntries(entries);

        SyndEntry entry;
        for (Iterator iter = articlesElements.iterator(); iter.hasNext();) {
            Element article = (Element) iter.next();
            int rid = Misc.parseInt(article.getText(), 0);
            Relation relation = new Relation(rid);
            Tools.sync(relation);
            entry = createArticleEntry(relation);
            entries.add(entry);
        }
    }

    /**
     * Generates RSS and Trafika feed for articles
     */
    public static void updateArticles() {
        try {
            SQLTool sqlTool = SQLTool.getInstance();

            SyndFeed feed = createSyndFeed("abclinuxu - aktuální články", "/clanky",
                    "Seznam čerstvých článků na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Map defaultSizes = VariableFetcher.getInstance().getDefaultSizes();
            int countArticles = (Integer) defaultSizes.get(VariableFetcher.KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, countArticles)};
            List<Relation> articles = sqlTool.findIndexArticlesRelations(qualifiers);
            Tools.syncList(articles);
            for (Relation found : articles) {
                entry = createArticleEntry(found);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileArticles);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();

            path = AbcConfig.calculateDeployedPath(fileTrafika);
            writer = new FileWriter(path);
            String date;
            synchronized (Constants.isoFormat) {
                date = Constants.isoFormat.format(new Date());
            }
            writer.write(date);
            writer.write('\n');
            for (Iterator iter = feed.getEntries().iterator(); iter.hasNext();) {
                entry = (SyndEntry) iter.next();
                writer.write(entry.getLink() + "|\\" + entry.getTitle() + "\n");
            }
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro clanky", e);
        }

        updateSeries();
    }

    /**
     * Generates RSS feed for selected and all blogs
     */
    public static void updateBlog(Category blog)  {
        try {
            SQLTool sqlTool = SQLTool.getInstance();
            Persistence persistence = PersistenceFactory.getPersistence();

            if (blog!=null) {
                User author = (User) persistence.findById(new User(blog.getOwner()));
                SyndFeed feed = createSyndFeed(Tools.limit(blog.getTitle(), 40, "..."), blog.getSubType() + "/",
                        "Seznam čerstvých zápisů uživatele " + author.getName());

                List entries = new ArrayList();
                feed.setEntries(entries);
                SyndEntry entry;

                List qualifiers = new ArrayList();
                qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL, blog.getOwner()));
                qualifiers.add(new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, SpecialValue.NOW));
                qualifiers.add(Qualifier.SORT_BY_CREATED);
                qualifiers.add(Qualifier.ORDER_DESCENDING);
                qualifiers.add(new LimitQualifier(0, feedLength));
                Qualifier[] qa = new Qualifier[qualifiers.size()];
                List<Relation> stories = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
                Tools.syncList(stories);
                for (Relation found : stories) {
                    entry = createStoryEntry(found, author);
                    entries.add(entry);
                }

                String path = AbcConfig.calculateDeployedPath(dirBlogs + blog.getSubType() + ".rss");
                Writer writer = getWriter(path);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feed, writer);
                writer.close();
            }

            SyndFeed feed = createSyndFeed("abclinuxu - blogy", "/blog/",
                    "Seznam čerstvých zápisů uživatelů www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[] {
                new CompareCondition(Field.CREATED, Operation.SMALLER_OR_EQUAL, SpecialValue.NOW),
                Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)
            };
            List stories = sqlTool.findItemRelationsWithType(Item.BLOG, qualifiers);
            Tools.syncList(stories);
            List blogs = new ArrayList();
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                blogs.add(found.getParent());
            }
            Tools.syncList(blogs);
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                blog = (Category) persistence.findById(found.getParent());

                // remove banned blog stories
                Item story = (Item) found.getChild();
                if(story.getSingleProperty(Constants.PROPERTY_BANNED_BLOG) == null) {
                    User author = (User) persistence.findById(new User(blog.getOwner()));
                    entry = createStoryEntry(found, author);
                    entries.add(entry);
                }
            }

            String path = AbcConfig.calculateDeployedPath(fileBlog);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro blogy", e);
        }
    }

    /**
     * Generates blog digest feed
     */
    public static void updateBlogDigest() {
        try {
            SQLTool sqlTool = SQLTool.getInstance();
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = createSyndFeed("abclinuxu - výběr z blogů", "/blog/",
                    "Seznam pečlivě vybraných zápisů uživatelů www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Set<String> values = Collections.singleton("yes");
            Map<String, Set<String>> filters = Collections.singletonMap(Constants.PROPERTY_BLOG_DIGEST, values);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)};
            List<Relation> stories = sqlTool.findItemRelationsWithTypeWithFilters(Item.BLOG, qualifiers, filters);
            Tools.syncList(stories);

            List blogs = new ArrayList();
            for (Relation found : stories) {
                blogs.add(found.getParent());
            }
            Tools.syncList(blogs);

            for (Relation found : stories) {
                Category blog = (Category) persistence.findById(found.getParent());
                User author = (User) persistence.findById(new User(blog.getOwner()));
                entry = createStoryEntry(found, author);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileBlogDigest);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba při generování RSS pro digest blogu", e);
        }
    }

    /**
     * Generates RSS for news.
     */
    public static void updateNews() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - čerstvé zprávičky", "/zpravicky",
                    "Seznam čerstvých zpráviček na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndContent description;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findNewsRelations(qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();

                String content = Tools.xpath(item, "data/content");
                String withoutTags = Tools.removeTags(content);
                description = new SyndContentImpl();
                description.setType("text/plain");

                description.setValue(withoutTags);
                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), description, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileNews);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro zpravicky", e);
        }
    }

    /**
     * Generates RSS for faq.
     */
    public static void updateFAQ() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - často kladené otázky", "/faq",
                    "Seznam aktualizovaných otázek s odpověďmi na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndContent description;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.FAQ, qualifiers);
            Tools.syncList(list);
            for (Relation found : list) {
                Item item = (Item) found.getChild();
                String content = Tools.xpath(item, "data/text");
                String withoutTags = Tools.removeTags(content);

                description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue(Tools.limitWords(withoutTags, newsWordLimit, " ..."));

                SyndEntry entry = createSyndEntry(found.getUrl(), item.getTitle(), description, item.getUpdated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileFaq);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro faq", e);
        }
    }

    /**
     * Generates RSS feed for discussion forum
     */
    public static void updatePolls() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - ankety", "/ankety",
                    "Seznam anket na portálu www.abclinuxu.cz");

            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findStandalonePollRelations(qualifiers);
            Tools.syncList(list);
            for (Relation relation : list) {
                Poll poll = (Poll) relation.getChild();
                String title = Tools.removeTags(poll.getText());
                SyndEntry entry = createSyndEntry(relation.getUrl(), title, null, poll.getCreated());
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(filePolls);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro ankety", e);
        }
    }

    public static void updateBazaar() {
        try {
            SyndFeed feed = createSyndFeed("abclinuxu - bazar", "/bazar", "Seznam inzerátů z bazaru na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List<Relation> list = SQLTool.getInstance().findItemRelationsWithType(Item.BAZAAR, qualifiers);
            Tools.syncList(list);
            for (Relation relation : list) {
                Item item = (Item) relation.getChild();

                String url = relation.getUrl();
                if (url == null)
                    url = UrlUtils.PREFIX_BAZAAR + "/show/" + relation.getId();

                SyndEntry entry = createSyndEntry(url, item.getTitle(), null, item.getCreated());
                setAuthor(item, entry);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileBazaar);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro bazar", e);
        }
    }

    /**
     * Creates SyndFeed instance and initializes some properties.
     * @param title       title
     * @param url         required URI, it must be absolute, but local (starting with slash)
     * @param description description
     * @return
     */
    protected static SyndFeed createSyndFeed(String title, String url, String description) {
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType(TYPE_RSS_1_0);
        feed.setEncoding("UTF-8");
        feed.setTitle(title);
        url = AbcConfig.getAbsoluteUrl() + url;
        feed.setLink(url);
        feed.setUri(url);
        feed.setDescription(description);
        return feed;
    }

    /**
     * Creates new entry from parameters.
     * @param url         required URI, it must be absolute, but local (starting with slash)
     * @param title       required title for this entry
     * @param description optional description
     * @param published   optional time, when the content was published
     * @return new SyndEntry
     */
    protected static SyndEntry createSyndEntry(String url, String title, SyndContent description, Date published) {
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(title);
        url = AbcConfig.getAbsoluteUrl() + url;
        entry.setLink(url);
        entry.setUri(url);
        if (published != null)
            entry.setPublishedDate(published);
        if (description != null)
            entry.setDescription(description);
        return entry;
    }

    /**
     * Sets item owner as entry's author.
     * @param item item
     * @param entry entry
     */
    protected static void setAuthor(Item item, SyndEntry entry) {
        Persistence persistence = PersistenceFactory.getPersistence();
        User author = (User) persistence.findById(new User(item.getOwner()));
        entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
    }

    /**
     * Create SyndEntry from blog story.
     * @return SyndEntry with link to selected story.
     */
    private static SyndEntry createStoryEntry(Relation relation, User author) {
        Item item = (Item) relation.getChild();
        Document document = item.getData();

        SyndContent description = null;
        Node node = document.selectSingleNode("/data/perex");
        if (node != null) {
            description = new SyndContentImpl();
            description.setType("text/html");
            String text = node.getText();
            text = Tools.limit(text, 500, "...");
            description.setValue(text);
        }

        SyndEntry entry = createSyndEntry(Tools.getUrlForBlogStory(relation), item.getTitle(), description, item.getCreated());
        entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
        return entry;
    }

    protected static SyndEntry createQuestionEntry(Relation relation) {
        DiscussionHeader diz = Tools.analyzeDiscussion(relation);
        String url = Tools.getUrlForDiscussion(relation);
        String title = diz.getTitle() + ", odpovědí: " + diz.getResponseCount();

        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        String question = Tools.xpath(diz.getDiscussion(), "data/text");
        question = Tools.removeTags(question);
        question = Tools.limit(question, 500, "...");
        description.setValue(question);

        return createSyndEntry(url, title, description, diz.getUpdated());
    }

    /**
     * Creates an RSS entry for specified article.
     * @param relation Relation of the article
     * @return RSS entry object
     */
    protected static SyndEntry createArticleEntry(Relation relation) {
        Item item = (Item) relation.getChild();
        Set authors = item.getProperty(Constants.PROPERTY_AUTHOR);
        String firstId = (String) authors.iterator().next();
        Relation authorRelation = (Relation) Tools.sync(new Relation(Misc.parseInt(firstId, 0)));
        Item author = (Item) authorRelation.getChild();
        Document document = item.getData();

        SyndContent description = new SyndContentImpl();
        description.setType("text/plain");
        Node node = document.selectSingleNode("/data/perex");
        description.setValue(node.getText());

        SyndEntry entry = createSyndEntry(relation.getUrl(), item.getTitle(), description, item.getCreated());
        entry.setAuthor(Tools.childName(author));
        return entry;
    }

    /**
     * Creates writer aware of correct encoding.
     * @param path name of file to be created
     * @return writer in UTF-8 encoding
     * @throws IOException
     */
    private static Writer getWriter(String path) throws IOException {
//        return new FileWriter(path);
        return new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        fileArticles = prefs.get(PREF_ARTICLES, null);
        fileBazaar = prefs.get(PREF_BAZAAR, null);
        fileBlog = prefs.get(PREF_BLOG, null);
        fileBlogDigest = prefs.get(PREF_BLOG_DIGEST, null);
        fileDictionary = prefs.get(PREF_DICTIONARY, null);
        fileDiscussions = prefs.get(PREF_DISCUSSIONS, null);
        fileDrivers = prefs.get(PREF_DRIVERS, null);
        fileFaq = prefs.get(PREF_FAQ, null);
        fileHardware = prefs.get(PREF_HARDWARE, null);
        fileNews = prefs.get(PREF_NEWS, null);
        filePersonalities = prefs.get(PREF_PERSONALITIES, null);
        filePolls = prefs.get(PREF_POLLS, null);
        fileDesktops = prefs.get(PREF_DESKTOPS, null);
        fileSoftware = prefs.get(PREF_SOFTWARE, null);
        dirBlogs = prefs.get(PREF_BLOGS, null);
        fileTrafika = prefs.get(PREF_TRAFIKA, null);
        newsWordLimit = prefs.getInt(PREF_NEWS_WORD_LIMIT, 10);

        try {
            Preferences subprefs = prefs.node(PREF_FORUMS);
            String[] keys = subprefs.keys();
            filesForums = new HashMap<Integer, String>(keys.length);
            
            for (int i = 0; i < keys.length; i++) {
                Integer key = new Integer(keys[i]);
                filesForums.put(key, subprefs.get(keys[i], null));
            }
            
            subprefs = prefs.node(PREF_ARTICLE_SERIES);
            keys = subprefs.keys();
            filesArticleSeries = new HashMap<Integer, String>(keys.length);

            for (int i = 0; i < keys.length; i++) {
                Integer key = new Integer(keys[i]);
                filesArticleSeries.put(key, subprefs.get(keys[i], null));
            }
        } catch(BackingStoreException e) {
            throw new ConfigurationException(e.getMessage(), e.getCause());
        }
        
        findSubportalForums();
    }

    /**
     * @return url for fresh articles feed
     */
    public static String getArticlesFeedUrl() {
        return "/" + fileArticles;
    }

    /**
     * @return url for fresh ads feed
     */
    public static String getBazaarFeedUrl() {
        return "/" + fileBazaar;
    }

    /**
     * @return url for fresh stories from given blog feed
     */
    public static String getBlogFeedUrl(Category blog) {
        return "/" + dirBlogs + blog.getSubType() + ".rss";
    }

    /**
     * @return url for fresh blog stories feed
     */
    public static String getBlogsFeedUrl() {
        return "/" + fileBlog;
    }

    /**
     * @return url for selected blog stories feed
     */
    public static String getBlogDigestFeedUrl() {
        return "/" + fileBlogDigest;
    }

    /**
     * @return url for fresh dictionaries items feed
     */
    public static String getDictionariesFeedUrl() {
        return "/" + fileDictionary;
    }

    /**
     * @return url for fresh drivers feed
     */
    public static String getDriversFeedUrl() {
        return "/" + fileDrivers;
    }

    /**
     * @return url for fresh FAQ items feed
     */
    public static String getFaqFeedUrl() {
        return "/" + fileFaq;
    }

    /**
     * @return url for fresh questions in forum feed
     */
    public static String getForumFeedUrl() {
        return "/" + fileDiscussions;
    }

    /**
     * @return url for fresh hardware items feed
     */
    public static String getHardwareFeedUrl() {
        return "/" + fileHardware;
    }

    /**
     * @return url for fresh news feed
     */
    public static String getNewsFeedUrl() {
        return "/" + fileNews;
    }

    /**
     * @return url for fresh personalities items feed
     */
    public static String getPersonalitiesFeedUrl() {
        return "/" + filePersonalities;
    }

    /**
     * @return url for fresh polls feed
     */
    public static String getPollsFeedUrl() {
        return "/" + filePolls;
    }

    /**
     * @param relationId relation id of the series
     * @return url for fresh articles in given feed or null, if it does not have its feed
     */
    public static String getSeriesFeedUrl(int relationId) {
        String s = filesArticleSeries.get(relationId);
        return (s != null) ? "/" + s : null;
    }
    
    /**
     * @param relationId relation id of the forum
     * @return The URL of the associated feed or null
     */
    public static String getForumFeedUrl(int relationId) {
        String s = filesForums.get(relationId);
        return (s != null) ? "/" + s : null;
    }

    /**
     * @return url for fresh software items feed
     */
    public static String getSoftwareFeedUrl() {
        return "/" + fileSoftware;
    }

    /**
     * @return url for fresh screenshots feed
     */
    public static String getScreenshotsFeedUrl() {
        return "/" + fileDesktops;
    }

    public static void main(String[] args) {
        if (args==null || args.length==0) {
            System.out.println("Enter one of all, hardware, software, articles, blog, blogs, drivers, news, faq, " +
                               "polls, bazaar, dictionary, desktops, whoiswho or forum as an argument!");
            System.exit(1);
        }

        Arrays.sort(args);
        boolean all = Arrays.binarySearch(args, "all") >= 0;
        if (all || Arrays.binarySearch(args, "articles") >= 0)
            updateArticles();
        if (all || Arrays.binarySearch(args, "blog") >= 0)
            updateBlog(null);
        if (all || Arrays.binarySearch(args, "bazaar") >= 0)
            updateBazaar();
        if (all || Arrays.binarySearch(args, "blogs") >= 0) {
            Persistence persistence = PersistenceFactory.getPersistence();
            Relation top = (Relation) persistence.findById(new Relation(Constants.REL_BLOGS));
            List blogs = top.getChild().getChildren();
            for (Iterator iter = blogs.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Category blog = (Category) persistence.findById(relation.getChild());
                updateBlog(blog);
            }
        }
        if (all || Arrays.binarySearch(args, "desktops") >= 0)
            updateDesktops();
        if (all || Arrays.binarySearch(args, "dictionary") >= 0)
            updateDictionary();
        if (all || Arrays.binarySearch(args, "drivers") >= 0)
            updateDrivers();
        if (all || Arrays.binarySearch(args, "faq") >= 0)
            updateFAQ();
        if (all || Arrays.binarySearch(args, "forum") >= 0)
            updateForum();
        if (all || Arrays.binarySearch(args, "hardware") >= 0)
            updateHardware();
        if (all || Arrays.binarySearch(args, "news") >= 0)
            updateNews();
        if (all || Arrays.binarySearch(args, "polls") >= 0)
            updatePolls();
        if (all || Arrays.binarySearch(args, "software") >= 0)
            updateSoftware();
        if (all || Arrays.binarySearch(args, "whoiswho") >= 0)
            updatePersonalities();
    }
}
