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
package cz.abclinuxu.persistence.impl;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.User;

/**
 * Helper that knows which objects can have properties.
 * This is used by persistence to optimalize loading
 * data from database.
 * @author literakl
 * @since 18.3.2007
 */
public class PropertiesConfig {

    /**
     * Tests if the object can have properties.
     * @param obj initialized GenericObject
     * @return true if properties are supported
     */
    public static boolean isSupported(GenericObject obj) {
        if (obj instanceof User)
            return true;
        if (! (obj instanceof Item))
            return false;
        if (! obj.isInitialized())
            return true;
        switch (((Item)obj).getType()) {
            case Item.ARTICLE:
            case Item.AUTHOR:
            case Item.BLOG:
            case Item.SOFTWARE: return true;
        }
        return false;
    }
}
