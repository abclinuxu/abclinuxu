/*
 * User: literakl
 * Date: Nov 24, 2001
 * Time: 8:47:24 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.*;
import junit.framework.*;
import junit.textui.TestRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Level;
import cz.abclinuxu.data.*;
import cz.abclinuxu.exceptions.NotFoundException;

public class TestMySqlPersistance extends TestCase {

    Persistance persistance;

    public TestMySqlPersistance(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        super.setUp();
        persistance = new MySqlPersistance(PersistanceFactory.defaultTestUrl);
        persistance.setCache(new LRUCache());
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
        Item a = new Item(0,Item.MAKE);
        a.setOwner(1);
        a.setData("<name>make</name>");
        persistance.create(a);

        Record b = new Record(0,Record.SOFTWARE);
        b.setOwner(2);
        b.setData("<name>sw b</name>");
        persistance.create(b);
        Relation relation = new Relation(a,b,0);
        persistance.create(relation);

        Record c = new Record(0,Record.ARTICLE);
        c.setOwner(1);
        c.setData("<name>article c</name>");
        persistance.create(c);
        relation = new Relation(a,c,0);
        persistance.create(relation);

        Category d = new Category(0);
        d.setOwner(2);
        d.setData("<name>section</name>");
        persistance.create(d);
        relation = new Relation(d,c,0);
        persistance.create(relation);

        persistance.remove(a);

        // now only c and d shall exist
        GenericObject test = persistance.findById(c);
        assertTrue(c.preciseEquals(test));

        test = persistance.findById(d);
        assertTrue(d.preciseEquals(test));

        try {
            test = persistance.findById(a);
            fail("found deleted object " + a);
        } catch (NotFoundException e) {
            assertTrue( true );
        }

        try {
            test = persistance.findById(b);
            fail("found deleted object " + b);
        } catch (NotFoundException e) {
            assertTrue(true);
        }

        // now clean up database
        persistance.remove(relation);

        try {
            test = persistance.findById(c);
            fail("found deleted object " + c);
        } catch (NotFoundException e) {
            assertTrue(true);
        }

        persistance.remove(d);
        try {
            test = persistance.findById(d);
            fail("found deleted object " + d);
        } catch (NotFoundException e) {
            assertTrue(true);
        }
    }

    public void testFindByExample() throws Exception {
        Record a = new Record(0,Record.HARDWARE);
        a.setData("<name>HP DeskJet 840C</name>");
        a.setOwner(1);
        persistance.create(a);

        Record b = new Record(0,Record.HARDWARE);
        b.setData("<name>Lehponen XT</name>");
        b.setOwner(3);
        persistance.create(b);

        Record c = new Record(0,Record.SOFTWARE);
        c.setData("<name>Laserjet Control Panel</name>");
        c.setOwner(2);
        persistance.create(c);

        // find a and b, don't find c
        List examples = new ArrayList();
        Record qa = new Record(0);
        qa.setSearchString("%HP%");
        examples.add(qa);
        List found = persistance.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue(containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find only a
        examples.clear();
        qa.setSearchString("%HP%");
        qa.setOwner(1);
        examples.add(qa);
        found = persistance.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue( ! containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // qa finds a only, qb finds c only
        Record qb = new Record(0,Record.SOFTWARE);
        qb.setOwner(2);
        examples.add(qb);
        found = persistance.findByExample(examples,null);
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
        found = persistance.findByExample(examples,"0 AND 1");
        assertTrue( ! containsId(found,a.getId()));
        assertTrue( containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find b and c
        examples.add(qb);
        found = persistance.findByExample(examples,"(0 AND 1) OR 2 ORDER BY data ASC");
        assertTrue( ! containsId(found,a.getId()));
        assertTrue( containsId(found,b.getId()));
        assertTrue( containsId(found,c.getId()));

        persistance.remove(a);
        persistance.remove(b);
        persistance.remove(c);
    }

    public void testFindByCommand() throws Exception {
        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistance.create(processors);

        List list = persistance.findByCommand("select cislo from kategorie where cislo="+processors.getId());
        assertEquals(1,list.size());
        Object[] objects = (Object[]) list.get(0);
        assertEquals(1,objects.length);
        assertEquals(new Integer(processors.getId()),objects[0]);

        persistance.remove(processors);
    }

    /**
     * tests functionality of tree
     */
    public void testTree() throws Exception {
        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistance.create(processors);

        Category intel = new Category(0);
        intel.setData("<name>Intel</name>");
        persistance.create(intel);

        Item duron = new Item(0,Item.MAKE);
        duron.setData("<name>Duron</name>");
        persistance.create(duron);
        Relation relProcDur = new Relation(processors,duron,0);
        persistance.create(relProcDur);

        Record duron1 = new Record(0,Record.HARDWARE);
        duron1.setData("<price>fine</price>");
        persistance.create(duron1);
        Relation relDurDur1 = new Relation(duron,duron1,relProcDur.getId());
        persistance.create(relDurDur1);

        Item pentium = new Item(0,Item.MAKE);
        pentium.setData("<name>Pentium 4</name>");
        persistance.create(pentium);
        Relation relProcPent = new Relation(processors,pentium,0);
        persistance.create(relProcPent);
        Relation relIntPent = new Relation(intel,pentium,0);
        persistance.create(relIntPent);

        Record pentium1 = new Record(0,Record.HARDWARE);
        pentium1.setData("<price>expensive</price>");
        persistance.create(pentium1);
        Relation relPentPent1 = new Relation(pentium,pentium1,relProcPent.getId());
        persistance.create(relPentPent1);

        Record pentium2 = new Record(0,Record.HARDWARE);
        pentium2.setData("<price>too expensive</price>");
        persistance.create(pentium2);
        Relation relPentPent2 = new Relation(pentium,pentium2,relProcPent.getId());
        persistance.create(relPentPent2);

        processors = (Category) persistance.findById(processors);
        intel = (Category) persistance.findById(intel);
        duron = (Item) persistance.findById(duron);
        pentium = (Item) persistance.findById(pentium);

        // tests create()
        List content = processors.getContent();
        assertEquals(2,content.size());
        GenericObject first = ((Relation)content.get(0)).getChild();
        GenericObject second = ((Relation)content.get(1)).getChild();
        assertTrue(first.getId()==duron.getId() || second.getId()==duron.getId());
        assertTrue(first.getId()==pentium.getId() || second.getId()==pentium.getId());

        content = intel.getContent();
        assertEquals(1,content.size());
        first = ((Relation)content.get(0)).getChild();
        assertTrue(first.getId()==pentium.getId());

        content = duron.getContent();
        assertEquals(1,content.size());
        first = ((Relation)content.get(0)).getChild();
        assertTrue(first.getId()==duron1.getId());

        content = pentium.getContent();
        assertEquals(2,content.size());
        first = ((Relation)content.get(0)).getChild();
        second = ((Relation)content.get(1)).getChild();
        assertTrue(first.getId()==pentium1.getId() || second.getId()==pentium1.getId());
        assertTrue(first.getId()==pentium2.getId() || second.getId()==pentium2.getId());

        // tests remove()
        persistance.remove(relProcDur);
        persistance.remove(relProcPent);

        processors = (Category) persistance.findById(processors);
        intel = (Category) persistance.findById(intel);
        pentium = (Item) persistance.findById(pentium);

        content = processors.getContent();
        assertEquals(0,content.size());

        content = intel.getContent();
        assertEquals(1,content.size());

        try {
            duron = (Item) persistance.findById(duron);
            fail("found deleted object " + duron);
        } catch (NotFoundException e) {
            assertTrue(true);
        }

        // cleanup
        persistance.remove(relIntPent);
        persistance.remove(processors);
        persistance.remove(intel);
    }

    public void testIncrement() throws Exception {
        Record a = new Record(0,Record.SOFTWARE);
        a.setData("<name>Disky</name>");
        persistance.create(a);

        persistance.incrementCounter(a);
        assertEquals(1,persistance.getCounterValue(a));

        persistance.incrementCounter(a);
        persistance.incrementCounter(a);
        persistance.incrementCounter(a);
        assertEquals(4,persistance.getCounterValue(a));

        persistance.removeCounter(a);
        persistance.remove(a);
    }

    /**
     * Cache is transparent, so we can't test it directly. We can just test
     * possible places of problems.
     */
    public void testCache() throws Exception {
        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistance.create(processors);

        Category intel = new Category(0);
        intel.setData("<name>Intel</name>");
        persistance.create(intel);
        Relation relProcPent = new Relation(processors,intel,0);
        persistance.create(relProcPent);

        Item duron = new Item(0,Item.MAKE);
        duron.setData("<name>Duron</name>");
        persistance.create(duron);
        Relation relProcDur = new Relation(processors,duron,0);
        persistance.create(relProcDur);

        processors = (Category) persistance.findById(processors);
        List content = processors.getContent();
        assertEquals(2,content.size());

        Category tmp = new Category(processors.getId());
        persistance.synchronize(tmp);
        content = processors.getContent();
        assertEquals(2,content.size());

        tmp = (Category) persistance.findById(processors);
        content = processors.getContent();
        assertEquals(2,content.size());

        persistance.remove(processors);
    }

    /**
     * Bug - when relation changes parent and is updated, the content
     * of previous and new parent in cache is not updated!
     */
    public void testRelationCache() throws Exception {
        Category first = new Category(); first.setData("<data/>");
        Category second = new Category(); second.setData("<data/>");
        Item item = new Item(); item.setData("<data/>");

        persistance.create(first);
        persistance.create(second);
        persistance.create(item);

        Relation relation = new Relation(first,item,0);
        persistance.create(relation);

        Category cacheFirst = (Category) persistance.findById(first);
        assertEquals(1,cacheFirst.getContent().size());

        relation.setParent(second);
        persistance.update(relation);
        cacheFirst = (Category) persistance.findById(first);
        assertEquals(0,cacheFirst.getContent().size());
        Category cacheSecond = (Category) persistance.findById(second);
        assertEquals(1,cacheSecond.getContent().size());

        persistance.remove(relation);
        persistance.remove(item);
        cacheSecond = (Category) persistance.findById(second);
        assertEquals(0,cacheSecond.getContent().size());

        persistance.remove(first);
        persistance.remove(second);
    }

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
