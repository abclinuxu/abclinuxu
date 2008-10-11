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
package cz.abclinuxu.utils;

import junit.framework.TestCase;
import cz.abclinuxu.utils.format.HtmlToTextFormatter;

/**
 * @author literakl
 * @since 8.10.2008
 */
public class TestHtmlToTextFormatter extends TestCase {

    public void testDoubleBrSimpleFormat() throws Exception {
        String input = "a\n<br><br>\nb";
        String expected = "a\n\nb";
        assertEquals("error", expected, HtmlToTextFormatter.format(input));
        input = "a\n<br><br>\n\nb";
        expected = "a\n\nb";
        assertEquals("error", expected, HtmlToTextFormatter.format(input));
    }

    public void testNewSimpleFormat() throws Exception {
        String  input = "a\n<br class=\"separator\">\nb";
        String expected = "a\n\nb";
        assertEquals("error", expected, HtmlToTextFormatter.format(input));
    }

    public void testTrailingNewLinesSimpleFormat() throws Exception {
        String  input = "a\n<br class=\"separator\">\nb\n";
        String expected = "a\n\nb";
        input = "a\n<br class=\"separator\">\nb\n\n\n";
        expected = "a\n\nb";
        input = "a\n<br class=\"separator\">\nb<p></p>";
        expected = "a\n\nb";
        assertEquals("error", expected, HtmlToTextFormatter.format(input));
    }

    public void testTextWithPre() throws Exception {
        String input = "a\n<br><br>\nb\n<pre>c\n\nd</pre>e";
        String expected = "a\n\nb\n\nc\n\nd\n\ne";
        assertEquals("error", expected, HtmlToTextFormatter.format(input));
    }
}
