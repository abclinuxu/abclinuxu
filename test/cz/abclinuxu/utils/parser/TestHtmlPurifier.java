/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.utils.parser;

import junit.framework.TestCase;
import cz.abclinuxu.utils.parser.clean.HtmlPurifier;

/**
 * Verifies correctness.
 * @author literakl
 * @since 23.9.2008
 */
public class TestHtmlPurifier extends TestCase {

    public void testOrdinaryText() throws Exception {
        String s = "a\n\nb\n";
        String expected = "a\n<br><br>\nb\n";
        assertEquals("failed", expected, HtmlPurifier.clean(s));
    }

    public void testOrdinaryTextMoreEmptyLines() throws Exception {
        String s = "a\n\n\nb\n";
//        String expected = "a\n<br><br>\nb\n"; not implemented freedy behaviour
        String expected = "a\n<br><br>\n\nb\n";
        assertEquals("failed", expected, HtmlPurifier.clean(s));
    }

    public void testOrdinaryTextMultipleParagraphs() throws Exception {
        String s = "a\n\nb\nc\n\nd";
        String expected = "a\n<br><br>\nb\nc\n<br><br>\nd";
        assertEquals("failed", expected, HtmlPurifier.clean(s));
    }

    public void testOrdinaryTextWin() throws Exception {
        String s = "a\r\n\r\nb";
        String expected = "a\n<br><br>\nb";
        assertEquals("failed", expected, HtmlPurifier.clean(s));
    }

    public void testOrdinaryTextWithPre() throws Exception {
        String s = "a\n\nb\n<pre>c\n\nd</pre>e";
        String expected = "a\n<br><br>\nb\n<pre>c\n\nd</pre>e";
        assertEquals("failed", expected, HtmlPurifier.clean(s));
    }

    public void testMultiLineTag() throws Exception {
        String s = "a\n<p\n\n>b</p>\n";
        String expected = "a\n<p\n\n>b</p>\n";
        assertEquals("failed", expected, HtmlPurifier.clean(s));
    }
}
