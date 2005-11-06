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
package cz.abclinuxu.utils.search;

import junit.framework.*;
import junit.textui.TestRunner;

import java.io.FileInputStream;

import cz.abclinuxu.utils.freemarker.Tools;

public class TestCreateIndex extends TestCase {

    public TestCreateIndex(String s) {
        super(s);
    }

    public void testRemoveTags() {
        String input = "toto <b>je <a href=\"http://www.abclinuxu.cz/~literakl/cgi?a=b\">maly</a> test</b>";
        String output = "toto je maly test";
        assertEquals(output,Tools.removeTags(input));
        input = "toto 3<x je <b>spatne</a> html";
        output = "toto 3<x je spatne html";
        assertEquals(output,Tools.removeTags(input));
        input = "obrazek <img src=\"/images/clanky/stefan/corel.jpg\" \nwidth=\"100\"> konec";
        output = "obrazek  konec";
        assertEquals(output,Tools.removeTags(input));
    }

    /**
     * For two strings the method removeTags() throws
     * StackOverflowError in tagRE.subst(). But deployed
     * version works fine.
     */
    public void testStackOverflowRegexp() throws Exception {
        String str = readFile("/home/literakl/abc/source/test/cz/abclinuxu/utils/search/chyba.txt");
        String result = Tools.removeTags(str);
    }

    String readFile(String name) throws Exception {
        FileInputStream fis = new FileInputStream(name);
        StringBuffer sb = new StringBuffer();
        int c;
        while ( (c=fis.read())!=-1 ) {
            sb.append((char)c);
        }
        fis.close();
        return sb.toString();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestCreateIndex.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestCreateIndex.suite());
    }
}
