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
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.persistence.PersistenceMapping;

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
    // todo pouzit, asi
    public static final String TYPE_QUESTION = "otazka";
    public static final String TYPE_DRIVER = "ovladac";
    public static final String TYPE_ARTICLE = "clanek";
    public static final String TYPE_NEWS = "zpravicka";
    public static final String TYPE_POLL = "anketa";
    public static final String TYPE_DICTIONARY = "pojem";
    public static final String TYPE_FAQ = "faq";
    public static final String TYPE_BLOG = "blog";
    public static final int ALL_TYPES_COUNT = 9;

    /** Object's text to be tokenized and indexed. */
    public static final String CONTENT = "obsah";
    /** Type of the object. */
    public static final String TYPE = "typ";
    /** URL, where to display the object. */
    public static final String VALUE_URL = "url";
    /** User friendly title. */
    public static final String TITLE = "title";
    /** Id of parent object. */
    public static final String PARENT = "parent";
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
    /** stored value in ISO format of time when object was created */
    public static final String VALUE_CREATED = "datum_vytvoreni";
    /** stored value in ISO format of time when object was modified last time */
    public static final String VALUE_UPDATED = "datum_zmeny";
    /** stored value - number of characters of indexed content */
    public static final String VALUE_SIZE = "velikost_obsahu";

    Document document;

    /**
     * Creates Documents and fills it with String to be indexed.
     */
    public MyDocument(String content) {
        document = new Document();
        document.add(Field.Text(CONTENT, content));
        document.add(Field.UnIndexed(VALUE_SIZE, Integer.toString(content.length())));
    }

    /**
     * Associates URL with Document.
     */
    public Field setURL(String url) {
        Field field = Field.UnIndexed(VALUE_URL,url);
        document.add(field);
        return field;
    }

    /**
     * Sets title for the Document.
     */
    public Field setTitle(String title) {
        title = Tools.removeTags(title);
        Field field = Field.Text(TITLE,title);
        document.removeField(TITLE);
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
        Field field = Field.Keyword(PARENT, Integer.toString(parent));
        document.add(field);
        return field;
    }

    /**
     * Sets class type and object id for fast retrieval of specific object from index
     */
    public Field setCid(String type, int id) {
        Field field = Field.Keyword(CID, type + id);
        document.add(field);
        return field;
    }

    /**
     * Sets class type and object id for fast retrieval of specific object from index
     * @param obj object to be stored, its id must be set
     */
    public Field setCid(GenericObject obj) {
        String type = PersistenceMapping.getGenericObjectType(obj);
        Field field = Field.Keyword(CID, type + obj.getId());
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
        String s = Constants.isoFormat.format(date); // 2006-07-31 12:10
        String t = Constants.isoSearchFormat.format(date); // 20060731
        document.add(Field.UnIndexed(VALUE_CREATED, s));
        Field field = Field.Keyword(CREATED, t);
        document.add(field);
        return field;
    }

    /**
     * Sets last time when object was updated.
     */
    public Field setUpdated(Date date) {
        String s = Constants.isoFormat.format(date); // 2006-07-31 12:10
        String t = Constants.isoSearchFormat.format(date); // 20060731
        document.add(Field.UnIndexed(VALUE_UPDATED, s));
        Field field = Field.Keyword(UPDATED, t);
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
