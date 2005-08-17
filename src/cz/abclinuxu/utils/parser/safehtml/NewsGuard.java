/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 16:35:01
 */
package cz.abclinuxu.utils.parser.safehtml;

import org.htmlparser.util.ParserException;

import java.util.*;

/**
 * This class is responsible for keeping news content
 * to contain minimal and valid HTML.
 */
public class NewsGuard {
    static final Map TAGS = new HashMap();
    static {
        TAGS.put("A", new CheckedTag("A", true, new String[]{"HREF"}));
        TAGS.put("CODE", new CheckedTag("CODE", true, null));
    }

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @throws TagNotAllowedException If tag is not allowed or recognized.
     * @throws TagNotClosedException If tag is not closed.
     * @throws AttributeNotAllowedException If attribute is not allowed.
     */
    public static void check(String s) throws HtmlCheckException, ParserException {
        TagValidator.check(s, TAGS);
    }
}
