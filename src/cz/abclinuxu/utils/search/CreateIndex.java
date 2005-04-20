/*
 * User: literakl
 * Date: 16.5.2002
 * Time: 15:19:10
 */
package cz.abclinuxu.utils.search;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.persistance.extra.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.freemarker.Tools;
import org.dom4j.Element;
import org.dom4j.Node;
import org.apache.lucene.index.IndexWriter;

import java.util.*;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.FileWriter;

/**
 * This class is responsible for creating and
 * maintaining Lucene's index.
 * todo give score boost to titles and names of objects
 */
public class CreateIndex implements Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateIndex.class);

    public static final String PREF_PATH = "path";
    public static final String PREF_LAST_RUN_NAME = "last.run.file";

    private final String LAST_RUN_FILE = "last_run.txt";

    static String indexPath,lastRunFilename;
    static Persistance persistance;
    static SQLTool sqlTool;
    static HashMap indexed = new HashMap(100000, 0.99f);

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new CreateIndex());
        persistance = PersistanceFactory.getPersistance(EmptyCache.class);
        sqlTool = SQLTool.getInstance();
    }

    public static void main(String[] args) throws Exception {
        String PATH = indexPath;
        if ( args.length>0 ) PATH = args[0];
        log.info("Starting to index data, using directory "+PATH);

        try {
            Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLES));
            Relation hardware = (Relation) persistance.findById(new Relation(Constants.REL_HARDWARE));
            Relation software = (Relation) persistance.findById(new Relation(Constants.REL_SOFTWARE));
            Relation drivers = (Relation) persistance.findById(new Relation(Constants.REL_DRIVERS));
            Relation abc = (Relation) persistance.findById(new Relation(Constants.REL_ABC));
            Relation blogs = (Relation) persistance.findById(new Relation(Constants.REL_BLOGS));
            List forums = sqlTool.findSectionRelationsWithType(Category.FORUM,null);

            long start = System.currentTimeMillis();

            IndexWriter indexWriter = new IndexWriter(PATH, new AbcCzechAnalyzer(), true);

            makeIndexOnArticles(indexWriter, articles.getChild().getChildren(), UrlUtils.PREFIX_CLANKY);
            makeIndexOnNews(indexWriter, UrlUtils.PREFIX_NEWS);
            makeIndexOnDictionary(indexWriter);
            makeIndexOnBlogs(indexWriter, blogs.getChild().getChildren());
            makeIndexOnForums(indexWriter, forums, UrlUtils.PREFIX_FORUM);
            makeIndexOn(indexWriter, hardware, UrlUtils.PREFIX_HARDWARE);
            makeIndexOn(indexWriter, software, UrlUtils.PREFIX_SOFTWARE);
            makeIndexOn(indexWriter, drivers, UrlUtils.PREFIX_DRIVERS);
            makeIndexOn(indexWriter, abc, UrlUtils.PREFIX_CLANKY);
            makeIndexOnBlogs(indexWriter, blogs.getChild().getChildren());

            indexWriter.optimize();
            indexWriter.close();

            long end = System.currentTimeMillis();

            FileWriter fos = new FileWriter(new File(PATH, lastRunFilename));
            fos.write(Constants.czFormat.format(new Date()));
            fos.close();

            String message = "Indexing of "+indexWriter.docCount()+" documents took "+(end-start)/1000+" seconds.";
            log.info(message);
            System.out.println(message);
        } catch (Throwable e) {
            log.error("Indexing failed!",e);
            e.printStackTrace();
        }
    }

    /**
     * Creates index on subtree starting at root.
     * @param root relation, where to start. It must be already synchronized.
     * @param urlPrefix prefix for URL for this subtree
     */
    static void makeIndexOn(IndexWriter indexWriter, Relation root, String urlPrefix) throws Exception {
        List stack = new ArrayList(100);
        stack.add(root);

        Relation relation;
        GenericObject child;
        Item item;
        MyDocument doc;
        boolean indexChildren;
        String url;

        while(stack.size()>0) {
            relation = (Relation) stack.remove(0);
            child = relation.getChild();
            if (hasBeenIndexed(child)) continue;
            child = persistance.findById(child);

            doc = null; indexChildren = true; url = relation.getUrl();
            if (child instanceof Category) {
                doc = indexCategory((Category) child);
                if (url==null)
                    url = urlPrefix+"/dir/"+relation.getId();
            } else if (child instanceof Item) {
                item = (Item) child;
                switch ( item.getType() ) {
                    case Item.ARTICLE:
                        doc = indexArticle(item); break;
                    case Item.DISCUSSION:
                        doc = indexDiscussion(item); break;
                    case Item.MAKE:
                        doc = indexMake(item); break;
                    case Item.DRIVER:
                        doc = indexDriver(item); break;
                }
                indexChildren = false;
                if (url==null)
                    url = urlPrefix+"/show/"+relation.getId();
            }

            if ( doc!=null ) {
                doc.setURL(url);
                doc.setParent(relation.getUpper());
                indexWriter.addDocument(doc.getDocument());
            }

            if (indexChildren)
                for ( Iterator iter = child.getChildren().iterator(); iter.hasNext(); )
                    stack.add(iter.next());
        }
    }

    /**
     * Tests, whether child has been already indexed. If it has not been,
     * its empty clone is stored to mark child as indexed.
     */
    static boolean hasBeenIndexed(GenericObject child) throws Exception {
        if ( indexed.containsKey(child) )
            return true;
        else {
            GenericObject key = (GenericObject) child.getClass().newInstance();
            key.setId(child.getId());
            indexed.put(key, Boolean.TRUE);
            return false;
        }
    }

    /**
     * Indexes content of given forums.
     */
    static void makeIndexOnForums(IndexWriter indexWriter, List forums, String urlPrefix) throws Exception {
        int total, i;
        Relation relation, relation2;
        GenericObject child;
        MyDocument doc;

        for ( Iterator iter = forums.iterator(); iter.hasNext(); ) {
            relation = (Relation) iter.next();
            total = sqlTool.countDiscussionRelationsWithParent(relation.getId());

            for ( i = 0; i<total; ) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
                List discussions = sqlTool.findDiscussionRelationsWithParent(relation.getId(), qualifiers);
                i += discussions.size();

                for ( Iterator iter2 = discussions.iterator(); iter2.hasNext(); ) {
                    relation2 = (Relation) iter2.next();
                    child = relation2.getChild();
                    if ( hasBeenIndexed(child) ) continue;
                    child = persistance.findById(child);
                    doc = indexDiscussion((Item)child);
                    doc.setURL(urlPrefix+"/show/"+relation2.getId());
                    doc.setParent(relation2.getUpper());
                    indexWriter.addDocument(doc.getDocument());
                }
            }
        }
    }

    /**
     * Indexes dictionary.
     */
    static void makeIndexOnDictionary(IndexWriter indexWriter) throws Exception {
        Item child;
        MyDocument doc;
        List relations = sqlTool.findItemRelationsWithType(Item.DICTIONARY, new Qualifier[0]);
        Relation relation;

        for (Iterator iter = relations.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            child = (Item) relation.getChild();
            if ( hasBeenIndexed(child) ) continue;
            child = (Item) persistance.findById(child);
            doc = indexDictionary(child);
            indexWriter.addDocument(doc.getDocument());
        }
    }

    /**
     * Indexes news.
     */
    static void makeIndexOnNews(IndexWriter indexWriter, String urlPrefix) throws Exception {
        int total = sqlTool.countNewsRelations(), i;
        Relation relation;
        GenericObject child;
        MyDocument doc;
        String url;

        for ( i = 0; i<total; ) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
            List data = sqlTool.findNewsRelations(qualifiers);
            i += data.size();

            for ( Iterator iter2 = data.iterator(); iter2.hasNext(); ) {
                relation = (Relation) iter2.next();
                child = relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;

                child = persistance.findById(child);
                doc = indexNews((Item) child);
                url = relation.getUrl();
                if (url==null)
                    url = urlPrefix + "/show/" + relation.getId();
                doc.setURL(url);
                doc.setParent(relation.getUpper());
                indexWriter.addDocument(doc.getDocument());
            }
        }
    }

    /**
     * Indexes article.
     */
    static void makeIndexOnArticles(IndexWriter indexWriter, List sections, String urlPrefix) throws Exception {
        int total, i;
        Relation relation;
        GenericObject child;
        MyDocument doc;
        String url;

        for (Iterator iter = sections.iterator(); iter.hasNext();) {
            Relation section =  (Relation) iter.next();
            int sectionId = section.getId();
            total = sqlTool.countArticleRelations(sectionId);

            for (i = 0; i < total;) {
                Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_ASCENDING, new LimitQualifier(i, 100)};
                List data = sqlTool.findArticleRelations(qualifiers, sectionId);
                i += data.size();

                for (Iterator iter2 = data.iterator(); iter2.hasNext();) {
                    relation = (Relation) iter2.next();
                    child = relation.getChild();
                    if (hasBeenIndexed(child))
                        continue;

                    child = persistance.findById(child);
                    doc = indexArticle((Item) child);
                    url = relation.getUrl();
                    if (url == null)
                        url = urlPrefix + "/show/" + relation.getId();
                    doc.setURL(url);
                    doc.setParent(relation.getUpper());
                    indexWriter.addDocument(doc.getDocument());
                }
            }
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlogs(IndexWriter indexWriter, List blogs) throws Exception {
        Relation relation;
        GenericObject child;
        MyDocument doc;

        for (Iterator iter = blogs.iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            Tools.sync(relation);
            child = relation.getChild();

            doc = indexBlog((Category) child);
            indexWriter.addDocument(doc.getDocument());
            makeIndexOnBlog(indexWriter, (Category) child);
        }
    }

    /**
     * Indexes blogs and stories.
     */
    static void makeIndexOnBlog(IndexWriter indexWriter, Category blog) throws Exception {
        Relation relation;
        GenericObject child;
        MyDocument doc;

        List qualifiers = new ArrayList();
        qualifiers.add(new CompareCondition(Field.OWNER, Operation.EQUAL,new Integer(blog.getOwner())));
        Qualifier[] qa = new Qualifier[qualifiers.size()];
        int total = sqlTool.countItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
        for ( int i = 0; i<total; ) {
            qualifiers.add(Qualifier.SORT_BY_CREATED);
            qualifiers.add(Qualifier.ORDER_ASCENDING);
            qualifiers.add(new LimitQualifier(i, 100));
            List data = sqlTool.findItemRelationsWithType(Item.BLOG, (Qualifier[]) qualifiers.toArray(qa));
            i += data.size();

            for ( Iterator iter2 = data.iterator(); iter2.hasNext(); ) {
                relation = (Relation) iter2.next();
                child = relation.getChild();
                if ( hasBeenIndexed(child) )
                    continue;

                Tools.sync(relation);
                doc = indexStory(relation, blog);
                doc.setParent(relation.getUpper());
                indexWriter.addDocument(doc.getDocument());
            }
        }
    }

    /**
     * Extracts data for indexing from blog.
     */
    static MyDocument indexBlog(Category category) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) category.getData().selectSingleNode("//custom");
        Node node = data.selectSingleNode("page_title");
        title = node.getText();
        sb.append(title);

        node = data.selectSingleNode("title");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.selectSingleNode("intro");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setURL("/blog/"+category.getSubType());
        doc.setType(MyDocument.TYPE_BLOG);
        return doc;
    }

    /**
     * Extracts data for indexing from blog.
     * @param relation story relation
     */
    static MyDocument indexStory(Relation relation, Category category) {
        StringBuffer sb = new StringBuffer();
        String title = null, s;
        Item story = (Item) relation.getChild();

        Element data = story.getData().getRootElement();
        Node node = data.selectSingleNode("name");
        title = node.getText();
        sb.append(title);

        node = data.selectSingleNode("perex");
        if (node!=null) {
            sb.append(" ");
            s = node.getText();
            sb.append(s);
        }

        node = data.selectSingleNode("content");
        sb.append(" ");
        s = node.getText();
        sb.append(s);

        for (Iterator iter = story.getChildren().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( child.getChild() instanceof Item ) {
                Item item = (Item) persistance.findById(child.getChild());
                if (item.getType()!=Item.DISCUSSION)
                    continue;

                MyDocument doc = indexDiscussion(item);
                s = doc.getDocument().get(MyDocument.CONTENT);
                if (s!=null) {
                    sb.append(" ");
                    sb.append(s);
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setURL(Tools.getUrlForBlogStory(category.getSubType(), story.getCreated(), relation.getId()));
        doc.setType(MyDocument.TYPE_BLOG);
        return doc;
    }

    /**
     * Extracts data for indexing from category. Category must be synchronized.
     */
    static MyDocument indexCategory(Category category) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) category.getData().selectSingleNode("data");
        Node node = data.selectSingleNode("name");
        title = node.getText();
        sb.append(title);

        node = data.selectSingleNode("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_CATEGORY);
        return doc;
    }

    /**
     * Extracts data for indexing from discussion. Item must be synchronized.
     */
    static MyDocument indexDiscussion(Item discussion) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) discussion.getData().selectSingleNode("data");
        Node node = data.selectSingleNode("title");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        } else
            title = "Diskuse";

        node = data.selectSingleNode("text");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        if ( discussion.getChildren().size()>0 ) {
            Record record = (Record) ((Relation) discussion.getChildren().get(0)).getChild();
            persistance.synchronize(record);

            Iterator nodes = record.getData().getRootElement().elementIterator("comment");
            while (nodes.hasNext()) {
                data = (Element) nodes.next();
                node = data.selectSingleNode("title");
                if ( node!=null ) {
                    sb.append(" ");
                    sb.append(node.getText());
                }

                node = data.selectSingleNode("text");
                if ( node!=null ) {
                    sb.append(" ");
                    sb.append(node.getText());
                }

                node = data.selectSingleNode("author");
                if ( node!=null ) {
                    sb.append(" ");
                    sb.append(node.getText());
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DISCUSSION);
        return doc;
    }

    /**
     * Extracts data for indexing from make. Item must be synchronized.
     */
    static MyDocument indexMake(Item make) {
        Element data = (Element) make.getData().selectSingleNode("data");
        String title = "", type = "", tmp;

        Node node = data.selectSingleNode("name");
        title = node.getText();

        StringBuffer sb = new StringBuffer(title);
        for ( Iterator iter = make.getChildren().iterator(); iter.hasNext(); ) {
            Relation relation = (Relation) iter.next();
            if (!(relation.getChild() instanceof Record)) continue;
            Record record = (Record) persistance.findById(relation.getChild());

            if ( record.getType()==Record.HARDWARE ) {
                indexHardware(record, sb);
                type = MyDocument.TYPE_HARDWARE;
            }
            if ( record.getType()==Record.SOFTWARE ) {
                indexSoftware(record, sb);
                type = MyDocument.TYPE_SOFTWARE;
            }
            sb.append(" ");
        }

        tmp = Tools.removeTags(sb.toString());
        MyDocument doc = new MyDocument(tmp);
        doc.setTitle(title);
        if (type.length()==0)
            log.warn("Unknown type for "+make);
        doc.setType(type);
        return doc;
    }

    /**
     * Extracts data for indexing from hardware. Record must be synchronized.
     */
    static void indexHardware(Record record, StringBuffer sb) {
        Element data = (Element) record.getData().selectSingleNode("data");
        Node node = data.selectSingleNode("setup");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.selectSingleNode("params");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.selectSingleNode("identification");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        node = data.selectSingleNode("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }
    }

    /**
     * Extracts data for indexing from software. Record must be synchronized.
     */
    static void indexSoftware(Record record, StringBuffer sb) {
        Element data = (Element) record.getData().selectSingleNode("data");
        Node node = data.selectSingleNode("text");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }
    }

    /**
     * Extracts data for indexing from driver. Item must be synchronized.
     */
    static MyDocument indexDriver(Item driver) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) driver.getData().selectSingleNode("data");
        Node node = data.selectSingleNode("name");
        title = node.getText();
        sb.append(title);

        node = data.selectSingleNode("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DRIVER);
        return doc;
    }

    /**
     * Extracts data for indexing from article. Item must be synchronized.
     */
    static MyDocument indexArticle(Item article) {
        StringBuffer sb = new StringBuffer();
        String title = null;

        Element data = (Element) article.getData().selectSingleNode("data");
        Node node = data.selectSingleNode("name");
        title = node.getText();
        sb.append(title);
        sb.append(" ");

        node = data.selectSingleNode("perex");
        if ( node!=null ) {
            sb.append(node.getText());
            sb.append(" ");
        }

        for ( Iterator iter = article.getChildren().iterator(); iter.hasNext(); ) {
            Relation child = (Relation) iter.next();

            if ( child.getChild() instanceof Record ) {
                Record record = (Record) persistance.findById(child.getChild());
                if ( record.getType()==Record.ARTICLE ) {
                    List nodes = record.getData().selectNodes("/data/content");
                    if (nodes.size()==1) {
                        sb.append(((Node)nodes.get(0)).getText());
                        sb.append(" ");
                    } else
                        for ( Iterator iter2 = nodes.iterator(); iter2.hasNext(); ) {
                            node = (Element) iter2.next();
                            sb.append(node.getText());
                            sb.append(" ");
                            sb.append(((Element)node).attributeValue("title"));
                            sb.append(" ");
                        }
                }
            } else if ( child.getChild() instanceof Item ) {
                Item item = (Item) persistance.findById(child.getChild());
                if (item.getType()!=Item.DISCUSSION) continue;
                MyDocument doc = indexDiscussion(item);
                String diz = doc.getDocument().get(MyDocument.CONTENT);
                if (diz!=null) {
                    sb.append(diz);
                    sb.append(" ");
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_ARTICLE);
        return doc;
    }

    /**
     * Extracts data for indexing from news. Item must be synchronized.
     */
    static MyDocument indexNews(Item news) {
        StringBuffer sb = new StringBuffer();
        String title;

        Element data = (Element) news.getData().selectSingleNode("data");
        String content = data.selectSingleNode("content").getText();
        sb.append(content);
        sb.append(" ");

        String tmp = Tools.removeTags(content);
        title = Tools.limit(tmp,50," ..");

        Node node = data.selectSingleNode("category");
        String category = null;
        if (node!=null)
            category = node.getText();

        for ( Iterator iter = news.getChildren().iterator(); iter.hasNext(); ) {
            Relation child = (Relation) iter.next();

            if ( child.getChild() instanceof Item ) {
                Item item = (Item) persistance.findById(child.getChild());
                if (item.getType()!=Item.DISCUSSION) continue;
                MyDocument doc = indexDiscussion(item);
                String diz = doc.getDocument().get(MyDocument.CONTENT);
                if (diz!=null) {
                    sb.append(diz);
                    sb.append(" ");
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_NEWS);
        if (category!=null)
            doc.setNewsCategory(category);
        return doc;
    }

    /**
     * Extracts text from dictionary item (and its records) for indexing.
     * @param dictionary initialized item
     */
    static MyDocument indexDictionary(Item dictionary) {
        String title = Tools.xpath(dictionary, "/data/name"), s;
        StringBuffer sb = new StringBuffer(title);
        Relation relation;
        Record record;

        for (Iterator iter = dictionary.getChildren().iterator(); iter.hasNext();) {
            relation = (Relation) iter.next();
            record = (Record) persistance.findById(relation.getChild());
            s = Tools.xpath(record, "//description");
            sb.append(' ');
            sb.append(Tools.removeTags(s));
        }

        MyDocument doc = new MyDocument(sb.toString());
        doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DICTIONARY);
        doc.setURL("/slovnik/"+dictionary.getSubType());

        return doc;
    }

    /**
     * Empty constructor.
     */
    public CreateIndex() {
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        indexPath = prefs.get(PREF_PATH, null);
        lastRunFilename = prefs.get(PREF_LAST_RUN_NAME, LAST_RUN_FILE);
    }

    /**
     * @return directory, where index has been generated.
     */
    public static String getIndexPath() {
        return indexPath;
    }

    /**
     * @return File, which serves as timestamp of last indexing. Used for monitoring.
     */
    public static File getLastRunFile() {
        return new File(indexPath,lastRunFilename);
    }
}
