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
    /** time, when Persistance.synchronizedCached(object) shall be called */
    public long nextSync;


    public CachedObject() {
    }

    public CachedObject(GenericObject object, long nextSync) {
        this.object = object;
        this.nextSync = nextSync;
    }

    public boolean equals(Object o) {
        CachedObject o2 = (CachedObject) o;
        return o2.object.getId()==object.getId();
    }

    public int hashCode() {
        return object.getId();
    }
}
