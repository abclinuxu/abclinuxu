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
package cz.abclinuxu.persistence;

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
