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
        persistance = PersistanceFactory.getPersistance("jdbc:mysql://localhost/unit?user=literakl");
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
        persistance.storeObject(a);

        Record b = new SoftwareRecord(0);
        b.setOwner(2);
        b.setData("sw b");
        persistance.storeObject(b);

        Record c = new ArticleRecord(0);
        c.setOwner(1);
        c.setData("article c");
        persistance.storeObject(c);

        Record d = new SoftwareRecord(0);
        d.setOwner(2);
        d.setData("sw d");
        persistance.storeObject(d);

        persistance.addObjectToTree(b,a);
        persistance.addObjectToTree(c,a);
        persistance.addObjectToTree(c,d);

        persistance.removeObject(a);

        // now only c and d shall exist
        GenericObject test = persistance.loadObject(c);
        assertEquals(c,test);

        test = persistance.loadObject(d);
        assertEquals(d,test);

        try {
            test = persistance.loadObject(a);
            fail("found deleted object " + a);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        try {
            test = persistance.loadObject(b);
            fail("found deleted object " + b);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        // now clean up database
        persistance.removeObject(c);
        persistance.removeObject(d);

        try {
            test = persistance.loadObject(c);
            fail("found deleted object " + c);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        try {
            test = persistance.loadObject(d);
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
        persistance.storeObject(a);

        Record b = new HardwareRecord(0);
        b.setData("Lehponen XT");
        b.setOwner(3);
        persistance.storeObject(b);

        Record c = new SoftwareRecord(0);
        c.setData("Laserjet II");
        c.setOwner(2);
        persistance.storeObject(c);

        // find a and b, don't find c
        List examples = new ArrayList();
        Record qa = new Record(0);
        qa.setData("%HP%");
        examples.add(qa);
        List found = persistance.findByExample(examples);
        assertTrue(containsId(found,a.getId()));
        assertTrue(containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // find only a
        examples.clear();
        qa.setData("%HP%");
        qa.setOwner(1);
        examples.add(qa);
        found = persistance.findByExample(examples);
        assertTrue(containsId(found,a.getId()));
        assertTrue( ! containsId(found,b.getId()));
        assertTrue( ! containsId(found,c.getId()));

        // qa finds a only, qb finds c only
        SoftwareRecord qb = new SoftwareRecord(0);
        qb.setOwner(2);
        examples.add(qb);
        found = persistance.findByExample(examples);
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

        persistance.removeObject(a);
        persistance.removeObject(b);
        persistance.removeObject(c);
    }

    /**
     * tests functionality of tree
     */
    public void testTree() throws Exception {
        org.apache.log4j.Category.getDefaultHierarchy().disableAll();

        Category processors = new Category(0);
        processors.setData("<name>Processors</name>");
        persistance.storeObject(processors);

        Category intel = new Category(0);
        intel.setData("<name>Intel</name>");
        persistance.storeObject(intel);

        Make duron = new Make(0);
        duron.setData("<name>Duron</name>");
        persistance.storeObject(duron);

        HardwareRecord duron1 = new HardwareRecord(0);
        duron1.setData("<price>fine</price>");
        persistance.storeObject(duron1);

        Make pentium = new Make(0);
        pentium.setData("<name>Pentium 4</name>");
        persistance.storeObject(pentium);

        HardwareRecord pentium1 = new HardwareRecord(0);
        pentium1.setData("<price>expensive</price>");
        persistance.storeObject(pentium1);

        HardwareRecord pentium2 = new HardwareRecord(0);
        pentium2.setData("<price>too expensive</price>");
        persistance.storeObject(pentium2);

        persistance.addObjectToTree(duron,processors);
        persistance.addObjectToTree(pentium,processors);
        persistance.addObjectToTree(pentium,intel);
        persistance.addObjectToTree(duron1,duron);
        persistance.addObjectToTree(pentium1,pentium);
        persistance.addObjectToTree(pentium2,pentium);

        processors = (Category) persistance.loadObject(processors);
        intel = (Category) persistance.loadObject(intel);
        duron = (Make) persistance.loadObject(duron);
        pentium = (Make) persistance.loadObject(pentium);

        // tests addObjectToTree and loadObject
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

        // tests removeObjectFromTree and removeObject
        persistance.removeObjectFromTree(duron,processors);
        persistance.removeObjectFromTree(pentium,processors);

        processors = (Category) persistance.loadObject(processors);
        intel = (Category) persistance.loadObject(intel);
        pentium = (Make) persistance.loadObject(pentium);

        content = processors.getContent();
        assertEquals(content.size(),0);

        content = intel.getContent();
        assertEquals(content.size(),1);

        try {
            duron = (Make) persistance.loadObject(duron);
            fail("found deleted object " + duron);
        } catch (PersistanceException e) {
            assertTrue( e.getStatus()==AbcException.DB_NOT_FOUND );
        }

        // cleanup
        persistance.removeObjectFromTree(pentium,intel);
        persistance.removeObject(processors);
        persistance.removeObject(intel);

        org.apache.log4j.Category.getDefaultHierarchy().enableAll();
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
