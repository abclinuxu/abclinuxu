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

import java.util.List;
import java.util.Map;

import cz.abclinuxu.data.*;

/**
 * This interface defines responsibility of any class, that provides persistence
 * for this object model. There is a policy, that all children are not initialized.
 * ( ((Relation)getContent().get(0)).getChild().isInitialized()==false )
 */
public interface Persistence {

    /**
     * Stores new object into persistant storage and updates several fields like <code>id</code>.
     * If you are storing Relation, both <code>parent</code> and <code>child</code> must exist
     * in persistant storage!
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void create(GenericObject obj);

    /**
     * Synchronizes persistant storage with changes made in <code>obj</code>.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void update(GenericObject obj);

    /**
     * Synchronizes <code>obj</code> with external changes made in persistant storage.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void synchronize(GenericObject obj);

    /**
     * Finds object, whose <code>id</code> is same as in <code>obj</code>.
     * @return New instance of GenericObject's subclass.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
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
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public List findByExample(List objects, String relations);

    /**
     * Finds objects, that fulfill <code>command</code>. Usage of this method requires deep knowledge
     * of persistence structure (such as SQL commands, database schema), which decreases portability
     * of your code.
     * @return List of arrays of primitive objects. You know, what it is.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public List findByCommand(String command);

    /**
     * Finds children of given GenericObject. Children are not initialized.
     * If there is no child for the obj, empty list is returned.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public List findChildren(GenericObject obj);

    /**
     * Finds children for list of GenericObjects. Children are not initialized.
     * If there is no child for the obj, empty list is used.
     * @param objects list of GenericObject
     * @return Map where GenericObject is key and List with uninitialized Relations is value.
     * @throws cz.abclinuxu.exceptions.PersistenceException
     *          When something goes wrong.
     */
    public Map findChildren(List objects);

    /**
     * Finds all parents of this Relation. First element is top level relation, the second is its child and the
     * last element is this relation.
     * @return List of Relations, starting at top level.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public List findParents(Relation relation);

    /**
     * Synchronizes list of GenericObjects. The list may hold different objects.
     * @param list
     */
    public void synchronizeList(List list);

    /**
     * Finds all relations where obj is children.
     * @param child
     * @return list of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistenceException
     */
    public List findRelations(GenericObject child);

    /**
     * Finds relation described by <code>example</code>. Just set child or parent.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public Relation[] findByExample(Relation example);

    /**
     * Removes GenericObject from Persistant storage. If <code>obj</code> is Relation,
     * this method removes this relation. If relation.getChild() becomes unreferenced,
     * it is removed too. If <code>obj</code> is not Relation, this object plus all its references
     * is deleted.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void remove(GenericObject obj);

    /**
     * Increments given counter for specified object.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void incrementCounter(GenericObject obj, String type);

    /**
     * Increment counter for one or more PollChoices of the same Poll.
     * @param choices list of PollChoices. They must have valid poll and id properties.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void incrementPollChoicesCounter(List choices);

    /**
     * @return value of given counter for specified object.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public int getCounterValue(GenericObject obj, String type);

    /**
     * Fetches given counters for specified objects.
     * @param objects list of GenericObjects
     * @return map where key is GenericObject and value is Number with its counter.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public Map getCountersValue(List objects, String type);

    /**
     * Removes given counter for specified object. To be used to clean up database after unit test.
     * @throws cz.abclinuxu.exceptions.PersistenceException When something goes wrong.
     */
    public void removeCounter(GenericObject obj, String type);

    /**
     * Sets cache.
     */
    public void setCache(Cache cache);

    /**
     * Removes content of associated cache.
     */
    public void clearCache();
}