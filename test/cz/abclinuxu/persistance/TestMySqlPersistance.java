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

        persistance.removeObject(a);
        persistance.removeObject(b);
        persistance.removeObject(c);
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
