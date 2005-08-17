package cz.abclinuxu.utils.parser.safehtml;

/**
 * Parent for all exceptions from html checkers.
 * User: literakl
 * Date: 17.8.2005
 */
public class HtmlCheckException extends Exception {
    public HtmlCheckException() {
    }

    public HtmlCheckException(String message) {
        super(message);
    }

    public HtmlCheckException(Throwable cause) {
        super(cause);
    }

    public HtmlCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
