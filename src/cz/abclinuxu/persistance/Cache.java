/*
 * User: literakl
 * Date: Dec 28, 2001
 * Time: 12:25:03 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import cz.abclinuxu.data.*;

/**
 * Cache of GenericObjects. Only selected classes are cached.
 */
public class Cache {
    Persistance persistance;
    Map categories;

    public static final int SYNC_INTERVAL = 5*60*1000; // 5 minutes

    public Cache(Persistance persistance) {
        this.persistance = persistance;
        categories = new HashMap();
    }

    /**
     * This method stores object into cache, so it can be retrieved
     * later without queueing database. If <code>obj</code> with same
     * PK is already stored in the cache, it is replaced by new version.
     */
    public void store(GenericObject obj) {
        if ( obj instanceof Category ) {
            categories.put(obj,new CachedObject(obj,System.currentTimeMillis()+SYNC_INTERVAL));
            return;
        }
        if ( obj instanceof Relation ) {
            GenericObject parent = ((Relation) obj).getParent();
            CachedObject cached = loadCachedObject(parent);
            if ( cached!=null ) {
                cached.object.addContent((Relation)obj);
            }
        }
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * cached object, otherwise it returns null.
     * @return cached object or null, if it is not found.
     */
    public GenericObject load(GenericObject obj) {
        if ( obj instanceof Category ) {
            CachedObject cached = (CachedObject) categories.get(obj);
            if ( cached!=null ) {
                return cached.object;
            }
        }
        return null;
    }

    /**
     * If <code>obj</code> is not available anymore, it is good practice to
     * delete it from Persistant storage too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
        if ( obj instanceof Category ) {
            categories.remove(obj);
            return;
        }
        if ( obj instanceof Relation ) {
            GenericObject parent = ((Relation) obj).getParent();
            CachedObject cached = loadCachedObject(parent);
            if ( cached!=null ) {
                List content = cached.object.getContent();
                content.remove(obj);
            }
        }
    }

    /**
     * This method searches cache for specified object.
     * @return CachedObject or null, if it is not found.
     */
    public CachedObject loadCachedObject(GenericObject obj) {
        if ( obj instanceof Category ) {
            return (CachedObject) categories.get(obj);
        }
        return null;
    }
}
