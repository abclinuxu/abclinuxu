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
public class BlogHTMLGuard {
    static final Map TAGS = new HashMap(SafeHTMLGuard.TAGS);
    static final Map TAGS_PEREX = new HashMap(SafeHTMLGuard.TAGS);
    static {
        TAGS.put("IMG", new CheckedTag("IMG", false, new String[]{"SRC", "WIDTH", "HEIGHT", "BORDER", "ALT"}));
        
        for (int i = 1; i <= 5; i++)
            TAGS_PEREX.remove("H"+i);
        TAGS_PEREX.remove("B");
        TAGS_PEREX.remove("BIG");
        TAGS_PEREX.remove("STRONG");
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
    public static void checkPerex(String s) throws HtmlCheckException, ParserException {
        String lower = s.toLowerCase();
        if (lower.indexOf("<p") != lower.lastIndexOf("<p"))
            throw new HtmlCheckException("V perexu není povolen více než jeden odstavec!");
        if (lower.indexOf("<br") != lower.lastIndexOf("<br"))
            throw new HtmlCheckException("V perexu není povolen více než jeden tag BR!");
        
        TagValidator.check(s, TAGS_PEREX);
    }
}
