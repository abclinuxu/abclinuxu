/*
 * User: literakl
 * Date: 4.9.2002
 * Time: 9:51:28
 */
package cz.abclinuxu.utils.velocity;

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
}
