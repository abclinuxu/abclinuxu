/*
 * User: Leos Literak
 * Date: Jan 5, 2003
 * Time: 7:17:39 PM
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.*;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.List;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;

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
     * @return list of GenericObjects sorted in ascending order by their date.
     */
    public static List byDate(List relations) {
        return byDate(relations,ASCENDING);
    }

    /**
     * @return list of GenericObjects sorted by their date in specified order.
     */
    public static List byDate(List relations, String order) {
        Comparator comparator = new DateComparator();
        if ( order.equals(DESCENDING) )
            comparator = new OpaqueComparator(comparator);
        Collections.sort(relations,comparator);
        return relations;
    }

    /**
     * @return list of GenericObjects sorted in ascending order by their id.
     */
    public static List byId(List relations) {
        return byId(relations,ASCENDING);
    }

    /**
     * @return list of GenericObjects sorted by their id in specified order.
     */
    public static List byId(List relations, String order) {
        Comparator comparator = new IdComparator();
        if ( order.equals(DESCENDING) )
            comparator = new OpaqueComparator(comparator);
        Collections.sort(relations,comparator);
        return relations;
    }

    /**
     * This comparator sorts relations by their name
     * in ascending order.
     */
    static class NameComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String s1 = getValue((Relation) o1,"data/name");
            String s2 = getValue((Relation) o2,"data/name");
            return s1.compareToIgnoreCase(s2);
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
            int i = getValue((GenericObject) o1);
            int j = getValue((GenericObject) o2);
            return i-j;
        }
        /**
         * Extracts value of GenericObjects.
         */
        private int getValue(GenericObject obj) {
            if ( obj instanceof Relation )
                obj = ((Relation)obj).getChild();
            return obj.getId();
        }
    }

    /**
     * This comparator reverses order of supplied comparator.
     */
    static class OpaqueComparator implements Comparator {
        Comparator comparator;

        public OpaqueComparator(Comparator comparator) {
            this.comparator = comparator;
        }

        public int compare(Object o1, Object o2) {
            int result = comparator.compare(o2,o1);
            return result;
        }
    }
}
