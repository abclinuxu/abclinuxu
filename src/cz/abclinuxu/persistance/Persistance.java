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
public interface Persistance {

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
     * Finds objects, that are similar to suppplied arguments.<ul>
     * <li>All objects in the list <code>objects</code>, must be of same class, which is extended of
     * GenericObject. The subclasses of this class are allowed. E.g. {Record, Record, SoftwareRecord}
     * is valid argument, {Link, Poll, Article} is wrong.
     * <li>For each object, only initialized fields are used, <code>id</code> and <code>updated</code>
     * are excluded. Because it is not possible to distinguish uninitialized boolean
     * fields from false, boolean fields are allways used. If there is more used field in one
     * object, AND relation is used for them.
     * <li>If <code>relations</code> is null, OR relation is used between all objects.
     * <li>For <code>relations</code> argument, you may use keywords AND, OR and parentheses.
     * You use indexes to <code>objects</code> as logical variables, first index is 0, maximum index is 9.
     * <li>Examples of <code>relations</code>:"0 AND 1", "0 OR 1", "0 OR (1 AND 2)", "(0 AND 1) OR (0 AND 2)"
     * </ul>
     * @return list of objects, which are of same class, as <code>objects</code>.
     */
    public abstract List findByExample(List objects, String relations) throws PersistanceException;

    /**
     * Finds objects, that are similar to suppplied argument. Same as findByExample(objects, null).
     * @see findByExample(List objects, String relations)
     */
    public abstract List findByExample(List objects) throws PersistanceException;

    /**
     * Searches persistant storage according to rules specified by <code>command</code>.
     * Command syntax is Persistance's subclass specific (usually SQL). Each record
     * will be stored as <code>returnType</code>, which may be any GenericObject's
     * subclass or List.
     * <p>
     * <b>Warning!</b> Usage of this method requires deep knowledge of persistance
     * (like database structure) and makes system less portable! You shall
     * not use it, if it is possible.
     */
    public abstract List findByCommand(String command, Class returnType) throws PersistanceException;

    /**
     * Adds <code>obj</code> under <code>parent</code> in the object tree.
     */
    public abstract void addObjectToTree(GenericObject obj, GenericObject parent) throws PersistanceException;

    /**
     * Removes <code>obj</code> from <code>parent</code> in the object tree.
     * If <code>obj</code> was referenced only by <code>parent</code>, <code>obj</code>
     * is removed from persistant storage.
     */
    public abstract void removeObjectFromTree(GenericObject obj, GenericObject parent) throws PersistanceException;

    /**
     * Increments counter for <code>obj</code>. This method can be used for ArticleRecord or Link.
     */
    public abstract void incrementCounter(GenericObject obj) throws PersistanceException;
}
