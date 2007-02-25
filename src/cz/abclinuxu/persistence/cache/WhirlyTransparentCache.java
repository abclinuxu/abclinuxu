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
package cz.abclinuxu.persistence.cache;

import com.whirlycott.cache.Cache;
import com.whirlycott.cache.CacheException;
import com.whirlycott.cache.CacheManager;
import cz.abclinuxu.data.GenericObject;
import org.apache.log4j.Logger;

/**
 * Cache, that uses WhirlyCache as its backend.
 */
public class WhirlyTransparentCache extends AbstractCache {
    static Logger log = Logger.getLogger(WhirlyTransparentCache.class);
    Cache cache;

    public WhirlyTransparentCache() {
        try {
            cache = CacheManager.getInstance().getCache("transparent");
            log.info("WhirlyTransparentCache started");
        } catch (CacheException e) {
            log.fatal("Cannot start cache!", e);
        }
    }

    protected void storeObject(GenericObject obj) {
        cache.store(obj, obj);
    }

    protected GenericObject loadObject(GenericObject obj) {
        return (GenericObject) cache.retrieve(obj);
    }

    protected void removeObject(GenericObject obj) {
        cache.remove(obj);
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        cache.clear();
        log.info("WhirlyTransparentCache restarted");
    }
}
