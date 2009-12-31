package cz.abclinuxu.data;

/**
 * Enums for roles in edition administration
 * User: literakl
 * Date: 28.12.2009
 */
public enum EditionRole {
    NONE, // no luck, get away!
    AUTHOR, // can write articles and manage own data
    EDITOR, // can manage content except money
    EDITOR_IN_CHIEF // can manage everything
}
