/*
 * User: literakl
 * Date: 2.3.2004
 * Time: 19:17:48
 */
package cz.abclinuxu.persistance.extra;

/**
 * Constants for database columns.
 */
public class Field {
    public static final Field CREATED = new Field("CREATED");
    public static final Field UPDATED = new Field("UPDATED");
    public static final Field ID = new Field("ID");
    public static final Field DATA = new Field("DATA");
    public static final Field TYPE = new Field("TYPE");
    public static final Field SUBTYPE = new Field("SUBTYPE");
    public static final Field OWNER = new Field("OWNER");

    private final String myName; // for debug only

    private Field(String name) {
        myName = name;
    }

    public String toString() {
        return myName;
    }
}
