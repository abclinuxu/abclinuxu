/*
 * User: literakl
 * Date: 17.5.2002
 * Time: 13:29:24
 * (c) 2001-2002 Tinnio
 */
package cz.abclinuxu.utils.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Easy to use and standardized generator of Document.
 * You must set URL before you get Document.
 */
public class MyDocument {
    public static final String TYPE_CATEGORY = "oddil";
    public static final String TYPE_MAKE = "polozka";
    public static final String TYPE_DISCUSSION = "diskuse";
    public static final String TYPE_RECORD = "zaznam";
    public static final String TYPE_DRIVER = "ovladac";
    public static final String TYPE_ARTICLE = "clanek";

    public static final String TYPE = "typ";
    public static final String CONTENT = "contents";
    public static final String URL = "url";
    public static final String TITLE = "title";

    Document document;

    /**
     * Creates Documents and fills it with String to be indexed.
     */
    public MyDocument(String content) {
        document = new Document();
        document.add(Field.Text(CONTENT,content));
    }

    /**
     * Associates URL with Document.
     */
    public void setURL(String url) {
        document.add(Field.UnIndexed(URL,url));
    }

    /**
     * Sets title for the Document.
     */
    public void setTitle(String title) {
        document.add(Field.UnIndexed(TITLE,title));
    }

    /**
     * Sets type of the Document.
     */
    public void setType(String type) {
        document.add(Field.Keyword(TYPE,type));
    }

    /**
     * @return Associated Lucene's Document.
     */
    public Document getDocument() {
        return document;
    }
}
