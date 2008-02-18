/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
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
        TAGS.put("ABBR", new CheckedTag("ABBR", true,  new String[]{"TITLE"}));
        TAGS.put("ACRONYM", new CheckedTag("ACRONYM", true, new String[]{"TITLE"}));
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
