/*
 * User: literakl
 * Date: Mar 3, 2002
 * Time: 6:05:19 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import junit.framework.*;
import junit.textui.TestRunner;
import org.apache.log4j.BasicConfigurator;

public class TestText extends TestCase {

    /**
     * tests method fixLines
     */
    public void testFixLines() {
        Tools tools = new Tools();

        String str1 = "Hello<br>My name is Bond.James Bond.";
        String exp1 = "Hello<br>My name is Bond.James Bond.";
        String str2 = "Hello<P>My name is Bond.James Bond.";
        String exp2 = "Hello<P>My name is Bond.James Bond.";
        String str3 = "Hello\nMy name is Bond.\nJames Bond.";
        String exp3 = "Hello\nMy name is Bond.\nJames Bond.";
        String str4 = "Hello\n\nMy name is Bond.\n\nJames Bond.";
        String exp4 = "Hello<p>\nMy name is Bond.<p>\nJames Bond.";
        String str5 = "Hello\r\n\r\nMy name is Bond.\r\n\r\nJames Bond.";
        String exp5 = "Hello<p>\nMy name is Bond.<p>\nJames Bond.";

        assertEquals(exp1,tools.render(str1,null));
        assertEquals(exp2,tools.render(str2, null));
        assertEquals(exp3,tools.render(str3, null));
        assertEquals(exp4,tools.render(str4, null));
        assertEquals(exp5,tools.render(str5, null));
    }

    public void testStripTags() {
        Tools tools = new Tools();

        String str1 = "Hello<br> Bond.";
        String exp1 = "Hello Bond.";
        String str2 = "Hello <a href=\"http://www.gov.uk/~007\">Bond</a>.";
        String exp2 = "Hello Bond.";
        String str3 = "Hello <h2>Bond</h2>.";
        String exp3 = "Hello Bond.";

        assertEquals(exp1, tools.removeTags(str1));
        assertEquals(exp2, tools.removeTags(str2));
        assertEquals(exp3, tools.removeTags(str3));
    }

    public TestText(String s) {
        super(s);
        BasicConfigurator.configure();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestText.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestText.suite());
    }

}
