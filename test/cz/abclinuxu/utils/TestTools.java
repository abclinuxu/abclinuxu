/*
 * User: literakl
 * Date: Jan 25, 2002
 * Time: 7:02:40 PM
 */
package cz.abclinuxu.utils;

import junit.framework.*;
import junit.textui.TestRunner;
import cz.abclinuxu.utils.Tools;

/**
 * Test of Tools features
 */
public class TestTools extends TestCase {

    public TestTools(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestTools.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Tests method <code>percent()</code>
     */
    public void testPercent() {
        Tools tools = new Tools();

        assertEquals(50,tools.percent(1,2));
        assertEquals(33,tools.percent(1,3));
        assertEquals(67,tools.percent(2,3));
        assertEquals(25,tools.percent(1,4));
        assertEquals(100,tools.percent(1,1));
        assertEquals(0,tools.percent(0,1));
        assertEquals(0,tools.percent(0,0));
    }
}
