/*
 * User: literakl
 * Date: May 18, 2002
 * Time: 10:36:52 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils.search;

import junit.framework.*;
import junit.textui.TestRunner;

public class TestCreateIndex extends TestCase {

    public TestCreateIndex(String s) {
        super(s);
    }

    public void testRemoveTags() {
        String input = "toto <b>je <a href=\"http://localhost/\">maly</a> test</b>";
        String output = "toto je maly test";
        assertEquals(output,CreateIndex.removeTags(input));
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestCreateIndex.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestCreateIndex.suite());
    }
}
