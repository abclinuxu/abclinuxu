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

import java.util.Iterator;
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
            ConfigurationManager.getConfigurator().configureMe(this);
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
            if ( obj instanceof Relation ) {
                GenericObject parent = ((Relation) obj).getParent();

                // if parent has been changed on stored relation (aka child was moved),
                // we must remove relation first, otherwise parent would be inconsistent.
                Relation original = (Relation) data.getElementNoLRU(obj);
                if ( original!=null && (! original.getParent().equals(parent)) )
                    remove(original);

                Relation clone = ((Relation) obj).cloneRelation();

                // if parent has been already cached and is allowed to laod children, add relation to it
                GenericObject cached = (GenericObject) data.getElementNoLRU(parent);
                if ( cached!=null && !PersistanceFactory.isLoadingChildrenForbidden(cached)) {
                    synchronized (cached) {
                        cached.addContent(clone);
                    }
                }

                data.addElement(clone,clone);
            } else {
                GenericObject key = (GenericObject) obj.getClass().newInstance();
                key.synchronizeWith(obj);
                key.clearContent();

                for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
                    Relation lightClone = ((Relation) iter.next()).cloneRelation();
                    data.addElement(lightClone,lightClone);
                    key.addContent(lightClone);
                }

                data.addElement(key,key);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * clone of cached object, otherwise it returns null.
     * @return cached object
     */
    public GenericObject load(GenericObject obj) {
        GenericObject found = (GenericObject) data.getElement(obj);
        if ( found==null )
            return null;

        // we cannot return immutable object, so we return clone at least.
        // for example if found is Relation, caller could synchronize its child or parent,
        // which could result in data inconsistency and unneccessary memory consumption.
        if ( found instanceof Relation ) {
            return ((Relation)found).cloneRelation();
        }  else {
            try {
                GenericObject clone = (GenericObject) found.getClass().newInstance();
                clone.synchronizeWith(found);
                clone.clearContent();
                synchronized (found) {
                    for ( Iterator iter = found.getContent().iterator(); iter.hasNext(); ) {
                        Relation relation = ((Relation) iter.next()).cloneRelation();
                        clone.addContent(relation);
                    }
                }
                return clone;
            } catch (Exception e) {
                log.error("Nemohu naklonovat nalezený object "+found, e);
                return null;
            }
        }
    }

    /**
     * If <code>obj</code> is deleted from persistant storage,
     * it is wise to delete it from cache too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
        if ( data.removeElement(obj)==null )
            return;

        if ( obj instanceof Relation ) {
            GenericObject parent = ((Relation) obj).getParent();
            GenericObject cached = (GenericObject) data.getElementNoLRU(parent);
            if ( cached!=null ) {
                synchronized (cached) {
                    cached.getContent().remove(obj);
                }
            }
        }
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
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
