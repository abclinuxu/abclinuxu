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
package cz.abclinuxu.utils.paging;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests Paging module.
 */
public class TestPaging extends TestCase {

    /**
     * Used by JUnit
     * @param s
     */
    public TestPaging(String s) {
        super(s);
    }

    /**
     * Performs all tests.
     */
    public void testFunctionality() throws Exception {
        List set = new ArrayList();
        set.add(new Integer(1));
        set.add(new Integer(2));
        set.add(new Integer(3));
        set.add(new Integer(4));
        set.add(new Integer(5));
        set.add(new Integer(6));
        set.add(new Integer(7));

        Paging paging = new Paging(set.subList(0,2),0,2);
        assertNull(paging.getPrevPage());
        assertNotNull(paging.getNextPage());
        assertEquals(1,((Integer)set.get(paging.getCurrentPage().row)).intValue());
        assertEquals(2,paging.getCurrentPage().size);
        assertEquals(2,paging.getNextPage().row);
        assertEquals(3, ((Integer) set.get(paging.getNextPage().row)).intValue());

        paging = new Paging(set.subList(1,3),1,2);
        assertNotNull(paging.getPrevPage());
        assertNotNull(paging.getNextPage());
        assertEquals(1,paging.getCurrentPage().row);
        assertEquals(0,paging.getPrevPage().row);
        assertEquals(1, ((Integer) set.get(paging.getPrevPage().row)).intValue());
        assertEquals(3,paging.getNextPage().row);
        assertEquals(4, ((Integer) set.get(paging.getNextPage().row)).intValue());

        paging = new Paging(set.subList(2,4),2,2);
        assertNotNull(paging.getPrevPage());
        assertNotNull(paging.getNextPage());
        assertEquals(0,paging.getPrevPage().row);
        assertEquals(1, ((Integer) set.get(paging.getPrevPage().row)).intValue());
        assertEquals(4,paging.getNextPage().row);
        assertEquals(5, ((Integer) set.get(paging.getNextPage().row)).intValue());

        paging = new Paging(set.subList(4,6),4,2);
        assertNotNull(paging.getPrevPage());
        assertNotNull(paging.getNextPage());
        assertEquals(2,paging.getPrevPage().row);
        assertEquals(3, ((Integer) set.get(paging.getPrevPage().row)).intValue());
        assertEquals(6,paging.getNextPage().row);
        assertEquals(7, ((Integer) set.get(paging.getNextPage().row)).intValue());

        paging = new Paging(set.subList(4,6),4,2,7);
        assertNotNull(paging.getPrevPage());
        assertNotNull(paging.getNextPage());
        assertEquals(2,paging.getPrevPage().row);
        assertEquals(3, ((Integer) set.get(paging.getPrevPage().row)).intValue());
        assertEquals(6,paging.getNextPage().row);
        assertEquals(7, ((Integer) set.get(paging.getNextPage().row)).intValue());

        paging = new Paging(set.subList(5,7),5,2);
        assertNotNull(paging.getPrevPage());
        assertNotNull(paging.getNextPage()); // algorithm doesn't know, we are at last Page.
        assertEquals(3,paging.getPrevPage().row);
        assertEquals(4, ((Integer) set.get(paging.getPrevPage().row)).intValue());
        assertEquals(7,paging.getNextPage().row);

        paging = new Paging(set.subList(5,7),5,2,7);
        assertNotNull(paging.getPrevPage());
        assertNull(paging.getNextPage()); // algorithm can compute, we are at the end of set
        assertEquals(3,paging.getPrevPage().row);
        assertEquals(4, ((Integer) set.get(paging.getPrevPage().row)).intValue());

        paging = new Paging(set.subList(6,7),6,2);
        assertNotNull(paging.getPrevPage());
        assertNull(paging.getNextPage()); // algorithm can compute, we are at the end of set
        assertEquals(4,paging.getPrevPage().row);
        assertEquals(5, ((Integer) set.get(paging.getPrevPage().row)).intValue());

        paging = new Paging(set.subList(6,7),6,2,7);
        assertNotNull(paging.getPrevPage());
        assertNull(paging.getNextPage()); // algorithm can compute, we are at the end of set
        assertEquals(4,paging.getPrevPage().row);
        assertEquals(5, ((Integer) set.get(paging.getPrevPage().row)).intValue());
    }
}
