/*
 * User: literakl
 * Date: 5.3.2004
 * Time: 20:17:40
 */
package cz.abclinuxu.persistance.cache;

import cz.abclinuxu.persistance.Cache;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.User;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unsynchronized cache, that stores only Users. For CreateIndex only.
 */
public class OnlyUserCache implements Cache {
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

    private class LRUMap extends LinkedHashMap {
        int MAX_SIZE;

        public LRUMap(int size) {
            super(size, 1.0f, true);
            MAX_SIZE = size;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size()>MAX_SIZE;
        }
    }
}
