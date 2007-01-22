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
package cz.abclinuxu.persistence;

import java.util.*;
import junit.framework.*;
import junit.textui.TestRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.persistence.cache.LRUCache;
import cz.abclinuxu.servlets.Constants;

public class TestMySqlPersistance extends TestCase {

    Persistence persistence;

    public TestMySqlPersistance(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        super.setUp();
//        persistence = new MySqlPersistance(PersistanceFactory.defaultTestUrl);
//        persistence.setCache(new LRUCache());
        persistence = PersistenceFactory.getPersistance(PersistenceFactory.defaultTestUrl, LRUCache.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        LogManager.getRootLogger().setLevel(Level.ALL);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestMySqlPersistance.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestMySqlPersistance.suite());
    }

    /**
     * test correctness of <code>remove</code>
     */
    public void testRemove() throws Exception {
        Item a = new Item(0,Item.HARDWARE);
        a.setOwner(1);
        a.setData("<name>make</name>");
        persistence.create(a);

        Record b = new Record(0,Record.SOFTWARE);
        b.setOwner(2);
        b.setData("<name>sw b</name>");
        persistence.create(b);

        Relation relation = new Relation(a,b,0);
        persistence.create(relation);
        a.addChildRelation(relation);

        Record c = new Record(0,Record.ARTICLE);
        c.setOwner(1);
        c.setData("<name>article c</name>");
        persistence.create(c);

        relation = new Relation(a,c,0);
        persistence.create(relation);
        a.addChildRelation(relation);

        Category d = new Category(0);
        d.setOwner(2);
        d.setData("<name>section</name>");
        persistence.create(d);

        relation = new Relation(d,c,0);
        persistence.create(relation);
        d.addChildRelation(relation);

        persistence.remove(a);

        // now only c and d shall exist
        GenericObject test = persistence.findById(c);
        assertTrue(c.preciseEquals(test));

        test = persistence.findById(d);
        assertTrue(d.preciseEquals(test));

        try {
            test = persistence.findById(a);
            fail("found deleted object " + a);
        } catch (NotFoundException e) {
            assertTrue( true );
        }

        try {
            test = persistence.findById(b);
            fail("found deleted object " + b);
        } catch (NotFoundException e) {
            assertTrue(true);
        }

        // now clean up database
        persistence.remove(relation);
        d.removeChildRelation(relation);

        try {
            test = persistence.findById(c);
            fail("found deleted object " + c);
        } catch (NotFoundException e) {
            assertTrue(true);
        }

        persistence.remove(d);
        try {
            test = persistence.findById(d);
            fail("found deleted object " + d);
        } catch (NotFoundException e) {
            assertTrue(true);
        }
    }

    /**
     * test correctness of <code>synchronize</code>
     */
    public void testSynchronize() throws Exception {
        Item a = new Item(0,Item.HARDWARE);
        a.setOwner(1);
        a.setData("<name>make</name>");
        persistence.create(a);

        Record b = new Record(0,Record.SOFTWARE);
        b.setOwner(2);
        b.setData("<name>sw b</name>");
        persistence.create(b);

        Relation relationAB = new Relation(a,b,0);
        persistence.create(relationAB);
        a.addChildRelation(relationAB);

        Record c = new Record(0,Record.ARTICLE);
        c.setOwner(1);
        c.setData("<name>article c</name>");
        persistence.create(c);

        Relation relationAC = new Relation(a,c,0);
        persistence.create(relationAC);
        a.addChildRelation(relationAC);

        Category d = new Category(0);
        d.setOwner(2);
        d.setData("<name>section</name>");
        persistence.create(d);

        Item e = new Item(0, Item.BLOG);
        e.setOwner(3);
        e.setData("<name>section</name>");
        persistence.create(e);

        Relation relationDE = new Relation(d,e,0);
        persistence.create(relationDE);
        d.addChildRelation(relationDE);

        List relationList = new ArrayList();
        relationList.add(new Relation(relationAB.getId()));
        relationList.add(relationAC);
        relationList.add(new Relation(relationDE.getId()));

        a.setSubType("changed");
        b.setSubType("changed");
        c.setSubType("changed"); // only this change shall survive synchronizeList
        d.setSubType("changed");
        e.setSubType("changed");

        persistence.synchronizeList(relationList);
        List childrenList = new ArrayList();

        GenericObject fetched = ((Relation)relationList.get(0)).getChild();
        childrenList.add(fetched);
        assertEquals(b.getId(), fetched.getId());
        assertTrue(((GenericDataObject)fetched).getOwner()==0);
        assertNull(((GenericDataObject)fetched).getData());
        fetched = ((Relation)relationList.get(1)).getChild();
        childrenList.add(fetched);
        assertEquals(c.getId(), fetched.getId());
        assertTrue(((GenericDataObject)fetched).getOwner()==c.getOwner());
        assertNotNull(((GenericDataObject)fetched).getData());
        fetched = ((Relation)relationList.get(2)).getChild();
        childrenList.add(fetched);
        assertEquals(e.getId(), fetched.getId());
        assertTrue(((GenericDataObject)fetched).getOwner()==0);
        assertNull(((GenericDataObject)fetched).getData());

        persistence.synchronizeList(childrenList);

        fetched = ((Relation) relationList.get(0)).getChild();
        assertTrue(! b.getSubType().equals(((GenericDataObject)fetched).getSubType()));
        assertEquals(b.getOwner(), ((GenericDataObject) fetched).getOwner());
        assertNotNull(((GenericDataObject) fetched).getData());
        fetched = ((Relation) relationList.get(1)).getChild();
        assertEquals(c.getSubType(), ((GenericDataObject) fetched).getSubType());
        assertEquals(c.getOwner(), ((GenericDataObject) fetched).getOwner());
        assertNotNull(((GenericDataObject) fetched).getData());
        fetched = ((Relation) relationList.get(2)).getChild();
        assertTrue(!e.getSubType().equals(((GenericDataObject) fetched).getSubType()));
        assertEquals(e.getOwner(), ((GenericDataObject) fetched).getOwner());
        assertNotNull(((GenericDataObject) fetched).getData());
    }

