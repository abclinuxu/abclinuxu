/*
 * User: literakl
 * Date: 4.9.2002
 * Time: 9:51:28
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.GenericObject;

import java.util.*;

/**
 * Miscallenous utilities.
 */
public class Misc {

    /**
     * Parses string into int. If str cannot be parsed
     * for any reason, it returns the second parameter.
     * @param str String to be parsed, may be null.
     * @param def Default value to be returned, if str is not integer
     */
    public static int parseInt(String str, int def) {
        if ( str==null || str.length()==0 ) return def;
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Ensures, that integer fits into specified range.
     * @param x variable to be checked
     * @param min lower limit
     * @param max upper limit
     * @return x, if min &gt;= x &lt;= max<br>
     * min, if x &lt; min<br>
     * max, if x &gt; max
     */
    public static int limit(int x, int min, int max) {
        if ( x<min ) return min;
        if ( x>max ) return max;
        return x;
    }

    /**
     * Associates value with given key in the map. Each key contains
     * list of values. If the list doesn't exist yet, it is created.
     */
    public static void storeToMap(Map map, String key, Object value) {
        List list = (List) map.get(key);
        if ( list==null ) {
            list = new ArrayList(5);
            map.put(key,list);
        }
        list.add(value);
    }

    /**
     * Finds out, whether string is empty.
     * @return true, if s is null or zero length
     */
    public static boolean empty(String s) {
        return ( s==null || s.length()==0 );
    }

    /**
     * Finds out, whether list is empty.
     * @return true, if list is null or zero length
     */
    public static boolean empty(List list) {
        return ( list==null || list.size()==0 );
    }

    /**
     * Compares two string for equality
     */
    public static boolean same(String a, String b) {
        if ( a==null ) {
            return (b==null);
        }
        return a.equals(b);
    }
}
