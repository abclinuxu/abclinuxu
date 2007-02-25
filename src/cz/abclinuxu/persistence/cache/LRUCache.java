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
import cz.abclinuxu.persistence.lru.CacheLRU;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.prefs.Preferences;

/**
 * This cache uses LRU policy. It is backed up
 * by CacheLRU from Jakarta's ORO project.
 */
public class LRUCache extends AbstractCache implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LRUCache.class);

    /** preference key for default size of LRUCache */
    public static final String PREF_SIZE = "size";
    /** default size of LRU cache */
    public static final int DEFAULT_SIZE = 1000;
    int size = DEFAULT_SIZE;
    CacheLRU data;

    /**
     * Default constructor with default maximum capacity of cache.
     */
    public LRUCache() {
        try {
            ConfigurationManager.getConfigurator().configureAndRememberMe(this);
        } catch (ConfigurationException e) { log.error(e); }
        data = new CacheLRU(size);
        log.info("LRUCache started with size "+size);
    }

    /**
     * Default constructor with given maximum capacity of cache.
     */
    public LRUCache(int aSize) {
        size = aSize;
        data = new CacheLRU(size);
    }

    protected void storeObject(GenericObject obj) {
        data.addElement(obj, obj);
    }

    protected GenericObject loadObject(GenericObject obj) {
        return (GenericObject) data.getElement(obj);
    }

    protected void removeObject(GenericObject obj) {
        data.removeElement(obj);
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        data.clear();
        data = null;
        data = new CacheLRU(size);
        log.info("Cache restarted with size "+size);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        size = prefs.getInt(PREF_SIZE,DEFAULT_SIZE);
    }
}
