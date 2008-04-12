/*
 *  Copyright (C) 2008 Leos Literak
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
package cz.abclinuxu.persistence;

import junit.framework.TestCase;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.view.SectionTreeCache;

/**
 * @author literakl
 * @since 7.4.2008
 */
public class TestSectionTreeCache extends TestCase {
    Persistence persistence = PersistenceFactory.getPersistence(PersistenceFactory.defaultTestUrl);
    Category root, A, A1, A11, A12, A2, A3, A31, B, B1, B11, B12, B2, C, C1, C11, C12;
    Relation relRootA, relAA1, relA1A11, relA1A12, relAA2, relAA3, relA3A31;
    Relation relRootB, relBB1, relB1B11, relB1B12, relBB2, relRootC, relCC1, relC1C11, relC1C12;

    /*
      section structure

      root
           A
            A1
              A11
              A12
            A2
            A3
              A31
           B
            B1
              B11
              B12
            B2
           C
            C1
              C11
              C12
    */

    public void testAll() throws Exception {
        SectionTreeCache cache = new SectionTreeCache("/", root.getId());
        cache.initialize();
        assertEquals("A", cache.getById(A.getId()).getName());
        assertEquals("A", cache.getByRelation(relRootA.getId()).getName());
        assertEquals("A1", cache.getById(A1.getId()).getName());
        assertEquals("A1", cache.getByRelation(relAA1.getId()).getName());
        assertEquals("A12", cache.getById(A12.getId()).getName());
        assertEquals("A12", cache.getByRelation(relA1A12.getId()).getName());
        assertEquals("B", cache.getById(B.getId()).getName());
        assertEquals("B", cache.getByRelation(relRootB.getId()).getName());
        assertEquals("B11", cache.getById(B11.getId()).getName());
        assertEquals("B11", cache.getByRelation(relB1B11.getId()).getName());
        assertEquals("C", cache.getById(C.getId()).getName());
        assertEquals("C", cache.getByRelation(relRootC.getId()).getName());
        assertEquals("C12", cache.getById(C12.getId()).getName());
        assertEquals("C12", cache.getByRelation(relC1C12.getId()).getName());
    }

    protected void setUp() throws Exception {
        root = new Category();
        root.setTitle("root");
        root.setData("<data></data>");
        persistence.create(root);

        A = new Category();
        A.setTitle("A");
        A.setData("<data></data>");
        persistence.create(A);

        relRootA = (Relation) new Relation(root, A, 0);
        persistence.create(relRootA);

        A1 = new Category();
        A1.setTitle("A1");
        A1.setData("<data></data>");
        persistence.create(A1);

        relAA1 = (Relation) new Relation(A, A1, relRootA.getId());
        persistence.create(relAA1);

        A11 = new Category();
        A11.setTitle("A11");
        A11.setData("<data></data>");
        persistence.create(A11);

        relA1A11 = (Relation) new Relation(A1, A11, relAA1.getId());
        persistence.create(relA1A11);

        A12 = new Category();
        A12.setTitle("A12");
        A12.setData("<data></data>");
        persistence.create(A12);

        relA1A12 = (Relation) new Relation(A1, A12, relAA1.getId());
        persistence.create(relA1A12);

        A2 = new Category();
        A2.setTitle("A2");
        A2.setData("<data></data>");
        persistence.create(A2);

        relAA2 = (Relation) new Relation(A, A2, relRootA.getId());
        persistence.create(relAA2);

        A3 = new Category();
        A3.setTitle("A3");
        A3.setData("<data></data>");
        persistence.create(A3);

        relAA3 = (Relation) new Relation(A, A3, relRootA.getId());
        persistence.create(relAA3);

        A31 = new Category();
        A31.setTitle("A31");
        A31.setData("<data></data>");
        persistence.create(A31);

        relA3A31 = (Relation) new Relation(A3, A31, relAA3.getId());
        persistence.create(relA3A31);

        B = new Category();
        B.setTitle("B");
        B.setData("<data></data>");
        persistence.create(B);

        relRootB = (Relation) new Relation(root, B, 0);
        persistence.create(relRootB);

        B1 = new Category();
        B1.setTitle("B1");
        B1.setData("<data></data>");
        persistence.create(B1);

        relBB1 = (Relation) new Relation(B, B1, relRootB.getId());
        persistence.create(relBB1);

        B11 = new Category();
        B11.setTitle("B11");
        B11.setData("<data></data>");
        persistence.create(B11);

        relB1B11 = (Relation) new Relation(B1, B11, relBB1.getId());
        persistence.create(relB1B11);

        B12 = new Category();
        B12.setTitle("B12");
        B12.setData("<data></data>");
        persistence.create(B12);

        relB1B12 = (Relation) new Relation(B1, B12, relBB1.getId());
        persistence.create(relB1B12);

        B2 = new Category();
        B2.setTitle("B2");
        B2.setData("<data></data>");
        persistence.create(B2);

        relBB2 = (Relation) new Relation(B, B2, relRootB.getId());
        persistence.create(relBB2);

        C = new Category();
        C.setTitle("C");
        C.setData("<data></data>");
        persistence.create(C);

        relRootC = (Relation) new Relation(root, C, 0);
        persistence.create(relRootC);

        C1 = new Category();
        C1.setTitle("C1");
        C1.setData("<data></data>");
        persistence.create(C1);

        relCC1 = (Relation) new Relation(C, C1, relRootC.getId());
        persistence.create(relCC1);

        C11 = new Category();
        C11.setTitle("C11");
        C11.setData("<data></data>");
        persistence.create(C11);

        relC1C11 = (Relation) new Relation(C1, C11, relCC1.getId());
        persistence.create(relC1C11);

        C12 = new Category();
        C12.setTitle("C12");
        C12.setData("<data></data>");
        persistence.create(C12);

        relC1C12 = (Relation) new Relation(C1, C12, relCC1.getId());
        persistence.create(relC1C12);
    }

    protected void tearDown() throws Exception {
        if (root != null)
            persistence.remove(root);
    }
}
