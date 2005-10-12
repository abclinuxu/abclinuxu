/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 16:35:01
 */
package cz.abclinuxu.utils.parser.safehtml;

import org.htmlparser.util.ParserException;

import java.util.*;

/**
 * This class is responsible for keeping HTML content in user's profile
 * to be safe. It blocks XSS and invalid HTML constructs.
 * <p>
 * These tags are allowed:
 * p, br, li (no attributes)
 * ul, ol, b, i, code, pre, h1, h2, h3 (no attributes, must be closed)
 * div (attribute style)
 * a (attributes href and target, must be closed)
 */
public class ProfileGuard {
    static final Map TAGS = new HashMap();
    static {
        TAGS.put("B", new CheckedTag("B", true, null));
        TAGS.put("I", new CheckedTag("I", true, null));
        TAGS.put("P", new CheckedTag("P", false, null));
        TAGS.put("BR", new CheckedTag("BR", false, null));
        TAGS.put("A", new CheckedTag("A", true, new String[]{"HREF","TARGET", "TITLE", "CLASS", "NAME"}));
        TAGS.put("PRE", new CheckedTag("PRE", true, new String[]{"WIDTH"}));
        TAGS.put("LI", new CheckedTag("LI", false, null));
        TAGS.put("UL", new CheckedTag("UL", true, null));
        TAGS.put("OL", new CheckedTag("OL", true, null));
        TAGS.put("CODE", new CheckedTag("CODE", true, null));
        TAGS.put("DIV", new CheckedTag("DIV", true, new String[]{"STYLE"}));
        TAGS.put("H1", new CheckedTag("H1", true, null));
        TAGS.put("H2", new CheckedTag("H2", true, null));
        TAGS.put("H3", new CheckedTag("H3", true, null));
        TAGS.put("EM", new CheckedTag("EM", true, null));
        TAGS.put("STRONG", new CheckedTag("STRONG", true, null));
        TAGS.put("CITE", new CheckedTag("CITE", true, null));
        TAGS.put("HR", new CheckedTag("HR", false, null));
    }

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @throws TagNotAllowedException If tag is not allowed or recognized.
     * @throws TagNotClosedException If tag is not closed.
     * @throws AttributeNotAllowedException If attribute is not allowed.
     */
    public static void check(String s) throws ParserException, HtmlCheckException {
        TagValidator.check(s, TAGS);
    }
}
