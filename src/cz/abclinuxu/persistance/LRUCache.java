/*
 * User: Leos Literak
 * Date: May 29, 2003
 * Time: 8:55:15 PM
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.persistance.lru.CacheLRU;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.prefs.Preferences;

/**
 * This cache uses LRU policy. It is backed up
 * by CacheLRU from Jakarta's ORO project.
 */
public class LRUCache implements Cache,Configurable {
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
    }

    /**
     * Default constructor with given maximum capacity of cache.
     */
    public LRUCache(int aSize) {
        size = aSize;
        data = new CacheLRU(size);
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
            data.addElement(key, key);
        } catch (Exception e) {
            log.error("Cloning failed",e);
        }
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * cached object, otherwise it returns null.
     * @return cached object
     */
    public GenericObject load(GenericObject obj) {
        return (GenericObject) data.getElement(obj);
    }

    /**
     * If <code>obj</code> is deleted from persistant storage,
     * it is wise to delete it from cache too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
        data.removeElement(obj);
        if (obj instanceof Relation)
            Nursery.getInstance().removeChild((Relation) obj);
        else
            Nursery.getInstance().removeParent(obj);
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        data.clear();
        data = null;
        data = new CacheLRU(size);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        size = prefs.getInt(PREF_SIZE,DEFAULT_SIZE);
    }
}
