/*
 * User: literakl
 * Date: 5.4.2004
 * Time: 19:52:48
 */
package cz.abclinuxu.utils.parser.safehtml;

/**
 * Representation of tag and its policy.
 */
class CheckedTag {
    /** uper case tag name */
    final String name;
    /** whether this tag must be closed */
    final boolean mustBeClosed;
    /** array of allowed attributes */
    final String[] attributes;

    public CheckedTag(String name, boolean mustBeClosed, String[] attributes) {
        this.name = name;
        this.mustBeClosed = mustBeClosed;
        this.attributes = attributes;
    }
}
