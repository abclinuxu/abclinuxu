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
 * <p>
 * Cache sucks really badly. The main problem is, that it returns
 * mutable objects, which are initialized in code. Then the data
 * stored in cache are not synchronized.
 * <p>
 * Imagine: clone of relation is stored, its parent and child are
 * both stored as not initialized. Later it is retrieved and someone
 * calls sync on parent. Now cache contains relation with initialized
 * parent. Someone else add new content to parent. The parent stored
 * in cache is updated, but parent stored in relation is not. So when
 * the relation is used next time, parent will be not synchronized,
 * because it is already marked as synchronized. But this copy doesn't
 * contain new content!
 * <p>
 * The best solution is to save copy of stored objects and return immutable
 * object. The second best solution is to clone object and return the clone
 * instead of real object.
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
