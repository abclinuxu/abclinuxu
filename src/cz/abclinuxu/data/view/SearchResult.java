/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.data.view;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DateTools;

import java.util.Date;
import java.text.ParseException;

import cz.abclinuxu.utils.search.MyDocument;

/**
 * This class holds one result from lucene search.
 * @author literakl
 * @since 9.4.2007
 */
public class SearchResult {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SearchResult.class);

    private Document document;
    private Date created, modified;
    private String highlightedText;

    public SearchResult(Document document) {
        this.document = document;
        String date = document.get(MyDocument.CREATED);
        try {
            if (date != null)
                created = DateTools.stringToDate(date);
            date = document.get(MyDocument.UPDATED);
            if (date != null)
                modified = DateTools.stringToDate(date);
        } catch (ParseException e) {
            log.warn("Failed to convert date '" + date + "' from index for " + document.get(MyDocument.CID) + "!", e);
        }
    }

    /**
     * Gets Field value from document.
     * @param key key specified in MyDocument. It must have been stored in index.
     * @return field value or null
     */
    public String get(String key) {
        return document.get(key);
    }

    /**
     * @return original lucene document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @return date when this indexed object was created or null, if unset
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @return date when this indexed object was last modified or null, if unset
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @return fragment of indexed object content with highlighted searched query
     */
    public String getHighlightedText() {
        return highlightedText;
    }

    /**
     * Sets fragment of indexed object content with highlighted searched query
     * @param highlightedText fragment
     */
    public void setHighlightedText(String highlightedText) {
        this.highlightedText = highlightedText;
    }
}
