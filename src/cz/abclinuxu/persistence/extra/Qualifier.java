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
package cz.abclinuxu.persistence.extra;

/**
 * Definition of behaviour of SQL commands or Persistance calls.
 */
public class Qualifier {
    public static final Qualifier SORT_BY_CREATED = new OrderByQualifier("CREATED", Field.CREATED);
    public static final Qualifier SORT_BY_UPDATED = new OrderByQualifier("UPDATED", Field.UPDATED);
    public static final Qualifier SORT_BY_WHEN = new OrderByQualifier("WHEN", Field.WHEN);
    public static final Qualifier SORT_BY_TITLE = new OrderByQualifier("TITLE", Field.TITLE);
    public static final Qualifier SORT_BY_ID = new OrderByQualifier("ID", Field.ID);
    public static final Qualifier SORT_BY_STRING1 = new OrderByQualifier("STRING1", Field.STRING1);
    public static final Qualifier SORT_BY_STRING2 = new OrderByQualifier("STRING2", Field.STRING2);
    
    public static final Qualifier ORDER_ASCENDING = new Qualifier("ORDER_ASCENDING");
    public static final Qualifier ORDER_DESCENDING = new Qualifier("ORDER_DESCENDING");

    protected final String name;

    protected Qualifier(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Qualifier)
            return ((Qualifier)obj).name.equals(name);
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }
}
