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
package cz.abclinuxu.persistence.cache;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.LRUMap;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Unsynchronized cache, that stores only Users. For CreateIndex only.
 */
public class OnlyUserCache implements TransparentCache {
    static Logger log = Logger.getLogger(OnlyUserCache.class);
    int size = 5000;
    Map data;

    public OnlyUserCache() {
        data = createMap();
    }

    public void store(GenericObject obj) {
        if (obj instanceof User)
            data.put(obj, obj);
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * cached object, otherwise it returns null.
     * @return cached object
     */
    public GenericObject load(GenericObject obj) {
        GenericObject result = (GenericObject) data.get(obj);
        return result;
    }

    /**
     * If <code>obj</code> is deleted from persistant storage,
     * it is wise to delete it from cache too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
        data.remove(obj);
    }

    /**
     * Instantiates new synchronized LRU map.
     */
    private Map createMap() {
        return new LRUMap(size);
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        data.clear();
        data = null;
        data = createMap();
    }
}
