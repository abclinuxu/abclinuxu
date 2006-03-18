/*
 *  Copyright (C) 2006 Leos Literak
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

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Date;

import cz.abclinuxu.data.XMLHandler;

/**
 * @author literakl
 * @since 18.2.2006
 */
public class RowComment extends Comment {
    int rowId;
    int record;
    Integer author;
    Date created;
    boolean _dirty;

    /**
     * Creates new instance of Discussion.
     */
    public RowComment() {
        children = new ArrayList(3);
    }

    /**
     * Creates new instance of Discussion.
     */
    public RowComment(Element comment) {
        this();
        documentHandler = new XMLHandler(comment);

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public void setParent(int parent) {
        this.parent = new Integer(parent);
    }

    public Integer getAuthor() {
        return author;
    }

    public Date getCreated() {
        return created;
    }

    public void setAuthor(int author) {
        this.author = new Integer(author);
    }

    public void setAuthor(Integer author) {
        this.author = author;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    /**
     * @return id of associated record. To be used only by persistence layer
     */
    public int getRecord() {
        return record;
    }

    /**
     * Sets id of associated record. To be used only by persistence layer.
     * @param record
     */
    public void setRecord(int record) {
        this.record = record;
    }

    /**
     * @return true if this instance is changed and it needs to be written to persistence
     */
    public boolean is_dirty() {
        return _dirty;
    }

    /**
     * Marks this comment as dirty, which instructs persistence to flush changes to database.
     * @param _dirty
     */
    public void set_dirty(boolean _dirty) {
        this._dirty = _dirty;
    }
}
