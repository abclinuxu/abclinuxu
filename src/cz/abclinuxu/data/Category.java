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
import org.dom4j.Element;
import org.dom4j.Document;

/**
 * Category is a node of the tree
 */
public class Category extends GenericDataObject {

    /** section that can hold hardware entries */
    public static final int HARDWARE_SECTION = 1;
    /** mark for forum */
    public static final int FORUM = 2;
    /** marks section containing blogs of the user */
    public static final int BLOG = 3;
    /** section holds articles */
    public static final int SECTION = 4;
    /** section that holds FAQ entries */
    public static final int FAQ = 5;
    /** section that holds software entries */
    public static final int SOFTWARE_SECTION = 6;
	/** subportal governed by a special interest-group */
	public static final int SUBPORTAL = 7;
    /** section of events */
    public static final int EVENT = 8;


    public Category() {
        super();
    }

    public Category(int id) {
        super(id);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Category: id=").append(id);
        sb.append(", type=").append(type);
        sb.append(": id=").append(id);
        if (title != null)
            sb.append(", title=").append(title);
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
