package cz.abclinuxu.utils.parser.safehtml;

import java.util.HashMap;
import java.util.Map;

import org.htmlparser.util.ParserException;

/**
 * This class is responsible for keeping HTML content in user's signature
 * to be safe. It blocks XSS and invalid HTML constructs.
 */
public class SignatureHTMLGuard {
    static final Map TAGS = new HashMap();
    static {
        TAGS.put("A", new CheckedTag("A", true, new String[]{"HREF","TARGET", "TITLE", "NAME", "REL"}));
        TAGS.put("ABBR", new CheckedTag("ABBR", true,  new String[]{"TITLE"}));
        TAGS.put("ACRONYM", new CheckedTag("ACRONYM", true, new String[]{"TITLE"}));
        TAGS.put("CITE", new CheckedTag("CITE", true, null));
        TAGS.put("CODE", new CheckedTag("CODE", true, null));
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
