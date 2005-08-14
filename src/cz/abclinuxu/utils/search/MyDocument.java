/*
 * User: literakl
 * Date: 17.5.2002
 * Time: 13:29:24
 */
package cz.abclinuxu.utils.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.Date;

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
    public static final String TYPE_DICTIONARY = "pojem";
    public static final String TYPE_FAQ = "faq";
    public static final String TYPE_BLOG = "blog";
    public static final int ALL_TYPES_COUNT = 9;

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
    /** State of question, possible values are ano/ne */
    public static final String QUESTION_SOLVED = "vyreseno";
    /** number of responses in discussion */
    public static final String NUMBER_OF_REPLIES = "odpovedi";
    /** time when object was created */
    public static final String CREATED = "vytvoreno";
    /** time of last update of object */
    public static final String UPDATED = "zmeneno";

    Document document;

    /**
     * Creates Documents and fills it with String to be indexed.
     */
    public MyDocument(String content) {
        document = new Document();
        document.add(Field.Text(CONTENT, content));
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
        title = Tools.removeTags(title);
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
     * Sets whether the question was solved.
     */
    public Field setQuestionSolved(boolean solved) {
        Field field = Field.Keyword(QUESTION_SOLVED, solved? "ano":"ne");
        document.add(field);
        return field;
    }

    /**
     * Sets numkber of replies in discussion.
     */
    public Field setNumberOfReplies(int replies) {
        Field field = Field.Keyword(NUMBER_OF_REPLIES, Integer.toString(replies));
        document.add(field);
        return field;
    }

    /**
     * Sets numkber of replies in discussion.
     */
    public Field setNumberOfReplies(String replies) {
        Field field = Field.Keyword(NUMBER_OF_REPLIES, replies);
        document.add(field);
        return field;
    }

    /**
     * Sets time when object was created.
     */
    public Field setCreated(Date date) {
        Field field = Field.Keyword(CREATED, date);
        document.add(field);
        return field;
    }

    /**
     * Sets last time when object was updated.
     */
    public Field setUpdated(Date date) {
        Field field = Field.Keyword(UPDATED, date);
        document.add(field);
        return field;
    }

    /**
     * Sets boost factor for this document.
     * @param boost boost factor
     */
    public void setBoost(float boost) {
        document.setBoost(boost);
    }

    /**
     * @return Associated Lucene's Document.
     */
    public Document getDocument() {
        return document;
    }
}
