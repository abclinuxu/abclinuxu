/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:20:28 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Link;

/**
 * This interface defines methods, how to store data objects
 * into persitant storage.
 */
public abstract class Persistance {

    /**
     * @return instance (or singleton) of object, which implements Persistance interface
     */
    public static Persistance getInstance() {
        return null;
    }

    /**
     * Downloads object described by <code>obj</code> (id and class name) from persistant storage.
     * It also fills its <code>content</code> field with uninitialized objects (only <code>id</code>).
     * They may be downloaded on request with this method again.
     */
    public abstract GenericObject loadObject(GenericObject obj) throws PersistanceException;

    /**
     * Makes object peristant. It may modify <code>id</code> of argument.
     */
    public abstract void storeObject(GenericObject obj) throws PersistanceException;

    /**
     * Synchronizes changes in the object with the persistant storage.
     */
    public abstract void updateObject(GenericObject obj) throws PersistanceException;

    /**
     * Remove object and references in tree from persistant storage.
     */
    public abstract void removeObject(GenericObject obj) throws PersistanceException;

    /**
     * Adds <code>obj</code> under <code>parent</code> in the object tree.
     */
    public abstract void addObjectToTree(GenericObject obj, GenericObject parent) throws PersistanceException;

    /**
     * Removes <code>obj</code> from <code>parent</code> in the object tree.
     */
    public abstract void removeObjectFromTree(GenericObject obj, GenericObject parent) throws PersistanceException;

    /**
     * Increments counter for <code>obj</code>. This method can be used for ArticleRecord or Link.
     */
    public abstract void incrementCounter(GenericObject obj) throws PersistanceException;
}
