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

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.text.Collator;

import org.dom4j.Node;

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

    /**
     * This comparator sorts relations by their name
     * in ascending order.
     */
    static class NameComparator implements Comparator {
        Collator collator = Collator.getInstance();

        public int compare(Object o1, Object o2) {
            String s1 = getValue((Relation) o1,"data/name").toLowerCase();
            String s2 = getValue((Relation) o2,"data/name").toLowerCase();
            return collator.compare(s1, s2);
        }
        /**
         * Extracts value of relation.
         */
        private String getValue(Relation r, String xpath) {
            String s = Tools.xpath(r,xpath);
            if ( s==null ) {
                s = Tools.xpath(r.getChild(),xpath);
                if ( s==null ) s = "";
            }
            return s;
        }
    }

    /**
     * This comparator sorts GenericObjects by their
     * modified property in ascending order. If GenericObject
     * is an relation, then its child is compared.
     */
    static class DateComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Date d1 = getValue(o1);
            Date d2 = getValue(o2);
            return d1.compareTo(d2);
        }
        /**
         * Extracts date value of GenericObject.
         */
        private Date getValue(Object obj) {
            if ( obj instanceof Relation )
                obj = ((Relation)obj).getChild();

            if ( obj instanceof Link )
                return ((Link)obj).getUpdated();
            if ( obj instanceof DiscussionHeader )
                return ((DiscussionHeader)obj).getUpdated();
            if ( obj instanceof GenericDataObject ) {
                GenericDataObject gdo = (GenericDataObject) obj;
                if ( gdo instanceof Item && gdo.getType()==Item.ARTICLE )
                    return gdo.getCreated();
                else
                    return gdo.getUpdated();
            }
            if ( obj instanceof Poll )
                return ((Poll)obj).getCreated();

            log.warn("Don't know how to handle "+obj);
            return new Date(0);
        }
    }

    /**
     * This comparator sorts GenericObjects by their id
     * in ascending order.
     */
    static class IdComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            int i = ((GenericObject) o1).getId();
            int j = ((GenericObject) o2).getId();
            return i-j;
        }
    }

    /**
     * This comparator sorts relations containg GenericDataObject by specified xpath.
     */
    static class XPathComparator implements Comparator {
        String xpath;

        public XPathComparator(String xpath) {
            this.xpath = xpath;
        }

        public int compare(Object o1, Object o2) {
            GenericDataObject i1 = (GenericDataObject) ((Relation) o1).getChild();
            GenericDataObject i2 = (GenericDataObject) ((Relation) o2).getChild();

            Node n1 = i1.getData().selectSingleNode(xpath);
            Node n2 = i2.getData().selectSingleNode(xpath);

            if (n1 == null) {
                if (n2 == null)
                    return 0;
                else
                    return -1;
            }
            if (n2 == null)
                return 1;

            String s1 = n1.getText();
            String s2 = n2.getText();
            return s1.compareTo(s2);
        }
    }
}
