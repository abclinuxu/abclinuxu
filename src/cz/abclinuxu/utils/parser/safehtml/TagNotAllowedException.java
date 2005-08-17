/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 18:02:11
 */
package cz.abclinuxu.utils.parser.safehtml;

/**
 * The tag is not allowed by portal policy.
 */
public class TagNotAllowedException extends HtmlCheckException {
    public TagNotAllowedException(String message) {
        super(message);
    }
}
