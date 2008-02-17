/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.persistence.TestTags;
import cz.abclinuxu.data.view.ParsedDocument;
import cz.abclinuxu.data.Tag;

import java.util.Set;

/**
 * @author literakl
 * @since 17.2.2008
 */
public class TestTagTool extends TestTags {

    public void testDetectTags() throws Exception {
        Tag tagAti = new Tag("ati", "ATI");
        persistence.create(tagAti);
        Tag tagDevel = new Tag("programovani", "Programovani");
        persistence.create(tagDevel);
        Tag tagNetwork = new Tag("site", "Sítě");
        persistence.create(tagNetwork);
        Tag tagUbuntu = new Tag("ubuntu", "Ubuntu");
        persistence.create(tagUbuntu);
        Tag tagDebian = new Tag("debian", "Debian");
        persistence.create(tagDebian);
        Tag tagVideo = new Tag("video", "Video");
        persistence.create(tagVideo);

        ParsedDocument doc = new ParsedDocument("Programování ATI ovladačů je v ubuntu hračka, narozdíl od debianu.Video!\rSite.");
        Set<String> found = TagTool.detectTags(doc);
        assertTrue(found.contains(tagDevel.getId()));
        assertTrue(found.contains(tagAti.getId()));
        assertTrue(found.contains(tagUbuntu.getId()));
        assertTrue(found.contains(tagVideo.getId()));
        assertTrue(found.contains(tagNetwork.getId()));
        assertEquals(5, found.size());
    }

    protected void setUp() throws Exception {
        super.setUp();
        TagTool.init();
    }
}
