/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 18:01:30
 */
package cz.abclinuxu.utils.parser.safehtml;

/**
 * The portal policy forbids given (any) attribute.
 */
public class AttributeNotAllowedException extends HtmlCheckException {
    public AttributeNotAllowedException(String message) {
        super(message);
    }
}
