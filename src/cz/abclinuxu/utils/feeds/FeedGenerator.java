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
import cz.abclinuxu.utils.news.NewsCategories;
import cz.abclinuxu.persistence.extra.*;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.scheduler.VariableFetcher;

import java.util.prefs.Preferences;
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
    static final String PREF_NEWS_WORD_LIMIT = "news.word.limit";

    static String fileDiscussions, fileArticles, fileDrivers, fileHardware, fileBlog, dirBlogs, fileBlogDigest;
    static String fileNews, fileFaq, filePolls, fileTrafika, fileSoftware;
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
            feed.setLink("http://www.abclinuxu.cz/diskuse.jsp");
            feed.setUri("http://www.abclinuxu.cz/diskuse.jsp");
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
                title = title.concat(", odpovìdí: " + diz.getResponseCount());
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
            Persistence persistence = PersistenceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setEncoding("UTF-8");
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - databáze ovladaèù");
            feed.setLink("http://www.abclinuxu.cz/ovladace");
            feed.setUri("http://www.abclinuxu.cz/ovladace");
            feed.setDescription("Seznam èerstvých záznamù do databáze linuxových ovladaèù na portálu www.abclinuxu.cz");
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
                entry.setLink("http://www.abclinuxu.cz" + found.getUrl());
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
     * Generates RSS feed for hardware database
     */
    public static void updateHardware() {
        try {
            Persistence persistence = PersistenceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - databáze hardwaru");
            feed.setLink("http://www.abclinuxu.cz/hardware");
            feed.setUri("http://www.abclinuxu.cz/hardware");
            feed.setDescription("Seznam èerstvých záznamù do databáze hardwarových poznatkù na portálu www.abclinuxu.cz");
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
                url = "http://www.abclinuxu.cz" + url;
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
            Persistence persistence = PersistenceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - katalog softwaru");
            feed.setLink("http://www.abclinuxu.cz/software");
            feed.setUri("http://www.abclinuxu.cz/software");
            feed.setDescription("Seznam èerstvých záznamù do softwarového katalogu na portálu www.abclinuxu.cz");
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
                url = "http://www.abclinuxu.cz" + url;
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
     * Generates RSS and Trafika feed for articles
     */
    public static void updateArticles() {
        try {
            SQLTool sqlTool = SQLTool.getInstance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - aktuální èlánky");
            feed.setLink("http://www.abclinuxu.cz/clanky");
            feed.setUri("http://www.abclinuxu.cz/clanky");
            feed.setDescription("Seznam èerstvých èlánkù na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;
            SyndContent description;

            Map defaultSizes = VariableFetcher.getInstance().getDefaultSizes();
            int countArticles = (Integer) defaultSizes.get(VariableFetcher.KEY_ARTICLE);
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, countArticles)};
            List articles = sqlTool.findIndexArticlesRelations(qualifiers);
            Tools.syncList(articles);
            for (Iterator iter = articles.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) found.getChild();
                Document document = item.getData();
                Node node = document.selectSingleNode("/data/author");
                User author = Tools.createUser(node.getText());

                entry = new SyndEntryImpl();
                entry.setLink("http://www.abclinuxu.cz" + found.getUrl());
                entry.setTitle(Tools.xpath(item, "data/name"));
                entry.setPublishedDate(item.getCreated());
                entry.setAuthor((author.getNick() != null) ? author.getNick() : author.getName());
                description = new SyndContentImpl();
                description.setType("text/plain");
                node = document.selectSingleNode("/data/perex");
                description.setValue(node.getText());
                entry.setDescription(description);
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
    }

    /**
     * Generates RSS feed for selected and all blogs
     */
    public static void updateBlog(Category blog)  {
        try {
            SQLTool sqlTool = SQLTool.getInstance();
            Persistence persistence = PersistenceFactory.getPersistance();

            if (blog!=null) {
                User author = (User) persistence.findById(new User(blog.getOwner()));
                SyndFeed feed = new SyndFeedImpl();
                feed.setFeedType(TYPE_RSS_1_0);
                feed.setEncoding("UTF-8");
                feed.setTitle(Tools.limit(Tools.xpath(blog, "//custom/title"), 40, "..."));
                feed.setLink("http://www.abclinuxu.cz/blog/"+blog.getSubType()+"/");
                feed.setUri("http://www.abclinuxu.cz/blog/"+blog.getSubType()+"/");
                feed.setDescription("Seznam èerstvých zápisù u¾ivatele "+author.getName());
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
            feed.setDescription("Seznam èerstvých zápisù u¾ivatelù www.abclinuxu.cz");
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
                User author = (User) persistence.findById(new User(blog.getOwner()));
                entry = getStorySyndicate(blog, found, author);
                entries.add(entry);
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
            Persistence persistence = PersistenceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - výbìr z blogù");
            feed.setLink("http://www.abclinuxu.cz/blog/");
            feed.setUri("http://www.abclinuxu.cz/blog/");
            feed.setDescription("Seznam peèlivì vybraných zápisù u¾ivatelù www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            HashSet values = new HashSet();
            values.add("yes");
            Map filters = new HashMap();
            filters.put(Constants.PROPERTY_BLOG_DIGEST, values);
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
            log.error("Chyba pøi generování RSS pro digest blogu", e);
        }
    }

    /**
     * Generates RSS for news.
     */
    public static void updateNews() {
        try {
            Persistence persistence = PersistenceFactory.getPersistance();
            String title;

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - èerstvé zprávièky");
            feed.setLink("http://www.abclinuxu.cz/zpravicky");
            feed.setUri("http://www.abclinuxu.cz/zpravicky");
            feed.setDescription("Seznam èerstvých zprávièek na portálu www.abclinuxu.cz");
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
                entry.setLink("http://www.abclinuxu.cz" + found.getUrl());
                entry.setTitle(title);
                description = new SyndContentImpl();
                description.setType("text/plain");
                description.setValue(Tools.limitWords(withoutTags, newsWordLimit, " ..."));
                entry.setDescription(description);
                entry.setPublishedDate(item.getUpdated());
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
            Persistence persistence = PersistenceFactory.getPersistance();
            String title, content, withoutTags;

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setEncoding("UTF-8");
            feed.setTitle("abclinuxu - èasto kladené otázky");
            feed.setLink("http://www.abclinuxu.cz/faq");
            feed.setUri("http://www.abclinuxu.cz/faq");
            feed.setDescription("Seznam aktualizovaných otázek s odpovìïmi na portálu www.abclinuxu.cz");
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
                entry.setLink("http://www.abclinuxu.cz" + found.getUrl());
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
                entry.setLink("http://www.abclinuxu.cz" + url);

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
        String url = found.getUrl();
        if (url == null)
            url = "http://www.abclinuxu.cz" + Tools.getUrlForBlogStory(blog.getSubType(), item.getUpdated(), found.getId());
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
        fileDiscussions = prefs.get(PREF_DISCUSSIONS, null);
        fileArticles = prefs.get(PREF_ARTICLES, null);
        fileDrivers = prefs.get(PREF_DRIVERS, null);
        fileHardware = prefs.get(PREF_HARDWARE, null);
        fileSoftware = prefs.get(PREF_SOFTWARE, null);
        fileBlog = prefs.get(PREF_BLOG, null);
        fileBlogDigest = prefs.get(PREF_BLOG_DIGEST, null);
        dirBlogs = prefs.get(PREF_BLOGS, null);
        fileTrafika = prefs.get(PREF_TRAFIKA, null);
        fileNews = prefs.get(PREF_NEWS, null);
        fileFaq = prefs.get(PREF_FAQ, null);
        filePolls = prefs.get(PREF_POLLS, null);
        newsWordLimit = prefs.getInt(PREF_NEWS_WORD_LIMIT, 10);
    }

    public static void main(String[] args) {
        if (args==null || args.length==0) {
            System.out.println("Enter one of hardware, software, articles, blog, blogs, drivers, news, faq, " +
                               "polls or forum as an argument!");
            System.exit(1);
        }
        Arrays.sort(args);
        if (Arrays.binarySearch(args, "hardware")>=0)
            updateHardware();
        if (Arrays.binarySearch(args, "software")>=0)
            updateSoftware();
        if (Arrays.binarySearch(args, "articles")>=0)
            updateArticles();
        if (Arrays.binarySearch(args, "drivers")>=0)
            updateDrivers();
        if (Arrays.binarySearch(args, "forum")>=0)
            updateForum();
        if (Arrays.binarySearch(args, "blog")>=0)
            updateBlog(null);
        if (Arrays.binarySearch(args, "news")>=0)
            updateNews();
        if (Arrays.binarySearch(args, "faq")>=0)
            updateFAQ();
        if (Arrays.binarySearch(args, "polls")>=0)
            updatePolls();
        if (Arrays.binarySearch(args, "blogs")>=0) {
            Persistence persistence = PersistenceFactory.getPersistance();
            Relation top = (Relation) persistence.findById(new Relation(Constants.REL_BLOGS));
            List blogs = top.getChild().getChildren();
            for (Iterator iter = blogs.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Category blog = (Category) persistence.findById(relation.getChild());
                updateBlog(blog);
            }
        }
    }
}
