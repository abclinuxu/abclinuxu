/*
 * User: literakl
 * Date: Jan 25, 2002
 * Time: 7:02:40 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets;

import junit.framework.*;
import junit.textui.TestRunner;
import org.apache.log4j.xml.DOMConfigurator;
import cz.abclinuxu.servlets.utils.VelocityHelper;

/**
 * Test of VelocityHelper features
 */
public class TestVelocityHelper extends TestCase {

    static {
        DOMConfigurator.configure("conf/log4j.xml");
    }

    public TestVelocityHelper(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestVelocityHelper.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Tests method <code>percent()</code>
     */
    public void testPercent() {
        VelocityHelper helper = new VelocityHelper();

        assertEquals(50,helper.percent(1,2));
        assertEquals(33,helper.percent(1,3));
        assertEquals(67,helper.percent(2,3));
        assertEquals(25,helper.percent(1,4));
        assertEquals(100,helper.percent(1,1));
        assertEquals(0,helper.percent(0,1));
        assertEquals(0,helper.percent(0,0));
    }
}
