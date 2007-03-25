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

import java.util.List;

/**
 * Interface to access versioning repository. It supports to
 * store (latest) version of document, load selected
 * document in given version and load information about all
 * versions of selected document. The document can hold any data,
 * for example serialized xml.
 * User: literakl
 * Date: 27.3.2005
 */
public interface Versioning {

    /**
     * Stores latest version of document into versioning repository.
     * @param document document to be stored
     * @param relation relation id for this document
     * @param user identifier of the user who commited this version
     * @return information about this version
     */
    public VersionInfo commit(String document, int relation, int user);

    /**
     * Loads document identified by path in selected version.
     * @param relation relation id for this document
     * @param version version to be fetched
     * @return document with versioning metadata
     * @throws VersionNotFoundException Thrown when either document or specified version doesn't exist.
     */
    public VersionedDocument load(int relation, int version) throws VersionNotFoundException;

    /**
     * Loads versioning history for selected document in descending order.
     * @param relation relation id for this document
     * @return list of VersionInfo objects. When the list is empty, then there is no
     * version of specified document.
     */
    public List getHistory(int relation);

    /**
     * Removes all information for given document from versioning repository.
     * @param relation relation id for this document
     * @return true if there were some revisions for specified document
     */
    public boolean purge(int relation);
}
