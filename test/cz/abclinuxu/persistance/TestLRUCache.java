/*
 * User: Leos Literak
 * Date: May 29, 2003
 * Time: 9:21:15 PM
 */
package cz.abclinuxu.persistance;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestLRUCache extends TestCase {

    public TestLRUCache(String s) {
        super(s);
    }

    /**
     * Hypotetically, if Relation has been stored and we
     * store its modified version again, it may happen,
     * that database will be inconsistent.
     */
    public void testDuplicateRelation() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestLRUCache.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
