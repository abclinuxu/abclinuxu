/*
 *  Copyright (C) 2007 Leos Literak
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

import cz.abclinuxu.persistence.cache.LRUCache;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Tag;
import cz.abclinuxu.exceptions.DuplicateKeyException;
import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author literakl
 * @since 16.9.2007
 */
public class TestTags extends TestCase {
    Persistence persistence;

    /**
     * Tests complete tag functionality
     * @throws Exception something was wrong
     */
    public void testAll() throws Exception {
        Item itemNvidia = new Item(0, Item.CONTENT);
        itemNvidia.setData("<data><title>nvidia otazka</title><content>nv je proprietarni ovladac, nouveau je open source</content></data>");
        persistence.create(itemNvidia);

        Item itemAti = new Item(0, Item.CONTENT);
        itemAti.setData("<data><title>ati otazka</title><content>ovladac ati nejde zkompilovat v debianu pres php</content></data>");
        persistence.create(itemAti);

        Item itemDevel = new Item(0, Item.CONTENT);
        itemDevel.setData("<data><title>programovaci otazka</title><content>make vola gcc pri kompilaci pascalu</content></data>");
        persistence.create(itemDevel);

        Item itemNetwork = new Item(0, Item.CONTENT);
        itemNetwork.setData("<data><title>dokument o sitich</title><content>editace hosts v ubuntu nesouvisi s djbdns</content></data>");
        persistence.create(itemNetwork);

        Item itemUbuntu = new Item(0, Item.CONTENT);
        itemUbuntu.setData("<data><title>dokument o kubuntu</title><content>kde a ubuntu se rovna kubuntu</content></data>");
        persistence.create(itemUbuntu);

        Item itemDebian = new Item(0, Item.CONTENT);
        itemDebian.setData("<data><title>dokument o debianu</title><content>debian je predek pro ubuntu</content></data>");
        persistence.create(itemDebian);

        Item itemVideo = new Item(0, Item.CONTENT);
        itemVideo.setData("<data><title>video dokument</title><content>sit neni potreba pro mplayer</content></data>");
        persistence.create(itemVideo);

        assertEquals(0, persistence.getTags().size());

        Tag tagNvidia = new Tag("nvidia", "NVidia");
        persistence.create(tagNvidia);

        assertEquals(1, persistence.getTags().size());

        try {
            persistence.create(tagNvidia);
            fail("duplicate tag exception expected");
        } catch (DuplicateKeyException e) {
            // expected
        }

        Tag tagAti = new Tag("ati", "ATI");
        persistence.create(tagAti);

        Tag tagDevel = new Tag("programovani", "Programovani");
        persistence.create(tagDevel);

        Tag tagNetwork = new Tag("site", "Sitarina");
        persistence.create(tagNetwork);
        tagNetwork.setTitle("Site");
        persistence.update(tagNetwork);

        Map<String, Tag> tags = persistence.getTags();
        Tag tagNetwork2 = tags.get(tagNetwork.getId());
        assertNotNull(tagNetwork2);
        assertEquals("Site", tagNetwork2.getTitle());

        Tag tagUbuntu = new Tag("ubuntu", "Ubuntu");
        persistence.create(tagUbuntu);

        Tag tagDebian = new Tag("debian", "Debian");
        persistence.create(tagDebian);

        Tag tagVideo = new Tag("video", "Video");
        persistence.create(tagVideo);

        persistence.assignTags(itemNvidia, toList(tagNvidia.getId()));
        persistence.assignTags(itemAti, toList(tagAti.getId(), tagDebian.getId()));
        persistence.assignTags(itemDevel, toList(tagDevel.getId()));
        persistence.assignTags(itemNetwork, toList(tagNetwork.getId(), tagUbuntu.getId(), tagDebian.getId()));
        persistence.assignTags(itemUbuntu, toList(tagUbuntu.getId()));
        persistence.assignTags(itemDebian, toList(tagDebian.getId(), tagUbuntu.getId()));
        persistence.assignTags(itemVideo, toList(tagVideo.getId()));

        persistence.unassignTags(itemNetwork, toList(tagDebian.getId()));

        persistence.remove(tagVideo);

        tags = persistence.getTags();
        tagVideo = tags.get(tagVideo.getId());
        assertNull(tagVideo);

        assertEquals(6, tags.size());
        assertEquals(1, tags.get(tagNvidia.getId()).getUsage());
        assertEquals(2, tags.get(tagDebian.getId()).getUsage());
        assertEquals(3, tags.get(tagUbuntu.getId()).getUsage());

        List<String> develTags = persistence.getAssignedTags(itemDevel);
        assertTrue(develTags.contains(tagDevel.getId()));

        List<String> develNetwork = persistence.getAssignedTags(itemNetwork);
        assertTrue(develNetwork.contains(tagNetwork.getId()));
        assertTrue(develNetwork.contains(tagUbuntu.getId()));
        assertFalse(develNetwork.contains(tagDebian.getId()));

        persistence.remove(itemUbuntu);

        tags = persistence.getTags();
        assertEquals(2, tags.get(tagUbuntu.getId()).getUsage());
    }

    /**
     * clean up before test
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();
        persistence = PersistenceFactory.getPersistence(PersistenceFactory.defaultTestUrl, LRUCache.class);

        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = ((MySqlPersistence) persistence).getSQLConnection();
            statement = con.createStatement();
            statement.executeUpdate("delete from stitek");
            statement.executeUpdate("delete from stitkovani");
        } finally {
            ((MySqlPersistence) persistence).releaseSQLResources(con, statement, resultSet);
        }
    }

    private List<String> toList(String... args) {
        List<String> list = new ArrayList<String>(args.length);
        for (String s : args) {
            list.add(s);
        }
        return list;
    }
}
