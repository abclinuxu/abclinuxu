/*
 * User: literakl
 * Date: Dec 4, 2001
 * Time: 7:44:15 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.utils;

import java.util.List;
import java.util.ArrayList;
import junit.framework.*;
import junit.textui.TestRunner;
import cz.abclinuxu.persistance.TestMySqlPersistance;
import cz.abclinuxu.persistance.LogicalExpressionTokenizer;

public class TestLogicalExpressionTokenizer extends TestCase {

    public TestLogicalExpressionTokenizer(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestLogicalExpressionTokenizer.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestLogicalExpressionTokenizer.suite());
    }

    public void testTokenizer() {
        LogicalExpressionTokenizer tokenizer = new LogicalExpressionTokenizer("0");
        assertEquals("0",tokenizer.nextToken());
        assertNull(tokenizer.nextToken());

        tokenizer = new LogicalExpressionTokenizer("0 OR 1");
        assertEquals("0",tokenizer.nextToken());
        assertEquals("OR",tokenizer.nextToken());
        assertEquals("1",tokenizer.nextToken());
        assertNull(tokenizer.nextToken());

        tokenizer = new LogicalExpressionTokenizer(" 0 OR  (1 AND 2)");
        assertEquals("0",tokenizer.nextToken());
        assertEquals("OR",tokenizer.nextToken());
        assertEquals("(",tokenizer.nextToken());
        assertEquals("1",tokenizer.nextToken());
        assertEquals("AND",tokenizer.nextToken());
        assertEquals("2",tokenizer.nextToken());
        assertEquals(")",tokenizer.nextToken());
        assertNull(tokenizer.nextToken());
    }

    public void testMakeOrRelation() {
        List objects = new ArrayList();
        objects.add("a");
        String result = LogicalExpressionTokenizer.makeOrRelation(objects);
        assertEquals("0",result);

        objects.add("b");
        result = LogicalExpressionTokenizer.makeOrRelation(objects);
        assertEquals("0 OR 1",result);

        objects.add("c");
        result = LogicalExpressionTokenizer.makeOrRelation(objects);
        assertEquals("0 OR 1 OR 2",result);

        objects.add("d");
        result = LogicalExpressionTokenizer.makeOrRelation(objects);
        assertEquals("0 OR 1 OR 2 OR 3",result);
    }
}
