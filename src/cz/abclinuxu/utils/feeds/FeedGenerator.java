package cz.abclinuxu.utils.feeds;

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;

import java.util.prefs.Preferences;
import java.util.*;
import java.io.*;

import com.sun.syndication.feed.synd.*;
import com.sun.syndication.io.SyndFeedOutput;
import org.apache.log4j.Logger;
import org.dom4j.Node;
import org.dom4j.Document;

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
    static final String PREF_BLOG = "blog";
    static final String PREF_BLOGS = "blogs";
    static final String PREF_TRAFIKA = "trafika";

    static String fileDiscussions, fileArticles, fileDrivers, fileHardware, fileBlog, dirBlogs, fileTrafika;
    static int feedLength = 10;
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
            feed.setTitle("abclinuxu - diskusní fórum");
            feed.setLink("http://www.abclinuxu.cz/diskuse.jsp");
            feed.setDescription("Seznam aktuálních diskusí na fóru portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);

            SyndEntry entry;
            SyndContent description;
            String question, title;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
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
            Persistance persistance = PersistanceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setEncoding("ISO-8859-2");
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - databáze ovladaèù");
            feed.setLink("http://www.abclinuxu.cz/drivers/dir/318");
            feed.setDescription("Seznam èerstvých záznamù do databáze linuxových ovladaèù na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List list = SQLTool.getInstance().findItemRelationsWithType(Item.DRIVER, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) persistance.findById(found.getChild());
                User author = (User) persistance.findById(new User(item.getOwner()));

                entry = new SyndEntryImpl();
                entry.setLink("http://www.abclinuxu.cz/drivers/show/" + found.getId());
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
            Persistance persistance = PersistanceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - databáze hardwaru");
            feed.setLink("http://www.abclinuxu.cz");
            feed.setDescription("Seznam èerstvých záznamù do databáze hardwarových poznatkù na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0,feedLength)};
            List list = SQLTool.getInstance().findRecordRelationsWithType(Record.HARDWARE, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                Item item = (Item) persistance.findById(found.getParent());
                Record record = (Record) persistance.findById(found.getChild());
                User author = (User) persistance.findById(new User(record.getOwner()));

                entry = new SyndEntryImpl();
                entry.setLink("http://www.abclinuxu.cz/hardware/show/"+found.getId());
                entry.setTitle(Tools.xpath(item,"data/name"));
                entry.setPublishedDate(record.getUpdated());
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
     * Generates RSS and Trafika feed for articles
     */
    public static void updateArticles() {
        try {
            Persistance persistance = PersistanceFactory.getPersistance();

            SyndFeed feed = new SyndFeedImpl();
            feed.setFeedType(TYPE_RSS_1_0);
            feed.setTitle("abclinuxu - aktuální èlánky");
            feed.setLink("http://www.abclinuxu.cz");
            feed.setDescription("Seznam èerstvých èlánkù na portálu www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;
            SyndContent description;

            Category actual = (Category) persistance.findById(new Category(Constants.CAT_ACTUAL_ARTICLES));
            List children = actual.getChildren();
            Tools.syncList(children);
            List list = Sorters2.byDate(children, Sorters2.DESCENDING);
            for (int i = 0; i < 6 && i < list.size(); i++) {
                Relation found = (Relation) list.get(i);
                Item item = (Item) found.getChild();
                Document document = item.getData();
                Node node = document.selectSingleNode("/data/author");
                User author = Tools.createUser(node.getText());

                entry = new SyndEntryImpl();
                entry.setLink("http://www.abclinuxu.cz/clanky/show/" + found.getId());
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
            writer = getWriter(path);
            writer.write(Constants.isoFormat.format(new Date()));
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
            Persistance persistance = PersistanceFactory.getPersistance();

            if (blog!=null) {
                User author = (User) persistance.findById(new User(blog.getOwner()));
                SyndFeed feed = new SyndFeedImpl();
                feed.setFeedType(TYPE_RSS_1_0);
                feed.setTitle(Tools.limit(Tools.xpath(blog, "//custom/title"), 40, "..."));
                feed.setLink("http://www.abclinuxu.cz/blog/"+blog.getSubType()+"/");
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
                for (Iterator iter = stories.iterator(); iter.hasNext();) {
                    Relation found = (Relation) iter.next();
                    entry = getStorySyndicate(persistance, blog, found, author);
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
            feed.setDescription("Seznam èerstvých zápisù u¾ivatelù www.abclinuxu.cz");
            List entries = new ArrayList();
            feed.setEntries(entries);
            SyndEntry entry;

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, feedLength)};
            List stories = sqlTool.findItemRelationsWithType(Item.BLOG, qualifiers);
            for (Iterator iter = stories.iterator(); iter.hasNext();) {
                Relation found = (Relation) iter.next();
                blog = (Category) persistance.findById(found.getParent());
                User author = (User) persistance.findById(new User(blog.getOwner()));
                entry = getStorySyndicate(persistance, blog, found, author);
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
     * Create SyndEntry from blog story.
     * @return SyndEntry with link to selected story.
     */
    private static SyndEntry getStorySyndicate(Persistance persistance, Category blog, Relation found, User author) {
        SyndEntry entry;
        SyndContent description;
        Item item = (Item) persistance.findById(found.getChild());
        Document document = item.getData();

        entry = new SyndEntryImpl();
        entry.setLink("http://www.abclinuxu.cz"+Tools.getUrlForBlogStory(blog.getSubType(), item.getUpdated(), found.getId()));
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
        return new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        fileDiscussions = prefs.get(PREF_DISCUSSIONS, null);
        fileArticles = prefs.get(PREF_ARTICLES, null);
        fileDrivers = prefs.get(PREF_DRIVERS, null);
        fileHardware = prefs.get(PREF_HARDWARE, null);
        fileBlog = prefs.get(PREF_BLOG, null);
        dirBlogs = prefs.get(PREF_BLOGS, null);
        fileTrafika = prefs.get(PREF_TRAFIKA, null);
    }

    public static void main(String[] args) {
        if (args==null || args.length==0) {
            System.out.println("Enter one of hardware, articles, blog, blogs, drivers or forum as an argument!");
            System.exit(1);
        }
        Arrays.sort(args);
        if (Arrays.binarySearch(args, "hardware")>=0)
            updateHardware();
        if (Arrays.binarySearch(args, "articles")>=0)
            updateArticles();
        if (Arrays.binarySearch(args, "drivers")>=0)
            updateDrivers();
        if (Arrays.binarySearch(args, "forum")>=0)
            updateForum();
        if (Arrays.binarySearch(args, "blog")>=0)
            updateBlog(null);
        if (Arrays.binarySearch(args, "blogs")>=0) {
            Persistance persistance = PersistanceFactory.getPersistance();
            Relation top = (Relation) persistance.findById(new Relation(Constants.REL_BLOGS));
            List blogs = top.getChild().getChildren();
            for (Iterator iter = blogs.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Category blog = (Category) persistance.findById(relation.getChild());
                updateBlog(blog);
            }
        }
    }
}
