/*
 * User: literakl
 * Date: 21.12.2003
 * Time: 13:56:31
 */
package cz.abclinuxu.persistance;

/**
 * Definition of behaviour of SQL commands or Persistance calls.
 */
public class Qualifier {
    public static final Qualifier SORT_BY_CREATED = new Qualifier("SORT_BY_CREATED");
    public static final Qualifier SORT_BY_UPDATED = new Qualifier("SORT_BY_UPDATED");
    public static final Qualifier SORT_BY_ID = new Qualifier("SORT_BY_ID");
    public static final Qualifier ORDER_ASCENDING = new Qualifier("ASCENDING_ORDER");
    public static final Qualifier ORDER_DESCENDING = new Qualifier("DESCENDING_ORDER");

    private final String name;

    protected Qualifier(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object obj) {
        if (! (obj instanceof Qualifier)) return false;
        return ((Qualifier)obj).name.equals(name);
    }

    public int hashCode() {
        return name.hashCode();
    }
}
