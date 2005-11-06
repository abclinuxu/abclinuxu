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
package cz.abclinuxu.persistance.versioning;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import java.util.List;
import java.util.Random;

/**
 * User: literakl
 * Date: 27.3.2005
 */
public class TestMysqlProvider extends TestCase {

    /**
     * Performs test of all interface method.
     * @throws Exception
     */
    public void testIt() throws Exception {
        Random random = new Random(System.currentTimeMillis());
        String identification = "a"+random.nextInt();
        Versioning api = new MysqlVersioningProvider();

        Document document = DocumentHelper.createDocument();
        document.addElement(identification).setText("a");

        VersionInfo info1 = api.commit(document.asXML(), identification, "user1");
        assertEquals("user1", info1.getUser());

        VersionedDocument versionedDocument = api.load(identification, info1.getVersion());
        Document fetched = DocumentHelper.parseText(versionedDocument.getDocument());
        assertEquals("a", fetched.getRootElement().getText());

        List versions = api.getHistory(identification);
        assertEquals(1, versions.size());
        assertEquals(info1, versions.get(0));

        document.getRootElement().setText("aa");
        VersionInfo info2 = api.commit(document.asXML(), identification, "user2");
        assertEquals("user2", info2.getUser());

        versionedDocument = api.load(identification, info2.getVersion());
        fetched = DocumentHelper.parseText(versionedDocument.getDocument());
        assertEquals("aa", fetched.getRootElement().getText());

        List history = api.getHistory(identification);
        assertEquals(2, history.size());

        VersionInfo info1a = (VersionInfo) history.get(0);
        assertEquals(info1, info1a);
        versionedDocument = api.load(identification, info1a.getVersion());
        fetched = DocumentHelper.parseText(versionedDocument.getDocument());
        assertEquals("a", fetched.getRootElement().getText());

        VersionInfo info2a = (VersionInfo) history.get(1);
        assertEquals(info2, info2a);
        versionedDocument = api.load(identification, info2a.getVersion());
        fetched = DocumentHelper.parseText(versionedDocument.getDocument());
        assertEquals("aa", fetched.getRootElement().getText());
    }

    public TestMysqlProvider(String s) {
        super(s);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestMysqlProvider.class);
        return suite;
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
