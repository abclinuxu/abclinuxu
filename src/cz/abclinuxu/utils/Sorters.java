/*
 * User: literakl
 * Date: Jan 29, 2002
 * Time: 3:04:03 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.*;

import java.util.*;
import java.io.Serializable;

/**
 * A lot of sorts.
 */
public class Sorters {

    /**
     * Sorts list of objects by date. If objects are relations, then their children are
     * compared. Objects shall be initialized to provide valid results!<p>
     * This comparator is not consistent with equals.
     * @param <code>ascendingOrder</code> Set it to true, if you wish list to be
     * sorted in ascending order.
     */
    public static void sortByDate(List objects, boolean ascendingOrder) {
        Collections.sort(objects, new DateComparator(ascendingOrder));
    }

    /**
     * Comparator for GenericObjects, which compares them by Updated or Created
     * field. If object doesn't define updated field or it is equal to null,
     * it is considered as more new.
     */
    static class DateComparator implements Comparator, Serializable {
        boolean ascendingOrder;

        public DateComparator(boolean ascendingOrder) {
            this.ascendingOrder = ascendingOrder;
        }

        public int compare(Object o1, Object o2) {
            GenericObject a = (GenericObject) o1, b = (GenericObject) o2;
            if ( a instanceof Relation ) a = ((Relation)a).getChild();
            if ( b instanceof Relation ) b = ((Relation)b).getChild();

            if ( a==null ) return signum(true); // unnecessary check, just for sure
            if ( b==null ) return signum(false);

            Date d1 = getDate(a);
            if ( d1==null ) return signum(false);
            Date d2 = getDate(b);
            if ( d2==null ) return signum(true);

            if ( d1.before(d2) ) return signum(true);
            if ( d2.before(d1) ) return signum(false);
            return 0;
        }

        /**
         * Helper method, which follows user's requested order.<p>
         * <table>
         * <tr><tr>ascendingOrder</tr><td>true</td><td>false</td></tr>
         * <tr>older is true<td></td><td>-1</td><td>+1</td></tr>
         * <tr>older is false<td></td><td>+1</td><td>-1</td></tr>
         * </table>
         */
        int signum(boolean firstIsOlder) {
            if ( ascendingOrder ) {
                return ( firstIsOlder )? -1:1;
            } else {
                return ( firstIsOlder )? 1:-1;
            }
        }

        Date getDate(GenericObject obj) {
            if ( obj instanceof GenericDataObject ) return ((GenericDataObject)obj).getUpdated();
            if ( obj instanceof Link ) return ((Link)obj).getUpdated();
            if ( obj instanceof Poll ) return ((Poll)obj).getCreated();
            return null;
        }
    }
}
