/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.utils;

public class InstanceUtils {

    /**
     * Compares two objects.
     * @return true, if both parameters are null or are equal.
     */
    public static boolean same(Object first, Object second) {
        if ( first!=null ) {
            if ( second==null ) return false;
            return first.equals(second);
        }
        return second==null;
    }
}
