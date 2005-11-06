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
package cz.abclinuxu.data;

import cz.abclinuxu.utils.Misc;

/**
 * Category is a node of the tree
 */
public class Category extends GenericDataObject {

    /** normal category, only admin can insert content */
    public static final int CLOSED_CATEGORY = 0;
    /** normal category, every logged user can insert content */
    public static final int OPEN_CATEGORY = 1;
    /** mark for forum */
    public static final int FORUM = 2;
    /** marks section containing blogs of the user */
    public static final int BLOG = 3;
    /** section holds articles */
    public static final int SECTION = 4;
    /** section that holds FAQ entries */
    public static final int FAQ = 5;


    public Category() {
        super();
    }

    public Category(int id) {
        super(id);
    }

    /**
     * @return whether normal users may add content to this category
     */
    public boolean isOpen() {
        return type==OPEN_CATEGORY;
    }

    /**
     * sets whether normal users may add content to this category
     */
    public void setOpen(boolean open) {
        type = (open)? OPEN_CATEGORY:CLOSED_CATEGORY;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Category: id="+id);
        if ( owner!=0 ) sb.append(",owner="+owner);
        if ( documentHandler!=null ) sb.append(",data="+getDataAsString());
        if ( isOpen() ) sb.append(", otevrena"); else sb.append(", uzavrena");
        return sb.toString();
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof Category) ) return false;
        if ( id!=((GenericObject)o).getId() ) return false;
        if ( type!=((GenericDataObject)o).type ) return false;
        if ( owner!=((GenericDataObject)o).owner ) return false;
        if ( ! Misc.same(getDataAsString(),((GenericDataObject)o).getDataAsString()) ) return false;
        return true;
    }

    public int hashCode() {
        String tmp = "Category"+id;
        return tmp.hashCode();
    }
}
