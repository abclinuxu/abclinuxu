/*
 * User: literakl
 * Date: Mar 3, 2002
 * Time: 6:05:19 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import junit.framework.*;
import junit.textui.TestRunner;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import org.apache.log4j.BasicConfigurator;

public class TestText extends TestCase {

    /**
     * tests method fixLines
     */
    public void testFixLines() {
        String str1 = "Hello<br>My name is Bond.James Bond.";
        String exp1 = "Hello<br>My name is Bond.James Bond.";
        String str2 = "Hello<P>My name is Bond.James Bond.";
        String exp2 = "Hello<P>My name is Bond.James Bond.";
        String str3 = "Hello\nMy name is Bond.\nJames Bond.";
        String exp3 = "Hello\nMy name is Bond.\nJames Bond.";
        String str4 = "Hello\n\nMy name is Bond.\n\nJames Bond.";
        String exp4 = "Hello<P>\nMy name is Bond.<P>\nJames Bond.";
        String str5 = "Hello\r\n\r\nMy name is Bond.\r\n\r\nJames Bond.";
        String exp5 = "Hello<P>\nMy name is Bond.<P>\nJames Bond.";

        assertEquals(exp1,VelocityHelper.fixLines(str1));
        assertEquals(exp2,VelocityHelper.fixLines(str2));
        assertEquals(exp3,VelocityHelper.fixLines(str3));
        assertEquals(exp4,VelocityHelper.fixLines(str4));
        assertEquals(exp5,VelocityHelper.fixLines(str5));
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
