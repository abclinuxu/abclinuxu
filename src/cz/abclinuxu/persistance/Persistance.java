/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:20:28 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.List;
import cz.abclinuxu.data.*;

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
     * @return instance (or singleton) of object, which implements Persistance interface
     * and is described by <code>url</code>
     */
    public static Persistance getInstance(String url) {
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
     * Remove object and its references in tree from persistant storage.
     */
    public abstract void removeObject(GenericObject obj) throws PersistanceException;

    /**
     * Searches persistant storage for objects, which are similar to arguments. For each
     * GenericObject: find objects, which have same values. <code>Id</code> field is
     * ignored, same as all null fields. There is a <code>and</code> relationship between
     * non-null fields (non-zero for integer fields). There is a <code>or</code>
     * relationship between objects in <code>objects</code> list.<p>
     * For text pattern search, you can use wildchars '%' and '?', in SQL meaning.<p>
     * Example: find all open categories, which have 'HP' in their names<br>
     * <pre>Category cat = new Category(0);
     *cat.setOpen(true);
     *cat.setData("%&lt;name>%HP%&lt;/name>%");
     *List objects = new ArrayList().add(cat);
     *List result = findByExample(objects);
     *</pre>
     * @return List of GenericObjects
     */
    public abstract List findByExample(List objects) throws PersistanceException;

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
