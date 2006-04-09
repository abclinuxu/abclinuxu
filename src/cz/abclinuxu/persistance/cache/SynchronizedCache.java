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
package cz.abclinuxu.persistance.cache;

import cz.abclinuxu.persistance.Cache;
import cz.abclinuxu.persistance.Nursery;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.LRUMap;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Relation;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Cache, that is synchronized and uses standard LinkedHashMap as its backend.
 */
public class SynchronizedCache implements Cache, Configurable {
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
        log.info("Cache started with size "+size);
    }

    /**
     * This method stores copy of object into cache, so it can be retrieved
     * later without queueing database. If <code>obj</code> with same
     * PK is already stored in the cache, it is replaced by new version.
     */
    public void store(GenericObject obj) {
        try {
            GenericObject key = (GenericObject) obj.getClass().newInstance();
            key.synchronizeWith(obj);
            if ( key instanceof Relation ) {
                Relation rel = (Relation) key;
                rel.setParent(rel.getParent().makeLightClone());
                rel.setChild(rel.getChild().makeLightClone());
            }
            data.put(key, key);
        } catch (Exception e) {
            log.error("Cloning failed", e);
        }
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * cached object, otherwise it returns null.
     * @return cached object
     */
    public GenericObject load(GenericObject obj) {
        GenericObject result = (GenericObject) data.get(obj);
        if ( result==null )
            return null;
        if ( result instanceof Relation ) {
            Relation rel = new Relation((Relation)result);
            rel.setParent(rel.getParent().makeLightClone());
            rel.setChild(rel.getChild().makeLightClone());
            result = rel;
        } else {
            try {
                GenericObject o = (GenericObject) result.getClass().newInstance();
                o.synchronizeWith(result);
                result = o;
            } catch (Exception e) {
                log.error("Cannot clone "+result, e);
            }
        }
        return result;
    }

    /**
     * If <code>obj</code> is deleted from persistant storage,
     * it is wise to delete it from cache too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
        data.remove(obj);
        // todo this shall be handled at application level. why it doesn't print anything to logs
        if ( obj instanceof Relation )
            Nursery.getInstance().removeChild((Relation) obj);
        else
            Nursery.getInstance().removeParent(obj);
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
