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
import org.dom4j.Element;
import org.dom4j.Node;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.*;

/**
 * This class is responsible for creating and
 * maintaining Lucene's index.
 */
public class CreateIndex {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateIndex.class);
    static String DEPLOY = "/home/literakl/tomcat/webapps/ROOT";
    static String PATH = DEPLOY+"/WEB-INF/index";

    static Persistance persistance;
    static RE tagRE;

    static {
        DOMConfigurator.configure(DEPLOY+"/WEB-INF/log4j.xml");

        persistance = new MySqlPersistance(PersistanceFactory.defaultUrl);
        persistance.setCache(new EmptyCache());

        try {
            tagRE = new RE("<[^>]+>");
        } catch (RESyntaxException e) {
            log.error("Cannot compile regexp!",e);
        }
    }

    IndexWriter indexWriter;
    HashMap indexed = new HashMap(5000);


    public static void main(String[] args) throws Exception {
        if ( args.length>0 ) PATH = args[0];
        log.info("Starting to index data, using directory "+PATH);

        try {
            IndexWriter indexWriter = new IndexWriter(PATH,new StandardAnalyzer(),true);
            CreateIndex test = new CreateIndex(indexWriter);
            Relation articles = (Relation) persistance.findById(new Relation(Constants.REL_ARTICLES));
            Relation hardware = (Relation) persistance.findById(new Relation(Constants.REL_HARDWARE));
            Relation software = (Relation) persistance.findById(new Relation(Constants.REL_SOFTWARE));
            Relation drivers = (Relation) persistance.findById(new Relation(Constants.REL_DRIVERS));

            long start = System.currentTimeMillis();

            test.makeIndexOn(articles,UrlUtils.PREFIX_CLANKY);
            test.makeIndexOn(hardware,UrlUtils.PREFIX_HARDWARE);
            test.makeIndexOn(software,UrlUtils.PREFIX_SOFTWARE);
            test.makeIndexOn(drivers,UrlUtils.PREFIX_DRIVERS);

            indexWriter.optimize();
            indexWriter.close();
            long end = System.currentTimeMillis();

            log.info("Indexing of "+indexWriter.docCount()+" documents took "+(end-start)/1000+" seconds.");
            System.out.println("Indexing of "+indexWriter.docCount()+" documents took "+(end-start)/1000+" seconds.");
        } catch (Exception e) {
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
        if ( indexed.containsKey(relation) ) return;
        indexed.put(new Relation(relation.getId()),new Relation(relation.getId()));

        GenericObject obj = relation.getChild();
        persistance.synchronize(obj);

        MyDocument doc = null;
        if ( obj instanceof Category ) {
            doc = getCategoryIndexingString((Category)obj);

        } else if ( obj instanceof Item ) {
            Item item = (Item) obj;
            switch ( item.getType() ) {
                case Item.ARTICLE: doc = getArticleIndexingString(item);break;
                case Item.DISCUSSION: doc = getDiscussionIndexingString(item);break;
                case Item.MAKE: doc = getMakeIndexingString(item);break;
                case Item.DRIVER: doc = getDriverIndexingString(item);break;
            }

        } else if ( obj instanceof Record ) {
            Record record = (Record) obj;
            switch ( record.getType() ) {
                case Record.HARDWARE: doc = getHardwareIndexingString(record);break;
                case Record.SOFTWARE: doc = getSoftwareIndexingString(record);break;
            }
        }

        if ( doc!=null ) {
            doc.setURL(urlPrefix+"/ViewRelation?relationId="+relation.getId());
            indexWriter.addDocument(doc.getDocument());
        }

        for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            makeIndexOn(child,urlPrefix);
        }
    }

    /**
     * This method removes all tags from text.
     */
    static String removeTags(String text) {
        return tagRE.subst(text,"");
    }

    /**
     * Extracts data for indexing from category. Category must be synchronized.
     */
    static MyDocument getCategoryIndexingString(Category category) {
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

        MyDocument doc = new MyDocument(removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_CATEGORY);
        return doc;
    }

    /**
     * Extracts data for indexing from article. Item must be synchronized.
     */
    static MyDocument getArticleIndexingString(Item article) {
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

        MyDocument doc = new MyDocument(removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_ARTICLE);
        return doc;
    }

    /**
     * Extracts data for indexing from discussion. Item must be synchronized.
     */
    static MyDocument getDiscussionIndexingString(Item discussion) {
        Element data = (Element) discussion.getData().selectSingleNode("data");
        StringBuffer sb = new StringBuffer();
        String title = null;

        Node node = data.selectSingleNode("title");
        if ( node!=null ) {
            title = node.getText();
            sb.append(title);
        }

        node = data.selectSingleNode("text");
        if ( node!=null ) {
            sb.append(" ");
            sb.append(node.getText());
        }

        for (Iterator iter = discussion.getContent().iterator(); iter.hasNext();) {
            Relation child = (Relation) iter.next();
            if ( child.getChild() instanceof Record ) {
                Record record = (Record) child.getChild();
                persistance.synchronize(record);
                iter.remove();

                sb.append(" ");
                data = (Element) record.getData().selectSingleNode("data");

                node = data.selectSingleNode("title");
                if ( node!=null ) sb.append(node.getText());

                node = data.selectSingleNode("text");
                if ( node!=null ) {
                    sb.append(" ");
                    sb.append(node.getText());
                }
            }
        }

        MyDocument doc = new MyDocument(removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DISCUSSION);
        return doc;
    }

    /**
     * Extracts data for indexing from make. Item must be synchronized.
     */
    static MyDocument getMakeIndexingString(Item make) {
        Element data = (Element) make.getData().selectSingleNode("data");
        String title = null;

        Node node = data.selectSingleNode("name");
        if ( node!=null ) {
            title = node.getText();
        }

        MyDocument doc = new MyDocument(removeTags(title));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_MAKE);
        return doc;
    }

    /**
     * Extracts data for indexing from driver. Item must be synchronized.
     */
    static MyDocument getDriverIndexingString(Item driver) {
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

        MyDocument doc = new MyDocument(removeTags(sb.toString()));
        if ( title!=null && title.length()>0 ) doc.setTitle(title);
        doc.setType(MyDocument.TYPE_DRIVER);
        return doc;
    }

    /**
     * Extracts data for indexing from hardware. Record must be synchronized.
     */
    static MyDocument getHardwareIndexingString(Record record) {
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

        MyDocument doc = new MyDocument(removeTags(sb.toString()));
        doc.setType(MyDocument.TYPE_RECORD);
        return doc;
    }

    /**
     * Extracts data for indexing from software. Record must be synchronized.
     */
    static MyDocument getSoftwareIndexingString(Record record) {
        Element data = (Element) record.getData().selectSingleNode("data");
        String str = null;

        Node node = data.selectSingleNode("text");
        if ( node!=null ) str = node.getText();

        MyDocument doc = new MyDocument(removeTags(str));
        doc.setType(MyDocument.TYPE_RECORD);
        return doc;
    }
}
