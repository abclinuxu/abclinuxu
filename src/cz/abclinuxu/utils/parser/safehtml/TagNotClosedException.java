/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 18:00:52
 */
package cz.abclinuxu.utils.parser.safehtml;

/**
 * The tag is not closed.
 */
public class TagNotClosedException extends Exception {
    public TagNotClosedException(String message) {
        super(message);
    }
}
