/*
 * User: literakl
 * Date: 19.7.2003
 * Time: 15:08:22
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.Record;
import cz.abclinuxu.servlets.edit.EditArticle;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;

/**
 * Miscalenous tests.
 */
public class VariousTest extends TestCase {
    static {
        System.setProperty("log4j.configuration", "/home/literakl/abc/deploy/WEB-INF/conf/log4j.xml");
        System.setProperty("abc.config", "/home/literakl/abc/deploy/WEB-INF/conf/systemPrefs.xml");
    }

    public VariousTest(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(VariousTest.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * EditArticle - pages test.
     */
    public void testArticleContent() throws Exception {
        Record record = new Record(0, Record.ARTICLE);
//        record.setData(DocumentHelper.createDocument());
        record.setData("<data><content>ahoj</content><content title=\"titulek\">cau</content></data>");
        Map params = new HashMap();
        String obsahPrvniStranky = "zacatek prvni stranky\npokracovani\nkonec prvni stranky";
        String obsahDruheStranky = "zacatek druhe stranky\npokracovani\nkonec druhe stranky";
        String obsahTretiStranky = "treti stranka";
        String content = "ignorovano\ni toto "+
                         "<page title=\"prvni\">"+obsahPrvniStranky+
                         "<page title=\"druha\">"+obsahDruheStranky+
                         "<page title=\"treti\">"+obsahTretiStranky;
        params.put(EditArticle.PARAM_CONTENT,content);

        EditArticle.setArticleContent(params,record,null);

        List nodes = record.getData().selectNodes("/data/content");
        assertEquals(3,nodes.size());
        Element element = (Element) nodes.get(0);
        assertEquals("prvni",element.attributeValue("title"));
        assertEquals(obsahPrvniStranky,element.getText());
        element = (Element) nodes.get(1);
        assertEquals("druha",element.attributeValue("title"));
        assertEquals(obsahDruheStranky, element.getText());
        element = (Element) nodes.get(2);
        assertEquals("treti",element.attributeValue("title"));
        assertEquals(obsahTretiStranky, element.getText());

        new XMLWriter(System.out).write(record.getData());
    }
}
