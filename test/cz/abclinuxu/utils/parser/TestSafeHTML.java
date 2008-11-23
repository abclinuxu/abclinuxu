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
package cz.abclinuxu.utils.parser;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import cz.abclinuxu.utils.parser.clean.exceptions.AttributeNotAllowedException;
import cz.abclinuxu.utils.parser.clean.exceptions.AttributeValueNotAllowedException;
import cz.abclinuxu.utils.parser.clean.exceptions.CrossedTagException;
import cz.abclinuxu.utils.parser.clean.exceptions.TagNotAllowedException;
import cz.abclinuxu.utils.parser.clean.exceptions.TagNotClosedException;
import cz.abclinuxu.utils.parser.clean.HtmlChecker;
import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.utils.config.ConfigurationManager;

/**
 * Verifies SafeHTMLGuard functionality.
 */
public class TestSafeHTML extends TestCase {
    boolean initialized = false;

    protected void setUp() throws Exception {
        super.setUp();
        if (!initialized) {
            HtmlChecker checker = new HtmlChecker();
            checker.setConfigFileName("conf/tag_checker.xml");
            ConfigurationManager.getConfigurator().configureAndRememberMe(checker);
            initialized = true;
        }
    }

    public void testForbiddenTag1() throws Exception {
        try {
            String s = "zde je <img src=\"/images/logo.gif\"> obrazek";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag5() throws Exception {
        try {
            String s = "zde je <script>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag6() throws Exception {
        try {
            String s = "zde je <form>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    // bug #544
    public void testForbiddenTag7() throws Exception {
        try {
            String s = "zde je <<form>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedNoAttributes1() throws Exception {
        String s = "zde je <br>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedNoAttributes2() throws Exception {
        try {
            String s = "zde je <br style=\"color:red\">";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedNoAttributes3() throws Exception {
        String s = "zde je <br>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedNoAttributes4() throws Exception {
        try {
            String s = "zde je <br style=\"color:red\">";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedNoAttributes5() throws Exception {
        String s = "zde je <li>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedNoAttributes6() throws Exception {
        try {
            String s = "zde je <li style=\"color:red\">";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes1() throws Exception {
        String s = "zde je <ul>a</ul>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes2() throws Exception {
        try {
            String s = "zde je <ul style=\"color:red\">a</ul>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes3() throws Exception {
        try {
            String s = "zde je <ul>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes4() throws Exception {
        String s = "zde je <ol>a</ol>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes5() throws Exception {
        try {
            String s = "zde je <ol style=\"color:red\">a</ol>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes6() throws Exception {
        try {
            String s = "zde je <ol>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes7() throws Exception {
        String s = "zde je <b>a</b>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes8() throws Exception {
        try {
            String s = "zde je <b style=\"color:red\">a</b>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes9() throws Exception {
        try {
            String s = "zde je <b>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes10() throws Exception {
        String s = "zde je <i>a</i>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes11() throws Exception {
        try {
            String s = "zde je <i style=\"color:red\">a</i>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes12() throws Exception {
        try {
            String s = "zde je <i>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes13() throws Exception {
        String s = "zde je <code>a</code>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes14() throws Exception {
        try {
            String s = "zde je <code style=\"color:red\">a</code>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes15() throws Exception {
        try {
            String s = "zde je <code>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes16() throws Exception {
        String s = "zde je <pre>a</pre>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes17() throws Exception {
        try {
            String s = "zde je <center style=\"color:red\">a</center>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes18() throws Exception {
        try {
            String s = "zde je <pre>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes19() throws Exception {
        String s = "zde je <div>a</div>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes20() throws Exception {
        try {
            String s = "zde je <ACRONYM style=\"color:red\">a</ACRONYM>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes21() throws Exception {
        try {
            String s = "zde je <div>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes22() throws Exception {
        String s = "zde je <h1>a</h1>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes23() throws Exception {
        try {
            String s = "zde je <h1 style=\"color:red\">a</h1>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes24() throws Exception {
        try {
            String s = "zde je <h1>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes25() throws Exception {
        String s = "zde je <h2>a</h2>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes26() throws Exception {
        try {
            String s = "zde je <h2 style=\"color:red\">a</h2>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes27() throws Exception {
        try {
            String s = "zde je <h2>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes28() throws Exception {
        String s = "zde je <h3>a</h3>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes29() throws Exception {
        try {
            String s = "zde je <h3 style=\"color:red\">a</h3>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes30() throws Exception {
        try {
            String s = "zde je <h3>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes32() throws Exception {
        try {
            String s = "zde je <i>nejaky <i>link</i>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes33() throws Exception {
        String s = "zde je <i>nejaky <i>link</i>.</i>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testAllowedClosedNoAttributes34() throws Exception {
        try {
            String s = "<p>Cháchá, to je slovo do pranice! \"Já jsem to napsal, protože \"<b>voni takoví\n" +
                    "jsou\" :-D </p>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (CrossedTagException e) {
            //ok
        }
    }

    public void testLinkTag1() throws Exception {
        String s = "zde je <a href=\"/clanky/show/1234\">odkaz</a>";
        HtmlChecker.check(Rules.DEFAULT, s);
    }

    public void testLinkTag2() throws Exception {
       try {
            String s = "zde je <a href=\"\">odkaz</a>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeValueNotAllowedException e) {
            //ok
        }
    }

    public void testLinkTag3() throws Exception {
        try {
            String s = "zde je <a href=\"javascript:location='http://images.ucomics.com/comics/ga/2006/ga06'+((new\n" +
                    "Date()).getMonth()+1)+(new Date()).getDate()+'.gif'\">odkaz</a>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeValueNotAllowedException e) {
            //ok
        }
    }

    public void testLinkTag4() throws Exception {
        try {
            String s = "zde je <a href=\"&#106;&#097;&#118;&#097;&#115;&#099;&#114;&#105;&#112;&#116;:alert('BAM!')\">zde</a>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: " + s);
        } catch (AttributeValueNotAllowedException e) {
            //ok
        }
    }

    public void testLinkTag5() throws Exception {
        try {
            String s = "zde je <a href=\"/clanky/show/1234\" style=\"color:red\">odkaz</a>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testLinkTag6() throws Exception {
        try {
            String s = "zde je <a>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testImgTag1() throws Exception {
        try {
            String s = "zde je <img src=\"data:image/gif;base64,R0lGODdhMAAwAPAAAAAAAP///ywAAAAAMAAwAAAC8IyPqcvt3wCcDkiLc7C0qwyGHhSWpjQu5yqmCYsapyuvUUlvONmOZtfzgFzByTB10QgxOR0TqBQejhRNzOfkVJ+5YiUqrXF5Y5lKh/DeuNcP5yLWGsEbtLiOSpa/TPg7JpJHxyendzWTBfX0cxOnKPjgBzi4diinWGdkF8kjdfnycQZXZeYGejmJlZeGl9i2icVqaNVailT6F5iJ90m6mvuTS4OK05M0vDk0Q4XUtwvKOzrcd3iq9uisF81M1OIcR7lEewwcLp7tuNNkM3uNna3F2JQFo97Vriy/Xl4/f1cf5VWzXyym7PHhhx4dbgYKAAA7\">";
            HtmlChecker.check(Rules.WIKI, s);
            fail("Shall have failed: " + s);
        } catch (AttributeValueNotAllowedException e) {
            //ok
        }
    }

    public void testImgTag2() throws Exception {
        try {
            String s = "zde je <img src=\"javascript:location='http://images.ucomics.com/comics/ga/2006/ga06'+((new\n" +
                    "Date()).getMonth()+1)+(new Date()).getDate()+'.gif'\">";
            HtmlChecker.check(Rules.WIKI, s);
            fail("Shall have failed: " + s);
        } catch (AttributeValueNotAllowedException e) {
            //ok
        }
    }

    public void testImgTag3() throws Exception {
        try {
            String s = "zde je <img src=\"&#106;&#097;&#118;&#097;&#115;&#099;&#114;&#105;&#112;&#116;:alert('BAM!')\">";
            HtmlChecker.check(Rules.WIKI, s);
            fail("Shall have failed: " + s);
        } catch (AttributeValueNotAllowedException e) {
            //ok
        }
    }

    public void testCrossedTag() throws Exception {
        try {
            String s = "zde <b>zacina<i>problematicky</b>kod</i>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: " + s);
        } catch (CrossedTagException e) {
            //ok
        }
    }

    public void testCrossedOptionallyClosedTag() throws Exception {
        try {
            String s = "zde <a>zacina<p>problematicky</a>kod</p>";
            HtmlChecker.check(Rules.DEFAULT, s);
            fail("Shall have failed: " + s);
        } catch (TagNotClosedException e) {
            //ok
        } catch (CrossedTagException e) {
            //ok
        }
    }

    public void testNestedTag() throws Exception {
        String nested = "<ol><li><ol><li>1.1</li></ol></li></ol>";
        HtmlChecker.check(Rules.DEFAULT, nested);

        nested = "<ol><li><ol><li>1.1</ol></ol>";
        HtmlChecker.check(Rules.DEFAULT, nested);
    }

    public void testXHtmlTag() throws Exception {
        String nested = "<p><img src=\"a\"/></p>";
        HtmlChecker.check(Rules.WIKI, nested);
        nested = "<ul/>";
        HtmlChecker.check(Rules.WIKI, nested);
    }

    public TestSafeHTML(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestSafeHTML.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
