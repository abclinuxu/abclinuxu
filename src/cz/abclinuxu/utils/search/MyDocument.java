/*
 * User: literakl
 * Date: 17.5.2002
 * Time: 13:29:24
 */
package cz.abclinuxu.utils.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Easy to use and standardized generator of Document.
 * You must set URL before you get Document.
 */
public class MyDocument {
    public static final String TYPE_CATEGORY = "sekce";
    public static final String TYPE_HARDWARE = "hardware";
    public static final String TYPE_SOFTWARE = "software";
    public static final String TYPE_DISCUSSION = "diskuse";
    public static final String TYPE_DRIVER = "ovladac";
    public static final String TYPE_ARTICLE = "clanek";
    public static final String TYPE_NEWS = "zpravicka";
    public static final int ALL_TYPES_COUNT = 7;

    /** Object's text to be tokenized and indexed. */
    public static final String CONTENT = "obsah";
    /** Type of the object. */
    public static final String TYPE = "typ";
    /** URL, where to display the object. */
    public static final String URL = "url";
    /** User friendly title. */
    public static final String TITLE = "title";
    /** Id of parent object. */
    public static final String PARENT = "parent";
    /** News category */
    public static final String NEWS_CATEGORY = "kategorie";

    Document document;

    /**
     * Creates Documents and fills it with String to be indexed.
     */
    public MyDocument(String content) {
        document = new Document();
        document.add(Field.UnStored(CONTENT,content));
    }

    /**
     * Associates URL with Document.
     */
    public Field setURL(String url) {
        Field field = Field.UnIndexed(URL,url);
        document.add(field);
        return field;
    }

    /**
     * Sets title for the Document.
     */
    public Field setTitle(String title) {
        Field field = Field.Text(TITLE,title);
        document.add(field);
        return field;
    }

    /**
     * Sets type of the Document.
     */
    public Field setType(String type) {
        Field field = Field.Keyword(TYPE,type);
        document.add(field);
        return field;
    }

    /**
     * Sets id of parent object.
     */
    public Field setParent(int parent) {
        Field field = Field.Keyword(PARENT,new Integer(parent).toString());
        document.add(field);
        return field;
    }

    /**
     * Sets id of parent object.
     */
    public Field setNewsCategory(String category) {
        Field field = Field.Keyword(NEWS_CATEGORY, category);
        document.add(field);
        return field;
    }

    /**
     * @return Associated Lucene's Document.
     */
    public Document getDocument() {
        return document;
    }
}
