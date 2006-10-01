/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.data.view;

import junit.framework.TestCase;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;

/**
 * @author literakl
 * @since 21.1.2006
 */
public class TestTOC extends TestCase {

    public void testWalkLeftRight() throws Exception {
        Element root = DocumentHelper.createElement("data");
        Element a = root.addElement("node").addAttribute("rid", "1");
        Element b = a.addElement("node").addAttribute("rid", "2");
        Element c = a.addElement("node").addAttribute("rid", "3");
        Element d = a.addElement("node").addAttribute("rid", "4");
        b.addElement("node").addAttribute("rid", "5");
        b.addElement("node").addAttribute("rid", "6");
        c.addElement("node").addAttribute("rid", "7");
        c.addElement("node").addAttribute("rid", "8");
        d.addElement("node").addAttribute("rid", "9");
        d.addElement("node").addAttribute("rid", "10");
        TOC toc = new TOC(root);

        Chapter current = toc.getChapter(1);
        current = current.getRightChapter();
        assertEquals(2, current.getRid());
        current = current.getRightChapter();
        assertEquals(5, current.getRid());
        current = current.getRightChapter();
        assertEquals(6, current.getRid());
        current = current.getRightChapter();
        assertEquals(3, current.getRid());
        current = current.getRightChapter();
        assertEquals(7, current.getRid());
        current = current.getRightChapter();
        assertEquals(8, current.getRid());
        current = current.getRightChapter();
        assertEquals(4, current.getRid());
        current = current.getRightChapter();
        assertEquals(9, current.getRid());
        current = current.getRightChapter();
        assertEquals(10, current.getRid());
        assertNull(current.getRightChapter());

        current = current.getLeftChapter();
        assertEquals(9, current.getRid());
        current = current.getLeftChapter();
        assertEquals(4, current.getRid());
        current = current.getLeftChapter();
        assertEquals(8, current.getRid());
        current = current.getLeftChapter();
        assertEquals(7, current.getRid());
        current = current.getLeftChapter();
        assertEquals(3, current.getRid());
        current = current.getLeftChapter();
        assertEquals(6, current.getRid());
        current = current.getLeftChapter();
        assertEquals(5, current.getRid());
        current = current.getLeftChapter();
        assertEquals(2, current.getRid());
        current = current.getLeftChapter();
        assertEquals(1, current.getRid());
        assertNull(current.getLeftChapter());
    }

    public TestTOC(String string) {
        super(string);
    }
}
