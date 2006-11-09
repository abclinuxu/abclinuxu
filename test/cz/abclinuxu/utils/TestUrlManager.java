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
package cz.abclinuxu.utils;

import junit.framework.TestCase;
import cz.abclinuxu.servlets.utils.url.URLManager;

/**
 * @author literakl
 * @since 9.11.2006
 */
public class TestUrlManager extends TestCase {

    public void testLastUrlParts() throws Exception {
        assertEquals("abcd", URLManager.enforceRelativeURL("abcd"));
        assertEquals("-1abcd", URLManager.enforceRelativeURL("1abcd"));
        assertEquals("abcd", URLManager.enforceRelativeURL("/abcd///"));
        assertEquals("cplusplus", URLManager.enforceRelativeURL("c++"));
        assertEquals("c", URLManager.enforceRelativeURL("c-"));
        assertEquals("c", URLManager.enforceRelativeURL("c."));
        assertEquals("c", URLManager.enforceRelativeURL("c/"));
        assertEquals("c", URLManager.enforceRelativeURL("c=\\|,?%()*&$#@"));
        assertEquals("krizala", URLManager.enforceRelativeURL("KØÍ®ALA"));
    }

    public TestUrlManager(String s) {
        super(s);
    }
}
