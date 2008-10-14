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
package cz.abclinuxu.utils;

import cz.abclinuxu.utils.comparator.OpaqueComparator;
import cz.abclinuxu.utils.comparator.DateComparator;
import cz.abclinuxu.utils.comparator.IdComparator;
import cz.abclinuxu.utils.comparator.NameComparator;
import cz.abclinuxu.utils.comparator.XPathComparator;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;

/**
 * This class provides several methods for sorting relations
 * based on some criteria and order. Refactoring of Sorters.
 */
public class Sorters2 {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Sorters2.class);

    /** ascending order */
    public static final String ASCENDING = "ASCENDING";
    /** descending order */
    public static final String DESCENDING = "DESCENDING";

    /**
     * @return list of relations sorted in ascending order by their name.
     */
    public static List byName(List relations) {
        return byName(relations,ASCENDING);
    }

    /**
     * @return list of relations sorted by their name in specified order.
     */
    public static List byName(List relations, String order) {
        Comparator comparator = new NameComparator();
        if ( order.equals(DESCENDING) )
            comparator = new OpaqueComparator(comparator);
        Collections.sort(relations,comparator);
        return relations;
    }

    /**
     * @return list of relations (where child is GenericDatObject) sorted
     * in ascending order by specified xpath.
     */
    public static List byXPath(List relations, String  xpath) {
        return byXPath(relations, xpath, ASCENDING);
    }

    /**
     * @return list of relations (where child is GenericDatObject) sorted
     * in given order by specified xpath.
     */
    public static List byXPath(List relations, String xpath, String order) {
        Comparator comparator = new XPathComparator(xpath);
        if ( order.equals(DESCENDING) )
            comparator = new OpaqueComparator(comparator);
        Collections.sort(relations, comparator);
        return relations;
    }

    /**
     * @return list of GenericObjects sorted in ascending order by their date.
     */
    public static List byDate(List objects) {
        return byDate(objects,ASCENDING);
    }

    /**
     * @return list of GenericObjects sorted by their date in specified order.
     */
    public static List byDate(List objects, String order) {
        Comparator comparator = new DateComparator();
        if ( order.equals(DESCENDING) )
            comparator = new OpaqueComparator(comparator);
        Collections.sort(objects,comparator);
        return objects;
    }

    /**
     * @return list of GenericObjects sorted in ascending order by their id.
     */
    public static List byId(List objects) {
        return byId(objects,ASCENDING);
    }

    /**
     * @return list of GenericObjects sorted by their id in specified order.
     */
    public static List byId(List objects, String order) {
        Comparator comparator = new IdComparator();
        if ( order.equals(DESCENDING) )
            comparator = new OpaqueComparator(comparator);
        Collections.sort(objects,comparator);
        return objects;
    }
}
