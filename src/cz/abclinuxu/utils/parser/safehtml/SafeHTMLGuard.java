/*
 * User: literakl
 * Date: 4.4.2004
 * Time: 16:35:01
 */
package cz.abclinuxu.utils.parser.safehtml;

import org.htmlparser.util.ParserException;

import java.util.*;

/**
 * This class is responsible for keeping HTML content
 * to be safe. E.g. it will blocks malicious (or stupid) user's
 * input, that can harm portal's UI or XSS.
 * <p>
 * These tags are allowed:
 * p, br, li (no attributes)
 * ul, ol, b, i, code, pre, div, h1, h2, h3 (no attributes, must be closed)
 * a (attribute href, must be closed)
 */
public class SafeHTMLGuard {
    static final Map TAGS = new HashMap();
    static {
        TAGS.put("B", new CheckedTag("B", true, null));
        TAGS.put("I", new CheckedTag("I", true, null));
        TAGS.put("P", new CheckedTag("P", false, null));
        TAGS.put("BR", new CheckedTag("BR", false, null));
        TAGS.put("A", new CheckedTag("A", true, new String[]{"HREF"}));
        TAGS.put("PRE", new CheckedTag("PRE", true, new String[]{"WIDTH"}));
        TAGS.put("LI", new CheckedTag("LI", false, null));
        TAGS.put("UL", new CheckedTag("UL", true, null));
        TAGS.put("OL", new CheckedTag("OL", true, null));
        TAGS.put("CODE", new CheckedTag("CODE", true, null));
        TAGS.put("DIV", new CheckedTag("DIV", true, null));
        TAGS.put("H1", new CheckedTag("H1", true, null));
        TAGS.put("H2", new CheckedTag("H2", true, null));
        TAGS.put("H3", new CheckedTag("H3", true, null));
        TAGS.put("EM", new CheckedTag("EM", true, null));
        TAGS.put("STRONG", new CheckedTag("STRONG", true, null));
        TAGS.put("CITE", new CheckedTag("CITE", true, null));
        TAGS.put("BLOCKQUOTE", new CheckedTag("BLOCKQUOTE", true, null));
        TAGS.put("VAR", new CheckedTag("VAR", true, null));
        TAGS.put("HR", new CheckedTag("HR", false, null));
    }

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotAllowedException If tag is not allowed or recognized.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotClosedException If tag is not closed.
     * @throws cz.abclinuxu.utils.parser.safehtml.AttributeNotAllowedException If attribute is not allowed.
     */
    public static void check(String s) throws TagNotAllowedException, TagNotClosedException, AttributeNotAllowedException, ParserException {
        TagValidator.check(s, TAGS);
    }
}
