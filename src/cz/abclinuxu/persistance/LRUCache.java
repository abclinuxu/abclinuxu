/*
 * User: Leos Literak
 * Date: May 29, 2003
 * Time: 8:55:15 PM
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.persistance.lru.CacheLRU;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Relation;

import java.util.Iterator;

/**
 * This cache uses LRU policy. It is backed up
 * by CacheLRU from Jakarta's ORO project.
 */
public class LRUCache implements Cache {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LRUCache.class);

    public static final int DEFAULT_SIZE = 1000;

    CacheLRU data;

    /**
     * Default constructor with default maximum capacity of cache.
     */
    public LRUCache() {
        this(DEFAULT_SIZE);
    }

    /**
     * Default constructor with given maximum capacity of cache.
     */
    public LRUCache(int size) {
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
                Relation lightClone = ((Relation) obj).cloneRelation();

                // if parent has been changed on stored relation (aka child was moved),
                // we must remove relation first, otherwise parent would be inconsistent.
                Relation original = (Relation) data.getElementNoLRU(obj);
                if ( original!=null && (! original.getParent().equals(parent)) )
                    remove(original);

                data.addElement(lightClone,lightClone);
                // if parent has been already cached, add relation to it
                GenericObject cached = (GenericObject) data.getElementNoLRU(parent);
                if ( cached!=null ) {
                    cached.addContent(lightClone);
                }
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
        return found;
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
                cached.getContent().remove(obj);
            }
        }
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        int capacity = data.capacity();
        data = null;
        data = new CacheLRU(capacity);
    }
}
