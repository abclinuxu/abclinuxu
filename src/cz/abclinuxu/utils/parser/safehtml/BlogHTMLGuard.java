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
 */
public class BlogHTMLGuard {
    static final Map TAGS = new HashMap();
    static {
        TAGS.put("P", new CheckedTag("P", false, new String[]{"ID", "CLASS"}));
        TAGS.put("BR", new CheckedTag("BR", false, null));
        TAGS.put("PRE", new CheckedTag("PRE", true, new String[]{"WIDTH", "ID", "CLASS"}));
        TAGS.put("DIV", new CheckedTag("DIV", true, new String[]{"ID", "CLASS"}));
        TAGS.put("SPAN", new CheckedTag("SPAN", true, new String[]{"ID", "CLASS"}));
        TAGS.put("H1", new CheckedTag("H1", true, new String[]{"ID", "CLASS"}));
        TAGS.put("H2", new CheckedTag("H2", true, new String[]{"ID", "CLASS"}));
        TAGS.put("H3", new CheckedTag("H3", true, new String[]{"ID", "CLASS"}));
        TAGS.put("H4", new CheckedTag("H4", true, new String[]{"ID", "CLASS"}));
        TAGS.put("A", new CheckedTag("A", true, new String[]{"HREF", "TITLE", "ID", "CLASS"}));
        // todo probably make it optional. available only in full blog display, not in perex
        TAGS.put("IMG", new CheckedTag("IMG", true, new String[]{"SRC", "WIDTH", "HEIGHT", "BORDER"}));
        TAGS.put("B", new CheckedTag("B", true, null));
        TAGS.put("I", new CheckedTag("I", true, null));
        TAGS.put("U", new CheckedTag("U", true, null));
        TAGS.put("CENTER", new CheckedTag("CENTER", true, null));
        TAGS.put("LI", new CheckedTag("LI", false, null));
        TAGS.put("UL", new CheckedTag("UL", true,  new String[]{"ID"}));
        TAGS.put("OL", new CheckedTag("OL", true, new String[]{"ID"}));
        TAGS.put("DL", new CheckedTag("DL", true, new String[]{"ID"}));
        TAGS.put("DT", new CheckedTag("DT", true, new String[]{"ID"}));
        TAGS.put("DD", new CheckedTag("DD", true, new String[]{"ID"}));
        TAGS.put("TABLE", new CheckedTag("TABLE", true, null));
        TAGS.put("TBODY", new CheckedTag("TBODY", true, null));
        TAGS.put("THEAD", new CheckedTag("THEAD", true, null));
        TAGS.put("TFOOT", new CheckedTag("TFOOT", true, null));
        TAGS.put("TR", new CheckedTag("TR", true, null));
        TAGS.put("TD", new CheckedTag("TD", true, null));
        TAGS.put("TH", new CheckedTag("TH", true, null));
        TAGS.put("CODE", new CheckedTag("CODE", true, null));
        TAGS.put("STRONG", new CheckedTag("STRONG", true, null));
        TAGS.put("KBD", new CheckedTag("KBD", true, null));
        TAGS.put("DEL", new CheckedTag("DEL", true, null));
        TAGS.put("INS", new CheckedTag("INS", true, null));
        TAGS.put("CITE", new CheckedTag("CITE", true, null));
        TAGS.put("EM", new CheckedTag("EM", true, null));
        TAGS.put("BLOCKQUOTE", new CheckedTag("BLOCKQUOTE", true, new String[]{"ID"}));
        TAGS.put("Q", new CheckedTag("Q", true, new String[]{"ID"}));
        TAGS.put("VAR", new CheckedTag("VAR", true, null));
        TAGS.put("HR", new CheckedTag("HR", false, null));
        TAGS.put("TT", new CheckedTag("TT", true, null));
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