    public void testFindByExample() throws Exception {
        Record a = new Record(0,Record.HARDWARE);
        a.setData("<name>HP DeskJet 840C</name>");
        a.setOwner(1);
        persistence.create(a);

        Record b = new Record(0,Record.HARDWARE);
        b.setData("<name>Lehponen XT</name>");
        b.setOwner(3);
        persistence.create(b);

        Record c = new Record(0,Record.SOFTWARE);
        c.setData("<name>Laserjet Control Panel</name>");
        c.setOwner(2);
        persistence.create(c);

        // find a and b, don't find c
        List examples = new ArrayList();
        Record qa = new Record(0);
        qa.setSearchString("%HP%");
        examples.add(qa);
        List found = persistence.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue(containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find only a
        examples.clear();
        qa.setSearchString("%HP%");
        qa.setOwner(1);
        examples.add(qa);
        found = persistence.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue( ! containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // qa finds a only, qb finds c only
        Record qb = new Record(0,Record.SOFTWARE);
        qb.setOwner(2);
        examples.add(qb);
        found = persistence.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue( ! containsId(found,b.getId()));
        assertTrue(containsId(found,c.getId()));

        // find b only
        examples.clear();
        qa.setOwner(0);
        examples.add(qa);
        Record qc = new Record(0,Record.HARDWARE);
        qc.setOwner(3);
        examples.add(qc);
        found = persistence.findByExample(examples,"0 AND 1");
        assertTrue( ! containsId(found,a.getId()));
        assertTrue( containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find b and c
        examples.add(qb);
        found = persistence.findByExample(examples,"(0 AND 1) OR 2 ORDER BY data ASC");
        assertTrue( ! containsId(found,a.getId()));
        assertTrue( containsId(found,b.getId()));
        assertTrue( containsId(found,c.getId()));

        persistence.remove(a);
        persistence.remove(b);
        persistence.remove(c);
    }

    public void testFindByCommand() throws Exception {
        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistence.create(processors);

        List list = persistence.findByCommand("select cislo from kategorie where cislo="+processors.getId());
        assertEquals(1,list.size());
        Object[] objects = (Object[]) list.get(0);
        assertEquals(1,objects.length);
        assertEquals(new Integer(processors.getId()),objects[0]);

        persistence.remove(processors);
    }

    public void testSyncList() throws Exception {
        Item item1 = new Item(0, Item.ARTICLE);
        item1.setData("<data>1</data>");
        item1.addProperty("property", "1");
        persistence.create(item1);
        Item item2 = new Item(0, Item.AUTHOR);
        item2.setData("<data>2</data>");
        item2.addProperty("property", "2");
        persistence.create(item2);
        Item item3 = new Item(0, Item.BAZAAR);
        item3.setData("<data>3</data>");
        item3.addProperty("property", "3");
        persistence.create(item3);
        Item item4 = new Item(0, Item.BLOG);
        item4.setData("<data>4</data>");
        item4.addProperty("property", "4");
        persistence.create(item4);

        try {
            Item cached1 = new Item(item1.getId());
            Item cached2 = new Item(item2.getId());
            Item cached3 = new Item(item3.getId());
            Item cached4 = new Item(item4.getId());

            List cachedList = new ArrayList();
            cachedList.add(cached1);
            cachedList.add(cached2);
            cachedList.add(cached3);
            cachedList.add(cached4);
            persistence.synchronizeList(cachedList);

            assertEquals(item1, cached1);
            assertEquals(item2, cached2);
            assertEquals(item3, cached3);
            assertEquals(item4, cached4);
            assertEquals(item1.getProperty("property"), cached1.getProperty("property"));
            assertEquals(item2.getProperty("property"), cached2.getProperty("property"));
            assertEquals(item3.getProperty("property"), cached3.getProperty("property"));
            assertEquals(item4.getProperty("property"), cached4.getProperty("property"));

            persistence.clearCache();

            Item fetched1 = new Item(item1.getId());
            Item fetched2 = new Item(item2.getId());
            Item fetched3 = new Item(item3.getId());
            Item fetched4 = new Item(item4.getId());

            List fetchedList = new ArrayList();
            fetchedList.add(fetched1);
            fetchedList.add(fetched2);
            fetchedList.add(fetched3);
            fetchedList.add(fetched4);
            persistence.synchronizeList(fetchedList);

            assertEquals(item1, fetched1);
            assertEquals(item2, fetched2);
            assertEquals(item3, fetched3);
            assertEquals(item4, fetched4);
            assertEquals(item1.getProperty("property"), fetched1.getProperty("property"));
            assertEquals(item2.getProperty("property"), fetched2.getProperty("property"));
            assertEquals(item3.getProperty("property"), fetched3.getProperty("property"));
            assertEquals(item4.getProperty("property"), fetched4.getProperty("property"));
        } finally {
            persistence.remove(item1);
            persistence.remove(item2);
            persistence.remove(item3);
            persistence.remove(item4);
        }
    }

    /**
     * tests functionality of tree
     */
    public void testTree() throws Exception {
        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistence.create(processors);

        Category intel = new Category(0);
        intel.setData("<name>Intel</name>");
        persistence.create(intel);

        Item duron = new Item(0,Item.HARDWARE);
        duron.setData("<name>Duron</name>");
        persistence.create(duron);

        Relation relProcDur = new Relation(processors,duron,0);
        persistence.create(relProcDur);
        processors.addChildRelation(relProcDur);

        Record duron1 = new Record(0,Record.HARDWARE);
        duron1.setData("<price>fine</price>");
        persistence.create(duron1);

        Relation relDurDur1 = new Relation(duron,duron1,relProcDur.getId());
        persistence.create(relDurDur1);
        duron.addChildRelation(relDurDur1);

        Item pentium = new Item(0,Item.HARDWARE);
        pentium.setData("<name>Pentium 4</name>");
        persistence.create(pentium);

        Relation relProcPent = new Relation(processors,pentium,0);
        persistence.create(relProcPent);
        processors.addChildRelation(relProcPent);

        Relation relIntPent = new Relation(intel,pentium,0);
        persistence.create(relIntPent);
        intel.addChildRelation(relIntPent);

        Record pentium1 = new Record(0,Record.HARDWARE);
        pentium1.setData("<price>expensive</price>");
        persistence.create(pentium1);

        Relation relPentPent1 = new Relation(pentium,pentium1,relProcPent.getId());
        persistence.create(relPentPent1);
        pentium.addChildRelation(relPentPent1);

        Record pentium2 = new Record(0,Record.HARDWARE);
        pentium2.setData("<price>too expensive</price>");
        persistence.create(pentium2);

        Relation relPentPent2 = new Relation(pentium,pentium2,relProcPent.getId());
        persistence.create(relPentPent2);
        pentium.addChildRelation(relPentPent2);

        processors = (Category) persistence.findById(processors);
        intel = (Category) persistence.findById(intel);
        duron = (Item) persistence.findById(duron);
        pentium = (Item) persistence.findById(pentium);

        // tests create()
        List content = processors.getChildren();
        assertEquals(2,content.size());
        GenericObject first = ((Relation)content.get(0)).getChild();
        GenericObject second = ((Relation)content.get(1)).getChild();
        assertTrue(first.getId()==duron.getId() || second.getId()==duron.getId());
        assertTrue(first.getId()==pentium.getId() || second.getId()==pentium.getId());

        content = intel.getChildren();
        assertEquals(1,content.size());
        first = ((Relation)content.get(0)).getChild();
        assertTrue(first.getId()==pentium.getId());

        content = duron.getChildren();
        assertEquals(1,content.size());
        first = ((Relation)content.get(0)).getChild();
        assertTrue(first.getId()==duron1.getId());

        content = pentium.getChildren();
        assertEquals(2,content.size());
        first = ((Relation)content.get(0)).getChild();
        second = ((Relation)content.get(1)).getChild();
        assertTrue(first.getId()==pentium1.getId() || second.getId()==pentium1.getId());
        assertTrue(first.getId()==pentium2.getId() || second.getId()==pentium2.getId());

        // tests remove()
        persistence.remove(relProcDur);
        processors.removeChildRelation(relProcDur);
        persistence.remove(relProcPent);
        processors.removeChildRelation(relProcPent);

        processors = (Category) persistence.findById(processors);
        intel = (Category) persistence.findById(intel);
        pentium = (Item) persistence.findById(pentium);

        content = processors.getChildren();
        assertEquals(0,content.size());

        content = intel.getChildren();
        assertEquals(1,content.size());

        try {
            duron = (Item) persistence.findById(duron);
            fail("found deleted object " + duron);
        } catch (NotFoundException e) {
            assertTrue(true);
        }

        // cleanup
        persistence.remove(relIntPent);
        intel.removeChildRelation(relIntPent);
        persistence.remove(processors);
        persistence.remove(intel);
    }

    public void testIncrement() throws Exception {
        Record a = new Record(0,Record.SOFTWARE);
        a.setData("<name>Disky</name>");
        persistence.create(a);

        persistence.incrementCounter(a, Constants.COUNTER_READ);
        assertEquals(1,persistence.getCounterValue(a, Constants.COUNTER_READ));

        persistence.incrementCounter(a, Constants.COUNTER_READ);
        persistence.incrementCounter(a, Constants.COUNTER_READ);
        persistence.incrementCounter(a, Constants.COUNTER_READ);
        assertEquals(4,persistence.getCounterValue(a, Constants.COUNTER_READ));

        persistence.removeCounter(a, Constants.COUNTER_READ);
        persistence.remove(a);
    }

    /**
     * Cache is transparent, so we can't test it directly. We can just test
     * possible places of problems.
     */
    public void testCache() throws Exception {
        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistence.create(processors);

        Category intel = new Category(0);
        intel.setData("<name>Intel</name>");
        persistence.create(intel);

        Relation relProcPent = new Relation(processors,intel,0);
        persistence.create(relProcPent);
        processors.addChildRelation(relProcPent);

        Item duron = new Item(0,Item.HARDWARE);
        duron.setData("<name>Duron</name>");
        persistence.create(duron);

        Relation relProcDur = new Relation(processors,duron,0);
        persistence.create(relProcDur);
        processors.addChildRelation(relProcDur);

        processors = (Category) persistence.findById(processors);
        List content = processors.getChildren();
        assertEquals(2,content.size());

        Category tmp = new Category(processors.getId());
        persistence.synchronize(tmp);
        content = processors.getChildren();
        assertEquals(2,content.size());

        tmp = (Category) persistence.findById(processors);
        content = processors.getChildren();
        assertEquals(2,content.size());

        persistence.remove(processors);
    }

    /**
     * Bug - when relation changes parent and is updated, the content
     * of previous and new parent in cache is not updated!
     */
    // Since Nursery introduction, this behaviour is normal. It is application developer responsibility
    // to remove child relation from old parent and add it to new parent.
//    public void testRelationCache() throws Exception {
//        Category first = new Category(); first.setData("<data/>");
//        Category second = new Category(); second.setData("<data/>");
//        Item item = new Item(); item.setData("<data/>");
//
//        persistence.create(first);
//        persistence.create(second);
//        persistence.create(item);
//
//        Relation relation = new Relation(first,item,0);
//        persistence.create(relation);
//        first.addChildRelation(relation);
//
//        Category cacheFirst = (Category) persistence.findById(first);
//        assertEquals(1,cacheFirst.getChildren().size());
//
//        relation.setParent(second);
//        persistence.update(relation);
//        cacheFirst = (Category) persistence.findById(first);
//        assertEquals(0,cacheFirst.getChildren().size());
//        Category cacheSecond = (Category) persistence.findById(second);
//        assertEquals(1,cacheSecond.getChildren().size());
//
//        persistence.remove(relation);
//        persistence.remove(item);
//        cacheSecond = (Category) persistence.findById(second);
//        assertEquals(0,cacheSecond.getChildren().size());
//
//        persistence.remove(first);
//        persistence.remove(second);
//    }

    /**
     * Searches list of GenericObjects for object with id equal to id.
     */
    protected boolean containsId(List list, int id) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            GenericObject object = (GenericObject) iter.next();
            if ( object.getId()==id ) return true;
        }
        return false;
    }
}
