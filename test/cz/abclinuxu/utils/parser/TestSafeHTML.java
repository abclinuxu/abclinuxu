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
import cz.abclinuxu.utils.parser.safehtml.*;

/**
 * Verifies SafeHTMLGuard functionality.
 */
public class TestSafeHTML extends TestCase {

    public void testForbiddenTag1() throws Exception {
        try {
            String s = "zde je <img src=\"/images/logo.gif\"> obrazek";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag2() throws Exception {
        try {
            String s = "zde je <table>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag3() throws Exception {
        try {
            String s = "zde je <tr>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag4() throws Exception {
        try {
            String s = "zde je <td>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag5() throws Exception {
        try {
            String s = "zde je <script>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testForbiddenTag6() throws Exception {
        try {
            String s = "zde je <form>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedNoAttributes1() throws Exception {
        String s = "zde je <p>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedNoAttributes2() throws Exception {
        try {
            String s = "zde je <p style=\"color:red\">";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedNoAttributes3() throws Exception {
        String s = "zde je <br>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedNoAttributes4() throws Exception {
        try {
            String s = "zde je <br style=\"color:red\">";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedNoAttributes5() throws Exception {
        String s = "zde je <li>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedNoAttributes6() throws Exception {
        try {
            String s = "zde je <li style=\"color:red\">";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes1() throws Exception {
        String s = "zde je <ul>a</ul>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes2() throws Exception {
        try {
            String s = "zde je <ul style=\"color:red\">a</ul>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes3() throws Exception {
        try {
            String s = "zde je <ul>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes4() throws Exception {
        String s = "zde je <ol>a</ol>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes5() throws Exception {
        try {
            String s = "zde je <ol style=\"color:red\">a</ol>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes6() throws Exception {
        try {
            String s = "zde je <ol>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes7() throws Exception {
        String s = "zde je <b>a</b>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes8() throws Exception {
        try {
            String s = "zde je <b style=\"color:red\">a</b>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes9() throws Exception {
        try {
            String s = "zde je <b>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes10() throws Exception {
        String s = "zde je <i>a</i>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes11() throws Exception {
        try {
            String s = "zde je <i style=\"color:red\">a</i>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes12() throws Exception {
        try {
            String s = "zde je <i>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes13() throws Exception {
        String s = "zde je <code>a</code>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes14() throws Exception {
        try {
            String s = "zde je <code style=\"color:red\">a</code>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes15() throws Exception {
        try {
            String s = "zde je <code>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes16() throws Exception {
        String s = "zde je <pre>a</pre>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes17() throws Exception {
        try {
            String s = "zde je <pre style=\"color:red\">a</pre>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes18() throws Exception {
        try {
            String s = "zde je <pre>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes19() throws Exception {
        String s = "zde je <div>a</div>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes20() throws Exception {
        try {
            String s = "zde je <div style=\"color:red\">a</div>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes21() throws Exception {
        try {
            String s = "zde je <div>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes22() throws Exception {
        String s = "zde je <h1>a</h1>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes23() throws Exception {
        try {
            String s = "zde je <h1 style=\"color:red\">a</h1>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes24() throws Exception {
        try {
            String s = "zde je <h1>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes25() throws Exception {
        String s = "zde je <h2>a</h2>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes26() throws Exception {
        try {
            String s = "zde je <h2 style=\"color:red\">a</h2>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes27() throws Exception {
        try {
            String s = "zde je <h2>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes28() throws Exception {
        String s = "zde je <h3>a</h3>";
        SafeHTMLGuard.check(s);
    }

    public void testAllowedClosedNoAttributes29() throws Exception {
        try {
            String s = "zde je <h3 style=\"color:red\">a</h3>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
    }

    public void testAllowedClosedNoAttributes30() throws Exception {
        try {
            String s = "zde je <h3>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testLinkTag() throws Exception {
        String s = "zde je <a href=\"/clanky/show/1234\">odkaz</a>";
        SafeHTMLGuard.check(s);

        try {
            s = "zde je <a href=\"/clanky/show/1234\" style=\"color:red\">odkaz</a>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (AttributeNotAllowedException e) {
            //ok
        }
        try {
            s = "zde je <a>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: "+s);
        } catch (TagNotClosedException e) {
            //ok
        }
    }

    public void testCrossedTag() throws Exception {
        try {
            String s = "zde <b>zacina<i>problematicky</b>kod</i>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: " + s);
        } catch (CrossedTagException e) {
            //ok
        }
    }

    public void testCrossedOptionallyClosedTag() throws Exception {
        try {
            String s = "zde <a>zacina<p>problematicky</a>kod</p>";
            SafeHTMLGuard.check(s);
            fail("Shall have failed: " + s);
        } catch (TagNotClosedException e) {
            //ok
        } catch (CrossedTagException e) {
            //ok
        }
    }

    public void testNestedTag() throws Exception {
        String nested = "<ol><li><ol><li>1.1</li></ol></li></ol>";
        SafeHTMLGuard.check(nested);

        nested = "<ol><li><ol><li>1.1</ol></ol>";
        SafeHTMLGuard.check(nested);
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
