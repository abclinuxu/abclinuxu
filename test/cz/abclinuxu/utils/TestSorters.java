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
