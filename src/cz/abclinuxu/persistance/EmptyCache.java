/*
 * User: literakl
 * Date: 16.5.2002
 * Time: 15:27:32
 * (c) 2001-2002 Tinnio
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.data.GenericObject;

/**
 * Version of cache, which does nothing.
 */
public class EmptyCache implements Cache {
    public EmptyCache() {
    }

    public void store(GenericObject obj) {
    }

    public GenericObject load(GenericObject obj) {
        return null;
    }

    public void remove(GenericObject obj) {
    }
}
