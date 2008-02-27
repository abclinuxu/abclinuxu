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
import cz.abclinuxu.data.view.NewsCategories;
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
 */
public class FeedGenerator implements Configurable {
    private static Logger log = Logger.getLogger(FeedGenerator.class);

    public static final String TYPE_RSS_1_0 = "rss_1.0";

    static final String PREF_DISCUSSIONS = "diskuse";
    static final String PREF_ARTICLES = "clanky";
    static final String PREF_DRIVERS = "ovladace";
    static final String PREF_HARDWARE = "hardware";
    static final String PREF_SOFTWARE = "software";
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

    static String fileDiscussions, fileArticles, fileDrivers, fileHardware, fileBlog, dirBlogs, fileBlogDigest;
    static String fileNews, fileFaq, filePolls, fileTrafika, fileSoftware, fileBazaar, fileDictionary;
    static String fileScreenshots, filePersonalities;
    static Map<Integer, String> filesArticleSeries;
    static int feedLength = 10, highFrequencyFeedLength = 25, newsWordLimit;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new FeedGenerator());
    }

    /**
     * Generates RSS feed for discussion forum
     */
    public static void updateForum() {
        try {
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - diskusní fórum");
            feed.setLink("http://www.abclinuxu.cz/poradna");
            feed.setUri("http://www.abclinuxu.cz/poradna");
            feed.setDescription("Seznam aktuálních diskusí na fóru portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);

            SyndEntry entry;
            SyndContent description;
            String question, title;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)};
            List list = SQLTool.getInstance().findDiscussionRelations(qualifiers);
            Tools.syncList(list);
            List discussions = new Tools().analyzeDiscussions(list);
            for (Iterator iter = discussions.iterator(); iter.hasNext();) {
                DiscussionHeader diz = (DiscussionHeader) iter.next();
                entry = new SyndEntryImpl();
                entry.setLink("http://www.abclinuxu.cz/forum/show/" + diz.getRelationId());
                title = Tools.xpath(diz.getDiscussion(), "data/title");
                title = title.concat(", odpovědí: " + diz.getResponseCount());
                entry.setTitle(title);
                entry.setPublishedDate(diz.getUpdated());
                description = new SyndContentImpl();
                description.setType("text/plain");
                question = Tools.xpath(diz.getDiscussion(), "data/text");
                question = Tools.removeTags(question);
                question = Tools.limit(question, 500, "...");
                description.setValue(question);
                entry.setDescription(description);
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileDiscussions);
            Writer writer = getWriter(path);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, writer);
            writer.close();
        } catch (Exception e) {
            log.error("Chyba pri generovani RSS pro ovladace", e);
        }
    }

    /**
     * Generates RSS feed for driver database
     */
    public static void updateDrivers() {
        try {
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = new SyndFeedImpl();
            feed.setEncoding("UTF-8");
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - databáze ovladačů");
            feed.setLink("http://www.abclinuxu.cz/ovladace");
            feed.setUri("http://www.abclinuxu.cz/ovladace");
            feed.setDescription("Seznam čerstvých záznamů do databáze linuxových ovladačů na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.DRIVER, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                entry.setLink("http://"+AbcConfig.getHostname() + found.getUrl());
                entry.setTitle(Tools.xpath(item, "data/name"));
                entry.setPublishedDate(item.getUpdated());
                entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
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
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = new SyndFeedImpl();
            feed.setEncoding("UTF-8");
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - výkladový slovník");
            feed.setLink("http://www.abclinuxu.cz/slovnik");
            feed.setUri("http://www.abclinuxu.cz/slovnik");
            feed.setDescription("Výkladový slovník na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.DICTIONARY, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                entry.setLink("http://"+AbcConfig.getHostname() + found.getUrl());
                entry.setTitle(Tools.xpath(item, "data/name"));
                entry.setPublishedDate(item.getUpdated());
                entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
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
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = new SyndFeedImpl();
            feed.setEncoding("UTF-8");
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - osobnosti");
            feed.setLink("http://www.abclinuxu.cz/kdo-je");
            feed.setUri("http://www.abclinuxu.cz/kdo-je");
            feed.setDescription("Osobnosti na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.PERSONALITY, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                entry.setLink("http://"+AbcConfig.getHostname() + found.getUrl());
                entry.setTitle(Tools.xpath(item, "data/name"));
                entry.setPublishedDate(item.getUpdated());
                entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
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
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - databáze hardwaru");
            feed.setLink("http://www.abclinuxu.cz/hardware");
            feed.setUri("http://www.abclinuxu.cz/hardware");
            feed.setDescription("Seznam čerstvých záznamů do databáze hardwarových poznatků na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.HARDWARE, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                String url = found.getUrl();
                if (url==null)
                    url = "/hardware/show/" + found.getId();
                url = "http://"+AbcConfig.getHostname() + url;
                entry.setLink(url);
                entry.setTitle(Tools.xpath(item,"data/name"));
                entry.setPublishedDate(item.getUpdated());
                entry.setAuthor((author.getNick()!=null) ? author.getNick() : author.getName());
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
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - katalog softwaru");
            feed.setLink("http://www.abclinuxu.cz/software");
            feed.setUri("http://www.abclinuxu.cz/software");
            feed.setDescription("Seznam čerstvých záznamů do softwarového katalogu na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;
            SyndContent description;
            Node node;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.SOFTWARE, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                String url = found.getUrl();
                url = "http://"+AbcConfig.getHostname() + url;
                entry.setLink(url);
                entry.setTitle(Tools.xpath(item,"data/name"));
                entry.setPublishedDate(item.getUpdated());
                entry.setAuthor((author.getNick()!=null) ? author.getNick() : author.getName());
                node = item.getData().selectSingleNode("/data/intro");
                if (node != null) {
                    description = new SyndContentImpl();
                    description.setType("text/plain");
                    description.setValue(node.getText());
                    entry.setDescription(description);
                }
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
     * Generates RSS feed for screenshots
     */
    public static void updateScreenshots() {
        try {
            Persistence persistence = PersistenceFactory.getPersistence();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - desktopy");
            feed.setLink("http://www.abclinuxu.cz/desktopy");
            feed.setUri("http://www.abclinuxu.cz/desktopy");
            feed.setDescription("Seznam čerstvých desktopů na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;
            SyndContent description;
            Node node;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.SCREENSHOT, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                String url = found.getUrl();
                url = "http://"+AbcConfig.getHostname() + url;
                entry.setLink(url);
                entry.setTitle(Tools.xpath(item,"data/title"));
                entry.setPublishedDate(item.getCreated());
                entry.setAuthor((author.getNick()!=null) ? author.getNick() : author.getName());
                entries.add(entry);
            }

            String path = AbcConfig.calculateDeployedPath(fileScreenshots);
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
                SyndFeed feed = new SyndFeedImpl();
                GenericDataObject series;
                String url;

                Tools.sync(relation);
                series = (GenericDataObject) relation.getChild();
                url = "http://" + AbcConfig.getHostname() + relation.getUrl();

                feed.setFeedType(TYPE_RSS_1_0);
                feed.setEncoding("UTF-8");
                feed.setUri(url);
                feed.setLink(url);
                feed.setTitle("abclinuxu - " + Tools.childName(series));

                if (series instanceof Category)
                    createSectionEntries((Category) series, feed, feedLength);
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
    public static void createSectionEntries(Category cat, SyndFeed feed, int maxArticles) {
        Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, maxArticles)};
        List<Relation> articles = SQLTool.getInstance().findArticleRelations(qualifiers, cat.getId());

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        feed.setDescription(Tools.xpath(cat, "/data/note"));
        feed.setEntries(entries);

        SyndEntry entry;
        for (Relation r : articles) {
            Tools.sync(r);
            entry = createArticleEntry(r);
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
        articlesElements = articlesElements.subList(0, Math.min(maxArticles, articlesElements.size()));
        articlesElements = new ArrayList<Element>(articlesElements);
        Collections.reverse(articlesElements);

        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        feed.setDescription(Tools.xpath(serie, "/data/description"));
        feed.setEntries(entries);

        SyndEntry entry;
        for (Iterator iter = articlesElements.iterator(); iter.hasNext();) {
            Element article = (Element) iter.next();
            int rid = Misc.parseInt(article.getText(), 0);
            Relation r = new Relation(rid);
            Tools.sync(r);
            entry = createArticleEntry(r);
            entries.add(entry);
        }
    }

    /**
     * Creates an RSS entry for specified article.
     * @param r Relation of the article
     * @return RSS entry object
     */
    protected static SyndEntry createArticleEntry(Relation r) {
        SyndContent description;
        Item item = (Item) r.getChild();
        Set authors = item.getProperty(Constants.PROPERTY_AUTHOR);
        String firstId = (String) authors.iterator().next();
        Relation authorRelation = (Relation) Tools.sync(new Relation(Misc.parseInt(firstId, 0)));
        Item author = (Item) authorRelation.getChild();
        Document document = item.getData();

        SyndEntry entry = new SyndEntryImpl();
        entry.setLink("http://"+AbcConfig.getHostname() + r.getUrl());
        entry.setTitle(Tools.xpath(item, "data/name"));
        entry.setPublishedDate(item.getCreated());
        entry.setAuthor(Tools.childName(author));
        description = new SyndContentImpl();
        description.setType("text/plain");
        Node node = document.selectSingleNode("/data/perex");
        description.setValue(node.getText());
        entry.setDescription(description);

        return entry;
    }

    /**
     * Generates RSS and Trafika feed for articles
     */
    public static void updateArticles() {
        try {
            SQLTool sqlTool = SQLTool.getInstance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - aktuální články");
            feed.setLink("http://www.abclinuxu.cz/clanky");
            feed.setUri("http://www.abclinuxu.cz/clanky");
            feed.setDescription("Seznam čerstvých článků na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Map defaultSizes = VariableFetcher.getInstance().getDefaultSizes();
            int countArticles = (Integer) defaultSizes.get(VariableFetcher.KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, countArticles)};
            List articles = sqlTool.findIndexArticlesRelations(qualifiers);
            Tools.syncList(articles);
            for (Iterator iter = articles.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
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
                SyndFeed feed = new SyndFeedImpl();
                feed.setFeedType(TYPE_RSS_1_0);
                feed.setEncoding("UTF-8");
                feed.setTitle(Tools.limit(Tools.xpath(blog, "//custom/title"), 40, "..."));
                feed.setLink("http://www.abclinuxu.cz/blog/"+blog.getSubType()+"/");
                feed.setUri("http://www.abclinuxu.cz/blog/"+blog.getSubType()+"/");
                feed.setDescription("Seznam čerstvých zápisů uživatele "+author.getName());
                List entries = new ArrayList();
                feed.setEntries(entries);
                SyndEntry entry;

                List qualifiers = new ArrayList();
                qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL, new Integer(blog.getOwner())));
                qualifiers.add(Qualifier.SORT_BY_CREATED);
                qualifiers.add(Qualifier.ORDER_DESCENDING);
                qualifiers.add(new LimitQualifier(0, feedLength));
                Qualifier[] qa = new Qualifier[qualifiers.size()];
                List stories = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
                Tools.syncList(stories);
                for (Iterator iter = stories.iterator(); iter.hasNext();) {
                    Relation found = (Relation) iter.next();
                    entry = getStorySyndicate(blog, found, author);
                    entries.add(entry);
                }

                String path = AbcConfig.calculateDeployedPath(dirBlogs + blog.getSubType() + ".rss");
                Writer writer = getWriter(path);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feed, writer);
                writer.close();
            }

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - blogy");
            feed.setLink("http://www.abclinuxu.cz/blog/");
            feed.setUri("http://www.abclinuxu.cz/blog/");
            feed.setDescription("Seznam čerstvých zápisů uživatelů www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)};
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
                    entry = getStorySyndicate(blog, found, author);
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

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - výběr z blogů");
            feed.setLink("http://www.abclinuxu.cz/blog/");
            feed.setUri("http://www.abclinuxu.cz/blog/");
            feed.setDescription("Seznam pečlivě vybraných zápisů uživatelů www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Set<String> values = Collections.singleton("yes");
            Map<String, Set<String>> filters = Collections.singletonMap(Constants.PROPERTY_BLOG_DIGEST, values);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, highFrequencyFeedLength)};
            List stories = sqlTool.findItemRelationsWithTypeWithFilters(Item.BLOG, qualifiers, filters);
            Tools.syncList(stories);
            List blogs = new ArrayList();
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                blogs.add(found.getParent());
            }
            Tools.syncList(blogs);
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Category blog = (Category) persistence.findById(found.getParent());
                User author = (User) persistence.findById(new User(blog.getOwner()));
                entry = getStorySyndicate(blog, found, author);
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
            Persistence persistence = PersistenceFactory.getPersistence();
            String title;

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - čerstvé zprávičky");
            feed.setLink("http://www.abclinuxu.cz/zpravicky");
            feed.setUri("http://www.abclinuxu.cz/zpravicky");
            feed.setDescription("Seznam čerstvých zpráviček na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;
            SyndContent description;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findNewsRelations(qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));

                String content = Tools.xpath(item, "data/content");
                String withoutTags = Tools.removeTags(content);

                Element element = (Element) item.getData().selectSingleNode("/data/title");
                if (element!=null)
                    title = element.getText();
                else
                    title = NewsCategories.get(item.getSubType()).getName();

                entry = new SyndEntryImpl();
                entry.setLink("http://"+AbcConfig.getHostname() + found.getUrl());
                entry.setTitle(title);
                description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue(withoutTags);
//                description.setValue(Tools.limitWords(withoutTags, newsWordLimit, " ..."));
                entry.setDescription(description);
                entry.setPublishedDate(item.getCreated());
                entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
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
            Persistence persistence = PersistenceFactory.getPersistence();
            String title, content, withoutTags;

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - často kladené otázky");
            feed.setLink("http://www.abclinuxu.cz/faq");
            feed.setUri("http://www.abclinuxu.cz/faq");
            feed.setDescription("Seznam aktualizovaných otázek s odpověďmi na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;
            SyndContent description;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.FAQ, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                User author = (User) persistence.findById(new User(item.getOwner()));
                content = Tools.xpath(item, "data/text");
                withoutTags = Tools.removeTags(content);

                Element element = (Element) item.getData().selectSingleNode("/data/title");
                title = element.getText();

                entry = new SyndEntryImpl();
                entry.setLink("http://"+AbcConfig.getHostname() + found.getUrl());
                entry.setTitle(title);
                description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue(Tools.limitWords(withoutTags, newsWordLimit, " ..."));
                entry.setDescription(description);
                entry.setPublishedDate(item.getUpdated());
                entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
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
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - ankety");
            feed.setLink("http://www.abclinuxu.cz/ankety");
            feed.setUri("http://www.abclinuxu.cz/ankety");
            feed.setDescription("Seznam anket na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);

            SyndEntry entry;
            String title;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findStandalonePollRelations(qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                entry = new SyndEntryImpl();
                String url = relation.getUrl();
                if (url==null)
                    url = UrlUtils.PREFIX_POLLS+"/show/"+relation.getId();
                entry.setLink("http://"+AbcConfig.getHostname() + url);

                Poll poll = (Poll) relation.getChild();
                title = Tools.removeTags(poll.getText());
                entry.setTitle(title);
                entry.setPublishedDate(poll.getCreated());
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
            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - bazar");
            feed.setLink("http://www.abclinuxu.cz/bazar");
            feed.setUri("http://www.abclinuxu.cz/bazar");
            feed.setDescription("Seznam inzerátů z bazaru na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);

            SyndEntry entry;
            String title;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.BAZAAR, qualifiers);
            Tools.syncList(list);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                entry = new SyndEntryImpl();
                String url = relation.getUrl();
                if (url == null)
                    url = UrlUtils.PREFIX_BAZAAR + "/show/" + relation.getId();
                entry.setLink("http://"+AbcConfig.getHostname() + url);

                Item item = (Item) relation.getChild();
                title = Tools.removeTags(Tools.childName(relation));
                entry.setTitle(title);
                entry.setPublishedDate(item.getCreated());
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
     * Create SyndEntry from blog story.
     * @return SyndEntry with link to selected story.
     */
    private static SyndEntry getStorySyndicate(Category blog, Relation found, User author) {
        SyndEntry entry;
        SyndContent description;
        Item item = (Item) found.getChild();
        Document document = item.getData();

        entry = new SyndEntryImpl();

        String url = "http://"+AbcConfig.getHostname() + Tools.getUrlForBlogStory(found);

        entry.setLink(url);
        entry.setTitle(Tools.xpath(item, "data/name"));
        entry.setPublishedDate(item.getCreated());
        entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
        description = new SyndContentImpl();
        description.setType("text/html");
        Node node = document.selectSingleNode("/data/perex");
        if (node!=null) {
            String text = node.getText();
            text = Tools.limit(text, 500, "...");
            description.setValue(text);
            entry.setDescription(description);
        }
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
        fileSoftware = prefs.get(PREF_SOFTWARE, null);
        dirBlogs = prefs.get(PREF_BLOGS, null);
        fileTrafika = prefs.get(PREF_TRAFIKA, null);
        newsWordLimit = prefs.getInt(PREF_NEWS_WORD_LIMIT, 10);

        try {
            Preferences subprefs = prefs.node(PREF_ARTICLE_SERIES);
            String[] keys = subprefs.keys();
            filesArticleSeries = new HashMap<Integer, String>(keys.length);

            for (int i = 0; i < keys.length; i++) {
                Integer key = new Integer(keys[i]);
                filesArticleSeries.put(key, subprefs.get(keys[i], null));
            }
        } catch(BackingStoreException e) {
            throw new ConfigurationException(e.getMessage(), e.getCause());
        }
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
     * @return url for fresh software items feed
     */
    public static String getSoftwareFeedUrl() {
        return "/" + fileSoftware;
    }

    /**
     * @return url for fresh screenshots feed
     */
    public static String getScreenshotsFeedUrl() {
        return "/" + fileScreenshots;
    }

    public static void main(String[] args) {
        if (args==null || args.length==0) {
            System.out.println("Enter one of hardware, software, articles, blog, blogs, drivers, news, faq, " +
                               "polls, bazaar, dictionary, screenshots or forum as an argument!");
            System.exit(1);
        }
        Arrays.sort(args);
        if (Arrays.binarySearch(args, "articles")>=0)
            updateArticles();
        if (Arrays.binarySearch(args, "blog") >= 0)
            updateBlog(null);
        if (Arrays.binarySearch(args, "bazaar") >= 0)
            updateBazaar();
        if (Arrays.binarySearch(args, "blogs") >= 0) {
            Persistence persistence = PersistenceFactory.getPersistence();
            Relation top = (Relation) persistence.findById(new Relation(Constants.REL_BLOGS));
            List blogs = top.getChild().getChildren();
            for (Iterator iter = blogs.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Category blog = (Category) persistence.findById(relation.getChild());
                updateBlog(blog);
            }
        }
        if (Arrays.binarySearch(args, "dictionary") >= 0)
            updateDictionary();
        if (Arrays.binarySearch(args, "drivers")>=0)
            updateDrivers();
        if (Arrays.binarySearch(args, "faq") >= 0)
            updateFAQ();
        if (Arrays.binarySearch(args, "forum")>=0)
            updateForum();
        if (Arrays.binarySearch(args, "hardware") >= 0)
            updateHardware();
        if (Arrays.binarySearch(args, "news")>=0)
            updateNews();
        if (Arrays.binarySearch(args, "polls")>=0)
            updatePolls();
        if (Arrays.binarySearch(args, "software") >= 0)
            updateSoftware();
        if (Arrays.binarySearch(args, "screenshots") >= 0)
            updateScreenshots();
    }
}
