/*
 * User: literakl
 * Date: Jan 29, 2002
 * Time: 5:01:46 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import junit.framework.*;
import junit.textui.TestRunner;
import cz.abclinuxu.data.*;

import java.util.*;

public class TestSorters extends TestCase {

    public TestSorters(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestSorters.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestSorters.suite());
    }

    public void testSortByDate() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();

        Category first = new Category();
        cal.setTime(now);
        cal.add(Calendar.YEAR,-1);
        first.setUpdated(cal.getTime());

        Record second = new Record();
        cal.setTime(now);
        cal.add(Calendar.MONTH,-1);
        second.setUpdated(cal.getTime());

        Poll third = new Poll();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_YEAR,-1);
        third.setCreated(cal.getTime());

        Link link = new Link();
        link.setUpdated(now);

        Relation fourth = new Relation(null,link,0);

        List list = new ArrayList();
        list.add(fourth); list.add(first); list.add(third); list.add(second);
        Sorters2.byDate(list,Sorters2.ASCENDING);
        assertEquals(first,list.get(0));
        assertEquals(second,list.get(1));
        assertEquals(third,list.get(2));
        assertEquals(fourth,list.get(3));

        list.clear();
        list.add(fourth); list.add(first); list.add(third); list.add(second);
        Sorters2.byDate(list, Sorters2.DESCENDING);
        assertEquals(first,list.get(3));
        assertEquals(second,list.get(2));
        assertEquals(third,list.get(1));
        assertEquals(fourth,list.get(0));
    }
}
