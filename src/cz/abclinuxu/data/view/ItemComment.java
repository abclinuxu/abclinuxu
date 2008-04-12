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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.XMLHandler;

import java.util.Date;

/**
 * This class can initialize Comment from Item
 * (typically the question the forum).
 * @author literakl
 * @since 18.2.2006
 */
public class ItemComment extends Comment {
    // todo je to dobry napad delat z toho fasadu?
    Item item;

    /**
     * Creates facade of comment arround Item - typically question.
     */
    public ItemComment(Item item) {
        this.item = item;
        documentHandler = new XMLHandler(item.getData());
        id = 0;
        parent = null;
    }

    public void setId(int id) {
        this.id = 0;
    }

    public String getTitle() {
        return item.getTitle();
    }

    public void setTitle(String title) {
        item.setTitle(title);
    }

    /**
     * @return id of an author, or null, if he was anonymous
     */
    public Integer getAuthor() {
        return item.getOwner() == 0 ? null : new Integer(item.getOwner());
    }

    public void setAuthor(Integer author) {
        if (author != null)
            item.setOwner(author.intValue());
        else
            item.setOwner(0);
    }

    public void setParent(Integer parent) {
        this.parent = null;
    }

    public Date getCreated() {
        return item.getCreated();
    }

    public void setCreated(Date created) {
        item.setCreated(created);
    }
}
