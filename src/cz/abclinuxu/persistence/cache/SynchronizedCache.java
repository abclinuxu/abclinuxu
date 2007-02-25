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
import cz.abclinuxu.utils.LRUMap;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Cache, that is synchronized and uses standard LinkedHashMap as its backend.
 */
public class SynchronizedCache extends AbstractCache implements Configurable {
    static Logger log = Logger.getLogger(SynchronizedCache.class);

    public static final String PREF_SIZE = "size";
    /** default size of LRU cache */
    public static final int DEFAULT_SIZE = 1000;
    int size = DEFAULT_SIZE;
    Map data;

    public SynchronizedCache() {
        try {
            ConfigurationManager.getConfigurator().configureAndRememberMe(this);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        data = createMap();
        log.info("SynchronizedCache started with size "+size);
    }

    protected void storeObject(GenericObject obj) {
        data.put(obj, obj);
    }

    protected GenericObject loadObject(GenericObject obj) {
        return (GenericObject) data.get(obj);
    }

    protected void removeObject(GenericObject obj) {
        data.remove(obj);
    }

    /**
     * Instantiates new synchronized LRU map.
     */
    private Map createMap() {
        return Collections.synchronizedMap(new LRUMap(size));
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        data.clear();
        data = null;
        data = createMap();
        log.info("Cache restarted with size "+size);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        size = prefs.getInt(PREF_SIZE, DEFAULT_SIZE);
    }

}
