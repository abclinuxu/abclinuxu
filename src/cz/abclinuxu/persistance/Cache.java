/*
 * User: literakl
 * Date: 17.5.2002
 * Time: 14:03:19
 * (c) 2001-2002 Tinnio
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.data.GenericObject;

/**
 * Interface for various cache implementations.
 */
public interface Cache {
    /**
     * This method stores copy of object into cache, so it can be retrieved
     * later without queueing database. If <code>obj</code> with same
     * PK is already stored in the cache, it is replaced by new version.
     */
    void store(GenericObject obj);

    /**
     * This method searches cache for specified object. If it is found, returns
     * clone of cached object, otherwise it returns null.
     * @return cached object
     */
    GenericObject load(GenericObject obj);

    /**
     * If <code>obj</code> is deleted from persistant storage,
     * it is wise to delete it from cache too. Otherwise inconsistency occurs.
     */
    void remove(GenericObject obj);

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    void clear();
}
