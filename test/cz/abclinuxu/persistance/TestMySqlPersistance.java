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
import org.apache.log4j.xml.DOMConfigurator;
import cz.abclinuxu.data.*;
import cz.abclinuxu.AbcException;

public class TestMySqlPersistance extends TestCase {

    Persistance persistance;

    static {
        DOMConfigurator.configure("WEB-INF/log4j.xml");
    }

    public TestMySqlPersistance(String s) {
        super(s);
    }

    protected void setUp() throws Exception {
        super.setUp();
        persistance = new MySqlPersistance("jdbc:mysql://localhost/unit?user=literakl");
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
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();

        Record a = new HardwareRecord(0);
        a.setOwner(1);
        a.setData("hw a");
        persistance.create(a,null);

        Record b = new SoftwareRecord(0);
        b.setOwner(2);
        b.setData("sw b");
        persistance.create(b,a);

        Record c = new ArticleRecord(0);
        c.setOwner(1);
        c.setData("article c");
        persistance.create(c,a);

        Record d = new SoftwareRecord(0);
        d.setOwner(2);
        d.setData("sw d");
        persistance.create(d,null);
        persistance.create(c,d);

        persistance.remove(a,null);

        // now only c and d shall exist
        GenericObject test = persistance.findById(c);
        assertEquals(c,test);

        test = persistance.findById(d);
        assertEquals(d,test);

        try {
            test = persistance.findById(a);
            fail("found deleted object " + a);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        try {
            test = persistance.findById(b);
            fail("found deleted object " + b);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        // now clean up database
        persistance.remove(c,d);

        try {
            test = persistance.findById(c);
            fail("found deleted object " + c);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        persistance.remove(d,null);
        try {
            test = persistance.findById(d);
            fail("found deleted object " + d);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        org.apache.log4j.Category.getDefaultHierarchy().enableAll();
    }

    public void testFindByExample() throws Exception {
        Record a = new HardwareRecord(0);
        a.setData("HP DeskJet 840C");
        a.setOwner(1);
        persistance.create(a,null);

        Record b = new HardwareRecord(0);
        b.setData("Lehponen XT");
        b.setOwner(3);
        persistance.create(b,null);

        Record c = new SoftwareRecord(0);
        c.setData("Laserjet II");
        c.setOwner(2);
        persistance.create(c,null);

        // find a and b, don't find c
        List examples = new ArrayList();
        Record qa = new Record(0);
        qa.setData("%HP%");
        examples.add(qa);
        List found = persistance.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue(containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find only a
        examples.clear();
        qa.setData("%HP%");
        qa.setOwner(1);
        examples.add(qa);
        found = persistance.findByExample(examples,null);
        assertTrue(containsId(found,a.getId()));
        assertTrue( ! containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // qa finds a only, qb finds c only
        SoftwareRecord qb = new SoftwareRecord(0);
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
        HardwareRecord qc = new HardwareRecord(0);
        qc.setOwner(3);
        examples.add(qc);
        found = persistance.findByExample(examples,"0 AND 1");
        assertTrue( ! containsId(found,a.getId()));
        assertTrue( containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find b and c
        examples.add(qb);
        found = persistance.findByExample(examples,"(0 AND 1) OR 2");
        assertTrue( ! containsId(found,a.getId()));
        assertTrue( containsId(found,b.getId()));
        assertTrue( containsId(found,c.getId()));

        persistance.remove(a,null);
        persistance.remove(b,null);
        persistance.remove(c,null);
    }

    /**
     * tests functionality of tree
     */
    public void testTree() throws Exception {
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();

        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistance.create(processors,null);

        Category intel = new Category(0);
        intel.setData("<name>Intel</name>");
        persistance.create(intel,null);

        Make duron = new Make(0);
        duron.setData("<name>Duron</name>");
        persistance.create(duron,processors);

        HardwareRecord duron1 = new HardwareRecord(0);
        duron1.setData("<price>fine</price>");
        persistance.create(duron1,duron);

        Make pentium = new Make(0);
        pentium.setData("<name>Pentium 4</name>");
        persistance.create(pentium,processors);
        persistance.create(pentium,intel);

        HardwareRecord pentium1 = new HardwareRecord(0);
        pentium1.setData("<price>expensive</price>");
        persistance.create(pentium1,pentium);

        HardwareRecord pentium2 = new HardwareRecord(0);
        pentium2.setData("<price>too expensive</price>");
        persistance.create(pentium2,pentium);

        processors = (Category) persistance.findById(processors);
        intel = (Category) persistance.findById(intel);
        duron = (Make) persistance.findById(duron);
        pentium = (Make) persistance.findById(pentium);

        // tests create()
        List content = processors.getContent();
        assertEquals(content.size(),2);
        GenericObject first = (GenericObject)content.get(0);
        GenericObject second = (GenericObject)content.get(1);
        assertTrue(first.getId()==duron.getId() || second.getId()==duron.getId());
        assertTrue(first.getId()==pentium.getId() || second.getId()==pentium.getId());

        content = intel.getContent();
        assertEquals(content.size(),1);
        first = (GenericObject)content.get(0);
        assertTrue(first.getId()==pentium.getId());

        content = duron.getContent();
        assertEquals(content.size(),1);
        first = (GenericObject)content.get(0);
        assertTrue(first.getId()==duron1.getId());

        content = pentium.getContent();
        assertEquals(content.size(),2);
        first = (GenericObject)content.get(0);
        second = (GenericObject)content.get(1);
        assertTrue(first.getId()==pentium1.getId() || second.getId()==pentium1.getId());
        assertTrue(first.getId()==pentium2.getId() || second.getId()==pentium2.getId());

        // tests remove()
        persistance.remove(duron,processors);
        persistance.remove(pentium,processors);

        processors = (Category) persistance.findById(processors);
        intel = (Category) persistance.findById(intel);
        pentium = (Make) persistance.findById(pentium);

        content = processors.getContent();
        assertEquals(content.size(),0);

        content = intel.getContent();
        assertEquals(content.size(),1);

        try {
            duron = (Make) persistance.findById(duron);
            fail("found deleted object " + duron);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        // cleanup
        persistance.remove(pentium,intel);
        persistance.remove(processors,null);
        persistance.remove(intel,null);

        org.apache.log4j.Category.getDefaultHierarchy().enableAll();
    }

    public void testIncrement() throws Exception {
        SoftwareRecord a = new SoftwareRecord(0);
        a.setData("<name>Disky</name>");
        persistance.create(a,null);

        persistance.incrementCounter(a);
        assertEquals(1,persistance.getCounterValue(a));

        persistance.incrementCounter(a);
        persistance.incrementCounter(a);
        persistance.incrementCounter(a);
        assertEquals(4,persistance.getCounterValue(a));

        persistance.removeCounter(a);
        persistance.remove(a,null);
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
