/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 10:46:36
 */
package cz.abclinuxu.migrate;

import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.persistance.cache.EmptyCache;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.format.Format;
import cz.abclinuxu.utils.format.FormatDetector;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.dom4j.Element;

/**
 * Format introduced. Tjis conversion utility finds it and sets it for data in database.
 * It can be run multiple times.
 */
public class InsertFormat {
    private static final String FORMAT = "format";
    static Persistance persistance;
    static SQLTool sqlTool;
    static int column = 0;

    public static void main(String[] args) throws Exception {
        persistance = PersistanceFactory.getPersistance(EmptyCache.class);
        sqlTool = SQLTool.getInstance();

        System.out.println("This utility must not be run, if portal is running!");
        System.err.println("Press enter");
        System.in.read();

        System.out.println("Starting ..\n");
        long start = System.currentTimeMillis();
        updateArticles();
        updateQuestion();
        updateDriver();
        updateNews();
        updateCategory();
        updateHardware();
        updateSoftware();
        updateComment();

        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("\nTotal time: "+seconds+" seconds");
    }

    static void updateArticles() throws Exception {
        System.out.println("About to update articles.");
        long start = System.currentTimeMillis();
        Record article;
        Integer key;
        Element element;
        List nodes;
        boolean modified = false;

        List articles = getRecords(Record.ARTICLE);
        for ( Iterator iter = articles.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            article = (Record) persistance.findById(new Record(key.intValue()));
            nodes = article.getData().selectNodes("/data/content");
            for ( Iterator iterIn = nodes.iterator(); iterIn.hasNext(); ) {
                element = (Element) iterIn.next();
                if (element.attributeValue(FORMAT)==null) {
                    hash();
                    element.addAttribute(FORMAT, Integer.toString(Format.HTML.getId()));
                    modified = true;
                }
            }
            if (modified) {
                Date lastModified = article.getUpdated();
                persistance.update(article);
                sqlTool.setUpdatedTimestamp(article, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateQuestion() throws Exception {
        System.out.println("About to update discussion questions");
        long start = System.currentTimeMillis();
        Item item;
        Integer key;
        Element element;
        Format format;
        boolean modified = false;

        List questions = getItems(Item.DISCUSSION);
        for ( Iterator iter = questions.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            item = (Item) persistance.findById(new Item(key.intValue()));
            element = (Element) item.getData().selectSingleNode("/data/text");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }
            if ( modified ) {
                Date lastModified = item.getUpdated();
                persistance.update(item);
                sqlTool.setUpdatedTimestamp(item, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateDriver() throws Exception {
        System.out.println("About to update drivers");
        long start = System.currentTimeMillis();
        Item item;
        Integer key;
        Element element;
        Format format;
        boolean modified = false;

        List drivers = getItems(Item.DRIVER);
        for ( Iterator iter = drivers.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            item = (Item) persistance.findById(new Item(key.intValue()));
            element = (Element) item.getData().selectSingleNode("/data/note");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }
            if ( modified ) {
                Date lastModified = item.getUpdated();
                persistance.update(item);
                sqlTool.setUpdatedTimestamp(item, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateNews() throws Exception {
        System.out.println("About to update news");
        long start = System.currentTimeMillis();
        Item item;
        Integer key;
        Element element;
        boolean modified = false;

        List news = getItems(Item.NEWS);
        for ( Iterator iter = news.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            item = (Item) persistance.findById(new Item(key.intValue()));
            element = (Element) item.getData().selectSingleNode("/data/content");
            if ( element.attributeValue(FORMAT)==null ) {
                hash();
                element.addAttribute(FORMAT, Integer.toString(Format.HTML.getId()));
                modified = true;
            }
            if ( modified ) {
                Date lastModified = item.getUpdated();
                persistance.update(item);
                sqlTool.setUpdatedTimestamp(item, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateCategory() throws Exception {
        System.out.println("About to update categories");
        long start = System.currentTimeMillis();
        Category category;
        Integer key;
        Element element;
        Format format;
        boolean modified = false;

        List categories = getCategories();
        for ( Iterator iter = categories.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            category = (Category) persistance.findById(new Category(key.intValue()));
            element = (Element) category.getData().selectSingleNode("/data/note");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }
            if ( modified ) {
                Date lastModified = category.getUpdated();
                persistance.update(category);
                sqlTool.setUpdatedTimestamp(category, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateHardware() throws Exception {
        System.out.println("About to update hardware");
        long start = System.currentTimeMillis();
        Record hardware;
        Integer key;
        Element element;
        Format format;
        boolean modified = false;

        List records = getRecords(Record.HARDWARE);
        for ( Iterator iter = records.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            hardware = (Record) persistance.findById(new Record(key.intValue()));

            element = (Element) hardware.getData().selectSingleNode("/data/setup");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }

            element = (Element) hardware.getData().selectSingleNode("/data/identification");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }

            element = (Element) hardware.getData().selectSingleNode("/data/params");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }

            element = (Element) hardware.getData().selectSingleNode("/data/note");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }
            if ( modified ) {
                Date lastModified = hardware.getUpdated();
                persistance.update(hardware);
                sqlTool.setUpdatedTimestamp(hardware, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateSoftware() throws Exception {
        System.out.println("About to update software");
        long start = System.currentTimeMillis();
        Record software;
        Integer key;
        Element element;
        Format format;
        boolean modified = false;

        List records = getRecords(Record.SOFTWARE);
        for ( Iterator iter = records.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            software = (Record) persistance.findById(new Record(key.intValue()));

            element = (Element) software.getData().selectSingleNode("/data/text");
            if ( element!=null && element.attributeValue(FORMAT)==null ) {
                hash();
                format = FormatDetector.detect(element.getText());
                element.addAttribute(FORMAT, Integer.toString(format.getId()));
                modified = true;
            }
            if ( modified ) {
                Date lastModified = software.getUpdated();
                persistance.update(software);
                sqlTool.setUpdatedTimestamp(software, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void updateComment() throws Exception {
        System.out.println("About to update dicsussion comments");
        long start = System.currentTimeMillis();
        Record comment;
        Integer key;
        Element element;
        List nodes;
        boolean modified = false;

        List records = getRecords(Record.DISCUSSION);
        for ( Iterator iter = records.iterator(); iter.hasNext(); ) {
            key = (Integer) iter.next();
            comment = (Record) persistance.findById(new Record(key.intValue()));
            nodes = comment.getData().selectNodes("/data/comment");
            for ( Iterator iterIn = nodes.iterator(); iterIn.hasNext(); ) {
                element = (Element) iterIn.next();
                element = (Element) element.selectSingleNode("text");
                if ( element.attributeValue(FORMAT)==null ) {
                    hash();
                    element.addAttribute(FORMAT, Integer.toString(Format.HTML.getId()));
                    modified = true;
                }
            }
            if ( modified ) {
                Date lastModified = comment.getUpdated();
                persistance.update(comment);
                sqlTool.setUpdatedTimestamp(comment, lastModified);
            }
        }
        resetHash();
        int seconds = (int) (System.currentTimeMillis()-start)/1000;
        System.out.println("finished ("+seconds+" seconds)");
    }

    static void hash() {
        if (column==40) {
            column = 0;
            System.out.print('\n');
            System.out.flush();
        }
        System.out.print('#');
        column++;
    }

    static void resetHash() {
        if (column>0)
            System.out.print('\n');
        column = 0;
    }

    static List getItems(int type) throws Exception {
        List list = new ArrayList(2000);
        int key;

        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = persistance.getSQLConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select cislo from polozka where typ="+type);
        while (rs.next()) {
            key = rs.getInt(1);
            list.add(new Integer(key));
        }
        rs.close();statement.close();con.close();
        return list;
    }

    static List getRecords(int type) throws Exception {
        List list = new ArrayList(10000);
        int key;

        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = persistance.getSQLConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select cislo from zaznam where typ="+type);
        while (rs.next()) {
            key = rs.getInt(1);
            list.add(new Integer(key));
        }
        rs.close();statement.close();con.close();
        return list;
    }

    static List getCategories() throws Exception {
        List list = new ArrayList(500);
        int key;

        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = persistance.getSQLConnection();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("select cislo from kategorie");
        while (rs.next()) {
            key = rs.getInt(1);
            list.add(new Integer(key));
        }
        rs.close();statement.close();con.close();
        return list;
    }
}
