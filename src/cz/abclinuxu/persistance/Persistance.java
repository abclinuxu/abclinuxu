/*
 * User: literakl
 * Date: Dec 5, 2001
 * Time: 2:47:14 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.List;
import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.PersistanceException;

/**
 * This interface defines responsibility of any class, that provides persistance
 * for this object model.
 */
public interface Persistance {

    /**
     * Stores new object into persistant storage and updates several fields like <code>id</code>.
     * If you are storing Relation, both <code>parent</code> and <code>child</code> must exist
     * in persistant storage!
     */
    public void create(GenericObject obj) throws PersistanceException;

    /**
     * Synchronizes persistant storage with changes made in <code>obj</code>.
     */
    public void update(GenericObject obj) throws PersistanceException;

    /**
     * Synchronizes <code>obj</code> with external changes made in persistant storage.
     */
    public void synchronize(GenericObject obj) throws PersistanceException;

    /**
     * Finds object, whose <code>id</code> is same as in <code>obj</code>.
     * @return New instance of GenericObject's subclass.
     */
    public GenericObject findById(GenericObject obj) throws PersistanceException;

    /**
     * Finds objects, that are similar to suppplied arguments.
     * <br><u>Rules:</u><ul>
     * <li>All objects in the list <code>objects</code>, must be of same class, which is extended of
     * GenericObject. The subclasses of this class are allowed. E.g. {Record, Record, SoftwareRecord}
     * is valid argument, {Link, Poll, Article} is wrong.
     * <li>For each object, only initialized fields are used, <code>id</code> and <code>updated</code>
     * fields are excluded, boolean fields are always used. If there are more such fields in one
     * object, there is an AND relation between them.
     * <li>If <code>relations</code> is null, OR relation is used between all objects.
     * <li>For <code>relations</code> argument, you may use keywords AND, OR and parentheses
     * and indexes to <code>objects</code> as logical variables. First index is 0.
     * <li>Examples of <code>relations</code>:"0 AND 1", "0 OR 1", "0 OR (1 AND 2)", "(0 AND 1) OR (0 AND 2)"
     * </ul>
     * @return List of objects, which are of same class, as <code>objects</code>.
     */
    public List findByExample(List objects, String relations) throws PersistanceException;

    /**
     * Finds objects, that fulfill <code>command</code>. Usage of this emthod requires deep knowledge
     * of persistance structure (such as SQL commands, database schema), which decreases portability
     * of your code.
     * @return List of array of objects. You know, what it is.
     */
    public List findByCommand(String command) throws PersistanceException;

    /**
     * Finds all parents of this Relation. First element is top level relation, the second is its child and the
     * last element is direct parent of this relation (last.getId()==relation.getUpper()).
     * @return List of Relations, starting at top level.
     */
    public List findParents(Relation relation) throws PersistanceException;

    /**
     * Removes GenericObject from Persistant storage. If <code>obj</code> is Relation,
     * this method removes this relation. If relation.getChild() becomes unreferenced,
     * it is removed too. If <code>obj</code> is not Relation, this object plus all its references
     * is deleted.
     */
    public void remove(GenericObject obj) throws PersistanceException;

    /**
     * Increments counter for specified object.
     */
    public void incrementCounter(GenericObject obj) throws PersistanceException;

    /**
     * @return Actual value of counter for specified object.
     */
    public int getCounterValue(GenericObject obj) throws PersistanceException;

    /**
     * Removes counter for specified object. To be used to clean up database after unit test.
     */
    public void removeCounter(GenericObject obj) throws PersistanceException;
}
