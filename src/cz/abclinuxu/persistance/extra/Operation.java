/*
 * User: literakl
 * Date: 2.3.2004
 * Time: 19:22:18
 */
package cz.abclinuxu.persistance.extra;

/**
 * Constants for operations.
 */
public class Operation {
    public static final Operation SMALLER = new Operation("SMALLER");
    public static final Operation SMALLER_OR_EQUAL = new Operation("SMALLER_OR_EQUAL");
    public static final Operation GREATER = new Operation("GREATER");
    public static final Operation GREATER_OR_EQUAL = new Operation("GREATER_OR_EQUAL");
    public static final Operation EQUAL = new Operation("EQUAL");
    public static final Operation NOT_EQUAL = new Operation("NOT_EQUAL");
    public static final Operation LIKE = new Operation("LIKE");

    private final String myName; // for debug only

    private Operation(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
