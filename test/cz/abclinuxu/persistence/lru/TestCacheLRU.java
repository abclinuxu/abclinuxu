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
package cz.abclinuxu.persistence.lru;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestCacheLRU extends TestCase {

    public TestCacheLRU(String s) {
        super(s);
    }

    /**
     * test new remove functionality on full cache
     */
    public void testRemoveFullCache() throws Exception {
        CacheLRU cache = new CacheLRU(5);
        cache.addElement("A","A");
        cache.addElement("B","B");
        cache.addElement("C","C");
        cache.addElement("D","D");
        cache.addElement("E","E");
        assertEquals("EDCBA",cache.printKeys(false));
        cache.removeElement("C");
        assertNull(cache.getElement("C"));
        assertEquals("EDBAC",cache.printKeys(false));
        assertEquals("CABDE",cache.printKeys(true));
        cache.addElement("F","F");
        assertEquals("FEDBA",cache.printKeys(false));
        assertEquals("ABDEF",cache.printKeys(true));
        cache.removeElement("F");
        assertNull(cache.getElement("F"));
        assertEquals("EDBAF",cache.printKeys(false));
        assertEquals("FABDE",cache.printKeys(true));
    }

    /**
     * test new remove functionality on not full cache
     */
    public void testRemoveEmptyCache() throws Exception {
        CacheLRU cache = new CacheLRU(4);
        cache.addElement("A","A");
        cache.removeElement("A");
        assertNull(cache.getElement("A"));
//        assertEquals("",cache.printKeys(false)); algorithm doesn't allow this
        cache.addElement("B","B");
        cache.addElement("C","C");
        assertEquals("CBA",cache.printKeys(false));
        cache.removeElement("C");
        assertEquals("BAC",cache.printKeys(false));
        assertEquals("CAB",cache.printKeys(true));
        cache.addElement("D","D");
        assertEquals("DBAC",cache.printKeys(false));
        assertEquals("CABD",cache.printKeys(true));
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestCacheLRU.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

//    void print() {
//        System.out.print("Data: ");
//        for (int i = 0; i < _cache.length; i++) {
//            String key = (_cache[i]._key==null) ? " ":_cache[i]._key.toString();
//            System.out.print("["+key+" ]");
//        }
//        System.out.print("\nPrev: ");
//        for (int i = 0; i < __prev.length; i++) {
//            System.out.print("["+__prev[i]+"]");
//        }
//        System.out.print("\nNext: ");
//        for (int i = 0; i < __next.length; i++) {
//            System.out.print("["+__next[i]+"]");
//        }
//        System.out.print(" head = " + __head);
//        System.out.print(", tail = " + __tail);
//        System.out.println(", size = " + _numEntries);
//        System.out.println();
//    }
}
