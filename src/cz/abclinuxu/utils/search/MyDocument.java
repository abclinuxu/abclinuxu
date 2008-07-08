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
package cz.abclinuxu.utils.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistence.PersistenceMapping;

import java.util.Date;

/**
 * Easy to use and standardized generator of Document.
 * You must set URL before you get Document.
 */
public class MyDocument {
    // following constants are keys from DocumentTypes.searchTypes property
    public static final String TYPE_ARTICLE = "clanek";
    public static final String TYPE_BAZAAR = "bazar";
    public static final String TYPE_BLOG = "blog";
    public static final String TYPE_CATEGORY = "sekce";
    public static final String TYPE_DICTIONARY = "pojem";
    public static final String TYPE_DISCUSSION = "diskuse";
    public static final String TYPE_DOCUMENT = "dokument";
    public static final String TYPE_DRIVER = "ovladac";
    public static final String TYPE_FAQ = "faq";
    public static final String TYPE_HARDWARE = "hardware";
    public static final String TYPE_NEWS = "zpravicka";
    public static final String TYPE_PERSONALITY = "osobnost";
    public static final String TYPE_POLL = "anketa";
    public static final String TYPE_QUESTION = "poradna";
    public static final String TYPE_SOFTWARE = "software";

    /** Object's text to be tokenized and indexed. */
    public static final String CONTENT = "obsah";
    /** Type of the object. */
    public static final String TYPE = "typ";
    /** URL, where to display the object. */
    public static final String VALUE_URL = "url";
    /** User friendly title. */
    public static final String TITLE = "titulek";
    /** Id of parent object. */
    public static final String PARENT = "predek";
    /** class type and id of object, e.g. P12345 */
    public static final String CID = "cid";
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
    /** stored value - number of characters of indexed content */
    public static final String VALUE_SIZE = "velikost_obsahu";

    Document document;

    /**
     * Creates Documents and fills it with String to be indexed.
     * @param title title of the document
     * @param content content of the document
     * @param concatenate whether to append title to content
     */
    public MyDocument(String title, String content, boolean concatenate) {
        document = new Document();

        if (title == null)
            title = "";
        else
            title = Tools.removeTags(title);
        Field field = new Field(TITLE, title, Field.Store.YES, Field.Index.TOKENIZED);
        document.removeField(TITLE);
        document.add(field);

        if (content == null)
            content = "";
        if (concatenate)
            content = title + " " + content;

        content = Tools.removeTags(content);
        document.add(new Field(CONTENT, content, Field.Store.YES, Field.Index.TOKENIZED));
        document.add(new Field(VALUE_SIZE, Integer.toString(content.length()), Field.Store.YES, Field.Index.NO));
    }

    /**
     * Associates URL with Document.
     */
    public Field setURL(String url) {
        Field field = new Field(VALUE_URL, url, Field.Store.YES, Field.Index.NO);
        document.add(field);
        return field;
    }

    /**
     * Sets type of the Document.
     */
    public Field setType(String type) {
        Field field = new Field(TYPE, type, Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets id of parent object.
     */
    public Field setParent(int parent) {
        Field field = new Field(PARENT, Integer.toString(parent), Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets class type and object id for fast retrieval of specific object from index
     */
    public Field setCid(String type, int id) {
        Field field = new Field(CID, type + id, Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets class type and object id for fast retrieval of specific object from index
     * @param obj object to be stored, its id must be set
     */
    public Field setCid(GenericObject obj) {
        String type = PersistenceMapping.getGenericObjectType(obj);
        Field field = new Field(CID, type + obj.getId(), Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets id of parent object.
     */
    public Field setNewsCategory(String category) {
        Field field = new Field(NEWS_CATEGORY, category, Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets whether the question was solved.
     */
    public Field setQuestionSolved(boolean solved) {
        Field field = new Field(QUESTION_SOLVED, solved? "ano":"ne", Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets numkber of replies in discussion.
     */
    public Field setNumberOfReplies(int replies) {
        return setNumberOfReplies(Integer.toString(replies));
    }

    /**
     * Sets numkber of replies in discussion.
     */
    public Field setNumberOfReplies(String replies) {
        Field field = new Field(NUMBER_OF_REPLIES, replies, Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets time when object was created.
     */
    public Field setCreated(Date date) {
        String t = DateTools.dateToString(date, DateTools.Resolution.DAY); // 20060731
        Field field = new Field(CREATED, t, Field.Store.YES, Field.Index.UN_TOKENIZED);
        document.add(field);
        return field;
    }

    /**
     * Sets last time when object was updated.
     */
    public Field setUpdated(Date date) {
        String t = DateTools.dateToString(date, DateTools.Resolution.DAY); // 20060731
        Field field = new Field(UPDATED, t, Field.Store.YES, Field.Index.UN_TOKENIZED);
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
