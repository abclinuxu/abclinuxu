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

import cz.abclinuxu.data.Relation;

import java.text.Collator;

/**
 * User's bookmark item.
 * @author literakl
 * @since 7.8.2007
 */
public class Bookmark implements Comparable {
    String title, prefix, type;
    Relation relation;

    public Bookmark(Relation relation, String title, String prefix, String type) {
        this.relation = relation;
        this.title = title;
        this.prefix = prefix;
        this.type = type;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation r) {
        this.relation = r;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // todo use Collator instead
    public int compareTo(Object obj) {
        Bookmark that = (Bookmark) obj;
        return Collator.getInstance().compare(title, that.title);
    }
}
