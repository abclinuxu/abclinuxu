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
 * This class is responsible for keeping HTML content
 * to be safe. E.g. it will blocks malicious (or stupid) user's
 * input, that can harm portal's UI or XSS.
 */
public class SafeHTMLGuard {
    static final Map TAGS = new HashMap();
    static {
        TAGS.put("A", new CheckedTag("A", true, new String[]{"HREF", "TITLE", "CLASS", "NAME", "TARGET"}));
        TAGS.put("B", new CheckedTag("B", true, null));
        TAGS.put("BLOCKQUOTE", new CheckedTag("BLOCKQUOTE", true, new String[]{"ID"}));
        TAGS.put("BR", new CheckedTag("BR", false, null));
        TAGS.put("CITE", new CheckedTag("CITE", true, null));
        TAGS.put("CODE", new CheckedTag("CODE", true, null));
        TAGS.put("DEL", new CheckedTag("DEL", true, null));
        TAGS.put("DIV", new CheckedTag("DIV", true, new String[]{"ID", "CLASS"}));
        TAGS.put("EM", new CheckedTag("EM", true, null));
        TAGS.put("H1", new CheckedTag("H1", true, new String[]{"ID", "CLASS"}));
        TAGS.put("H2", new CheckedTag("H2", true, new String[]{"ID", "CLASS"}));
        TAGS.put("H3", new CheckedTag("H3", true, new String[]{"ID", "CLASS"}));
        TAGS.put("HR", new CheckedTag("HR", false, null));
        TAGS.put("I", new CheckedTag("I", true, null));
        TAGS.put("INS", new CheckedTag("INS", true, null));
        TAGS.put("LI", new CheckedTag("LI", false, null));
        TAGS.put("OL", new CheckedTag("OL", true, new String[]{"ID"}));
        TAGS.put("P", new CheckedTag("P", false, new String[]{"ID", "CLASS"}));
        TAGS.put("PRE", new CheckedTag("PRE", true, new String[]{"WIDTH", "ID", "CLASS"}));
        TAGS.put("Q", new CheckedTag("Q", true, new String[]{"ID"}));
        TAGS.put("STRONG", new CheckedTag("STRONG", true, null));
        TAGS.put("TT", new CheckedTag("TT", true, null));
        TAGS.put("U", new CheckedTag("U", true, null));
        TAGS.put("UL", new CheckedTag("UL", true, new String[]{"ID"}));
        TAGS.put("VAR", new CheckedTag("VAR", true, null));
    }

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotAllowedException If tag is not allowed or recognized.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotClosedException If tag is not closed.
     * @throws cz.abclinuxu.utils.parser.safehtml.AttributeNotAllowedException If attribute is not allowed.
     */
    public static void check(String s) throws HtmlCheckException, ParserException {
        TagValidator.check(s, TAGS);
    }
}
