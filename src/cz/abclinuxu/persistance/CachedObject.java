/*
 * User: literakl
 * Date: Dec 28, 2001
 * Time: 12:18:57 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.data.GenericObject;

/**
 * This class encapsulates one GenericObject in cache with time,
 * when it shall be synchronized with external changes in database.
 */
public class CachedObject {

    public GenericObject object;
    /** last updated time */
    public long lastSync;
    /** last accessed time */
    public long lastAccessed;


    public CachedObject() {
    }

    public CachedObject(GenericObject object) {
        this.object = object;
        lastAccessed = lastSync = System.currentTimeMillis();
    }

    public boolean equals(Object o) {
        CachedObject o2 = (CachedObject) o;
        if ( !object.getClass().isInstance(o) ) return false;
        return o2.object.getId()==object.getId();
    }

    public int hashCode() {
        String tmp = getClass().getName()+object.getId();
        return tmp.hashCode();
    }

    /**
     * Analogue of unix's command touch. It doesn't affect content of object, just changes
     * last modification timestamp.
     */
    public void touch() {
        lastAccessed = System.currentTimeMillis();
    }
}
