/*
 * User: literakl
 * Date: 16.5.2002
 * Time: 15:19:10
 * (c) 2001-2002 Tinnio
 */
package cz.abclinuxu.utils.search;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.Tools;
import org.dom4j.Element;
import org.dom4j.Node;
import org.apache.lucene.index.IndexWriter;

import java.util.*;
import java.util.prefs.Preferences;

/**
 * This class is responsible for creating and
 * maintaining Lucene's index.
 * todo give score boost to titles and names of objects
 * todo replace recursion with stack based implementation 
 */
public class CreateIndex implements Configurable {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateIndex.class);

    public static final String PREF_PATH = "path";

    static String indexPath;
    static Persistance persistance;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new CreateIndex(null));
        persistance = PersistanceFactory.getPersistance(EmptyCache.class);
    }

    IndexWriter indexWriter;
    HashMap indexedRelations = new HashMap(50000,0.9f);
    HashMap indexedItems = new HashMap(10000,0.9f);


    public static void main(String[] args) throws Exception {
        String PATH = indexPath;
        if ( args.length>0 ) PATH = args[0];
        log.info("Starting to index data, using directory "+PATH);

        try {
            IndexWriter indexWriter = new IndexWriter(PATH,new AbcCzechAnalyzer(),true);
            CreateIndex test = new CreateIndex(indexWriter);
            Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLES));
            Relation hardware = (Relation) persistance.findById(new Relation(Constants.REL_HARDWARE));
            Relation software = (Relation) persistance.findById(new Relation(Constants.REL_SOFTWARE));
            Relation drivers = (Relation) persistance.findById(new Relation(Constants.REL_DRIVERS));
            Relation abc = (Relation) persistance.findById(new Relation(Constants.REL_ABC));

            long start = System.currentTimeMillis();

            test.makeIndexOn(articles,UrlUtils.PREFIX_CLANKY);
            test.makeIndexOn(hardware,UrlUtils.PREFIX_HARDWARE);
            test.makeIndexOn(software,UrlUtils.PREFIX_SOFTWARE);
            test.makeIndexOn(drivers,UrlUtils.PREFIX_DRIVERS);
            test.makeIndexOn(abc,UrlUtils.PREFIX_CLANKY);

            indexWriter.optimize();
            indexWriter.close();
            long end = System.currentTimeMillis();

            log.info("Indexing of "+indexWriter.docCount()+" documents took "+(end-start)/1000+" seconds.");
            System.out.println("Indexing of "+indexWriter.docCount()+" documents took "+(end-start)/1000+" seconds.");
        } catch (Throwable e) {
            log.error("Indexing failed!",e);
        }
    }

    /**
     * Creates new instance with initialized IndexWriter.
     */
    public CreateIndex(IndexWriter writer) {
        indexWriter = writer;
    }

    /**
     * This method recursively makes index on relation and its
     * children. There must be no loops in tree!
     * @param relation relation, where to start. It must be already synchronized.
     * @param urlPrefix prefix for URL for this subtree
     */
    void makeIndexOn(Relation relation, String urlPrefix) throws Exception {
        Integer id = new Integer(relation.getId());
        if ( indexedRelations.containsKey(id) ) return;
        indexedRelations.put(id,id);

        GenericObject obj = relation.getChild();
        persistance.synchronize(obj);

        MyDocument doc = null;
        if ( obj instanceof Category ) {
            doc = indexCategory((Category)obj);
        } else if ( obj instanceof Item ) {
            Item item = (Item) obj;
            id = new Integer(item.getId());
            if ( indexedItems.containsKey(id) ) return;
            indexedItems.put(id, id);

            switch ( item.getType() ) {
                case Item.ARTICLE: doc = indexArticle(item);break;
                case Item.DISCUSSION: doc = indexDiscussion(item,relation);break;
                case Item.MAKE: doc = indexMake(item);break;
                case Item.DRIVER: doc = indexDriver(item);break;
            }
        }

        if ( doc!=null ) {
            doc.setURL(urlPrefix+"/ViewRelation?rid="+relation.getId());
            indexWriter.addDocument(doc.getDocument());
        }

        for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( ! (child.getChild() instanceof Record) )
                makeIndexOn(child,urlPrefix);
        }
    }

    /**
     * Extracts data for indexing from category. Category must be synchronized.
     */
    static MyDocument indexCategory(Category category) {
        Element data = (Element) category.getData().selectSingleNode("data");
        StringBuffer sb = new StringBuffer();
        String title = null;

        Node node = data.selectSingleNode("name");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        }

        node = data.selectSingleNode("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_CATEGORY);
        return doc;
    }

    /**
     * Extracts data for indexing from article. Item must be synchronized.
     */
    static MyDocument indexArticle(Item article) {
        Element data = (Element) article.getData().selectSingleNode("data");
        StringBuffer sb = new StringBuffer();
        String title = null;

        Node node = data.selectSingleNode("name");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        }

        node = data.selectSingleNode("perex");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }
        sb.append(" ");

        for (Iterator iter = article.getContent().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( child.getChild() instanceof Record ) {
                Record record = (Record) child.getChild();
                persistance.synchronize(record);
                if ( record.getType()==Record.ARTICLE ) {
                    iter.remove();

                    data = (Element) record.getData().selectSingleNode("data");
                    node = data.selectSingleNode("content");
                    if ( node!=null ) sb.append(node.getText());

                    node = data.selectSingleNode("name");
                    if ( node!=null ) {
                        sb.append(" ");
                        sb.append(node.getText());
                    }
                }
            }
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_ARTICLE);
        return doc;
    }

    /**
     * Extracts data for indexing from discussion. Item must be synchronized.
     */
    static MyDocument indexDiscussion(Item discussion, Relation relation) {
        Element data = (Element) discussion.getData().selectSingleNode("data");
        StringBuffer sb = new StringBuffer();
        String title = null;

        Node node = data.selectSingleNode("title");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        } else {
            // let's use parent's title, if this is discussion to article
            GenericObject parent = relation.getParent();
            if ( parent instanceof Item && ((Item)parent).getType()==Item.ARTICLE ) {
                node = ((Item)parent).getData().selectSingleNode("data/name");
                if ( node!=null )
                    title = "Diskuse k �l�nku " + node.getText();
            }
        }

        node = data.selectSingleNode("text");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        if (discussion.getContent().size()>0) {
            Record record = (Record) ((Relation) discussion.getContent().get(0)).getChild();
            persistance.synchronize(record);
            List nodes = record.getData().selectNodes("/data/comment");

            for ( Iterator iter = nodes.iterator(); iter.hasNext(); ) {
                data = (Element) iter.next();
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
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DISCUSSION);
        return doc;
    }

    /**
     * Extracts data for indexing from make. Item must be synchronized.
     */
    static MyDocument indexMake(Item make) {
        Element data = (Element) make.getData().selectSingleNode("data");
        String title = "", tmp = "";

        Node node = data.selectSingleNode("name");
        if ( node!=null ) {
            title = node.getText();
        }
        StringBuffer sb = new StringBuffer(title);

        List content = make.getContent();
        Map children = Tools.groupByType(content);
        List records = (List) children.get(Constants.TYPE_RECORD);

        for (Iterator iter = records.iterator(); iter.hasNext();) {
            Relation relation = (Relation)iter.next();
            Record record = (Record) relation.getChild();
            content.remove(relation);

            if ( record.getType()==Record.HARDWARE ) tmp = indexHardware(record);
            if ( record.getType()==Record.SOFTWARE ) tmp = indexSoftware(record);
            sb.append(" ");
            sb.append(tmp);
        }

        MyDocument doc = new MyDocument(Tools.removeTags(tmp));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_MAKE);
        return doc;
    }

    /**
     * Extracts data for indexing from driver. Item must be synchronized.
     */
    static MyDocument indexDriver(Item driver) {
        Element data = (Element) driver.getData().selectSingleNode("data");
        StringBuffer sb = new StringBuffer();
        String title = null;

        Node node = data.selectSingleNode("name");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        }

        node = data.selectSingleNode("note");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        MyDocument doc = new MyDocument(Tools.removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DRIVER);
        return doc;
    }

    /**
     * Extracts data for indexing from hardware. Record must be synchronized.
     */
    static String indexHardware(Record record) {
        Element data = (Element) record.getData().selectSingleNode("data");
        StringBuffer sb = new StringBuffer();

        Node node = data.selectSingleNode("setup");
        if ( node!=null ) sb.append(node.getText());

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

        return sb.toString();
    }

    /**
     * Extracts data for indexing from software. Record must be synchronized.
     */
    static String indexSoftware(Record record) {
        Element data = (Element) record.getData().selectSingleNode("data");
        String str = null;

        Node node = data.selectSingleNode("text");
        if ( node!=null ) str = node.getText();

        return str;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        indexPath = prefs.get(PREF_PATH, null);
    }

    /**
     * @return directory, where index has been generated.
     */
    public static String getIndexPath() {
        return indexPath;
    }
}
