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
package cz.abclinuxu.utils;

import cz.abclinuxu.data.Record;
import cz.abclinuxu.servlets.html.edit.EditArticle;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.apache.regexp.RE;
import static cz.abclinuxu.servlets.Constants.PARAM_CONTENT;

/**
 * Miscallenous tests.
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

    public static void main(String[] args) throws Exception {
//        TestRunner.run(suite());
        VariousTest variousTest = new VariousTest(null);
        variousTest.testRegexpSubst();
    }

    public void testRegexpSubst() throws Exception {
//        String s = "http://localhost:8080/clanky/ViewRelation?relationId=53458";
//        String s = "http://localhost:8080/clanky/ViewRelation?rid=53458";
//        String s = "http://localhost:8080/";
//        String s = "http://localhost:8080/clanky/ViewCategory?rid=5";
        String s = "http://www.abclinuxu.cz/clanky/ViewRelation?varianta=print&rid=47338";
//        String pattern = "(ViewRelation?relationId=)([\\d]+)";
//        String pattern = "ViewRelation\\?(relationId|rid)=";
//        String pattern = "/$";
//        String pattern = "ViewCategory\\?(relationId|rid)=";
//        String pattern = "ViewCategory.+(relationId|rid)=([\\d]+).*";
        String pattern = "ViewRelation.+(relationId|rid)=([\\d]+).*";
//        String replace = "show/";
        String replace = "dir/$2";
        RE re = new RE(pattern);
//        String result = re.subst(s, replace, RE.REPLACE_FIRSTONLY);
        String result = re.subst(s, replace, RE.REPLACE_FIRSTONLY + RE.REPLACE_BACKREFERENCES);
        System.out.println("result = "+result);
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
        params.put(PARAM_CONTENT,content);

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
