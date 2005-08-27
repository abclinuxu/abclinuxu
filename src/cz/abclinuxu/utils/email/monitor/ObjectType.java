/*
 * User: literakl
 * Date: 7.11.2003
 * Time: 8:17:07
 */
package cz.abclinuxu.utils.email.monitor;

/**
 * Enumaration of available objects, that can be monitored.
 */
public class ObjectType {
    private final String name;

    public static final ObjectType DISCUSSION = new ObjectType("discussion");
    public static final ObjectType DRIVER = new ObjectType("driver");
    public static final ObjectType ITEM = new ObjectType("item");
    public static final ObjectType DICTIONARY = new ObjectType("dictionary");
    public static final ObjectType FAQ = new ObjectType("faq");
    public static final ObjectType CONTENT = new ObjectType("content");

    private ObjectType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
