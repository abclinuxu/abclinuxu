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
package cz.abclinuxu.persistence.versioning;

import cz.abclinuxu.data.GenericDataObject;

import java.util.List;

/**
 * Interface to access versioning repository. It supports
 * storing and loading GenericDataObject revisions.
 * User: literakl
 * Date: 27.3.2005
 */
public interface Versioning {

    /**
     * Updates versioning element inside object's XML. This call is mandatory
     * before versioned object can be persisted and committed. It increments
     * latest version and list of last committers. It is neccessary to store
     * these changes to persistence, once this method is finished.
     * Typical use case is:
     * <ul>
     * <li>versioning.prepareObjectBeforeCommit(obj, user.getId());</li>
     * <li>persistence.create(obj); or persistence.update(obj);</li>
     * <li>versioning.commit(obj, user.getId(), commitMessage);</li>
     * </ul>
     * @param obj object that shall be updated
     * @param user identifier of the user who commited this version
     */
    public void prepareObjectBeforeCommit(GenericDataObject obj, int user);

    /**
     * Stores latest version of document into versioning repository. Object's XML document
     * must contain valid versioning/info element. You must call prepareObjectBeforeCommit()
     * method prior to this call.
     * @param obj object that shall be stored
     * @param user identifier of the user who commited this version
     * @param descr description of commited changes
     */
    public void commit(GenericDataObject obj, int user, String descr) throws VersioningException;

    /**
     * Loads given document in selected version from versioning.
     * @param obj object to be loaded and updated to contain same data like in specified revision
     * @param version version to be fetched
     * @throws VersionNotFoundException Thrown when either document or specified version doesn't exist.
     */
    public void load(GenericDataObject obj, int version) throws VersionNotFoundException;

    /**
     * Loads versioning history for selected document in descending order.
     * @param obj object, its type and id is used for identification
     * @return list of VersionInfo objects. When the list is empty, then there is no
     * version of specified document.
     */
    public List<VersionInfo> getHistory(GenericDataObject obj);

    /**
     * Removes all information for given document from versioning repository.
     * @param obj object, its type and id is used for identification
     * @return true if there were some revisions for specified document
     */
    public boolean purge(GenericDataObject obj);
}
