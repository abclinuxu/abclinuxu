/*
 * User: literakl
 * Date: Dec 5, 2001
 * Time: 2:47:14 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.List;
import cz.abclinuxu.data.*;

/**
 * This interface defines responsibility of any class, that provides persistance
 * for this object model. There is a policy, that all children are not initialized.
 * ( ((Relation)getContent().get(0)).getChild().isInitialized()==false )
 */
public interface Persistance {

    /**
     * Stores new object into persistant storage and updates several fields like <code>id</code>.
     * If you are storing Relation, both <code>parent</code> and <code>child</code> must exist
     * in persistant storage!
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void create(GenericObject obj);

    /**
     * Synchronizes persistant storage with changes made in <code>obj</code>.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void update(GenericObject obj);

    /**
     * Synchronizes <code>obj</code> with external changes made in persistant storage.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void synchronize(GenericObject obj);

    /**
     * Finds object, whose <code>id</code> is same as in <code>obj</code>.
     * @return New instance of GenericObject's subclass.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public GenericObject findById(GenericObject obj);

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
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public List findByExample(List objects, String relations);

    /**
     * Finds objects, that fulfill <code>command</code>. Usage of this method requires deep knowledge
     * of persistance structure (such as SQL commands, database schema), which decreases portability
     * of your code.
     * @return List of arrays of primitive objects. You know, what it is.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public List findByCommand(String command);

    /**
     * Finds children of given GenericObject. Children are not initialized.
     * If there is no child for the obj, empty list is returned.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public List findChildren(GenericObject obj);

    /**
     * Finds all parents of this Relation. First element is top level relation, the second is its child and the
     * last element is this relation.
     * @return List of Relations, starting at top level.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public List findParents(Relation relation);

    /**
     * Finds all relations where obj is children.
     * @param child
     * @return list of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException
     */
    public List findRelations(GenericObject child);

    /**
     * Finds relation described by <code>example</code>. Just set child or parent.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public Relation[] findByExample(Relation example);

    /**
     * Removes GenericObject from Persistant storage. If <code>obj</code> is Relation,
     * this method removes this relation. If relation.getChild() becomes unreferenced,
     * it is removed too. If <code>obj</code> is not Relation, this object plus all its references
     * is deleted.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void remove(GenericObject obj);

    /**
     * Increments counter for specified object.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void incrementCounter(GenericObject obj);

    /**
     * Increments counter for specified choice od the poll.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void incrementCounter(PollChoice choice);

    /**
     * @return Actual value of counter for specified object.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public int getCounterValue(GenericObject obj);

    /**
     * Removes counter for specified object. To be used to clean up database after unit test.
     * @throws cz.abclinuxu.exceptions.PersistanceException When something goes wrong.
     */
    public void removeCounter(GenericObject obj);

    /**
     * Sets cache.
     */
    public void setCache(Cache cache);

    /**
     * Removes content of associated cache.
     */
    public void clearCache();
}
