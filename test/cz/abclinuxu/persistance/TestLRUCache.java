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
