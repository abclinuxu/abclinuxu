/*
 *  Copyright (C) 2006 Leos Literak
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

import java.util.Collections;

/**
 * This guard forbids any HTML tags. To be used for texts
 * supposed not to hold HTML, e.g. user names etc. Especially
 * neccessary for XSS protection.
 * @author literakl
 * @since 2.5.2006
 */
public class NoHTMLGuard {

    /**
     * Performs check of html string.
     *
     * @param s html to be checked.
     * @throws TagNotAllowedException       If tag is not allowed or recognized.
     * @throws TagNotClosedException        If tag is not closed.
     * @throws AttributeNotAllowedException If attribute is not allowed.
     */
    public static void check(String s) throws HtmlCheckException, ParserException {
        TagValidator.check(s, Collections.EMPTY_MAP);
    }
}
