/*
 * User: literakl
 * Date: 28.1.2004
 * Time: 21:11:39
 */
package cz.abclinuxu.utils.format;

/**
 * Holds text formats, that Render can understand.
 */
public class Format {
    /** Empty line is replaced with P tag, it may contain HTML tags except P, DIV and PRE. */
    public static final Format SIMPLE = new Format("SIMPLE",0);
    /** HTML-formatted text */
    public static final Format HTML = new Format("HTML",1);
    /** new proposed format similar to wiki */
    public static final Format WIKI = new Format("WIKI",2);

    private final String myName;
    private final int id;

    private Format(String name, int id) {
        myName = name;
        this.id = id;
    }

    public String toString() {
        return myName;
    }

    public int getId() {
        return id;
    }
}
