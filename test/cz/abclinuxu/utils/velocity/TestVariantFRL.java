/*
 * User: literakl
 * Date: 21.8.2002
 * Time: 11:43:00
 * (c) 2001-2002 Tinnio
 */
package cz.abclinuxu.utils.velocity;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Unit test for VariantFileResourceLoader
 */
public class TestVariantFRL extends TestCase {
    File home,root,web,lynx,web_header,lynx_header,web_add,web_add_category;
    String contentLynxHeader = "lynx/header.vm";
    String contentWebHeader = "web/header.vm";
    String contentWebCategory = "web/add/category.vm";
    String contentLynxCategory = "lynx/add/category.vm";

    /**
     * creates few directories in user home
     */
    protected void setUp() throws Exception {
        super.setUp();
        home = new File(System.getProperty("user.home"));

        root = new File(home,"unit");
        root.mkdir();
        root.deleteOnExit();

        web = new File(root,"web");
        web.mkdir();
        web.deleteOnExit();

        lynx = new File(root,"lynx");
        lynx.mkdir();
        lynx.deleteOnExit();

        web_header = new File(web,"header.vm");
        addContent(web_header,contentWebHeader);
        web_header.deleteOnExit();

        lynx_header = new File(lynx,"header.vm");
        addContent(lynx_header,contentLynxHeader);
        lynx_header.deleteOnExit();

        web_add = new File(web,"add");
        web_add.mkdir();
        web_add.deleteOnExit();

        web_add_category = new File(web_add,"category.vm");
        addContent(web_add_category,contentWebCategory);
        web_add_category.deleteOnExit();
    }

    /**
     * Performs complete test
     */
    public void testComplete() throws Exception {
        Properties p = new Properties();
        p.setProperty("resource.loader","my");
        p.setProperty("my.resource.loader.class","cz.abclinuxu.utils.velocity.VariantFileResourceLoader");
        p.setProperty("my.resource.loader.path",root.getAbsolutePath());
        p.setProperty("my.resource.loader.default","web");

        VelocityEngine ve = new VelocityEngine();
        VelocityContext vc = new VelocityContext();
        ve.init(p);

        StringWriter sw = new StringWriter();
        ve.mergeTemplate(contentWebHeader,vc,sw);
        assertEquals(contentWebHeader,sw.toString());

        sw = new StringWriter();
        ve.mergeTemplate(contentLynxHeader,vc,sw);
        assertEquals(contentLynxHeader,sw.toString());
        sw.close();

        sw = new StringWriter();
        ve.mergeTemplate(contentWebCategory,vc,sw);
        assertEquals(contentWebCategory,sw.toString());
        sw.close();

        sw = new StringWriter();
        ve.mergeTemplate(contentLynxCategory,vc,sw);
        assertEquals(contentWebCategory,sw.toString());
        sw.close();

        try {
            ve.mergeTemplate("lynx/add/nonexistent.vm",vc,sw);
            fail("It found non existing resource!");
        } catch (ResourceNotFoundException e) {}
    }

    public TestVariantFRL(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestVariantFRL.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(TestVariantFRL.suite());
    }

    /**
     * Inserts specified content into desired file.
     */
    private void addContent(File file, String content) throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }
}
