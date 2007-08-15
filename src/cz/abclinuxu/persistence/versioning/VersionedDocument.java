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

import java.util.Date;

/**
 * This class holds one version of the document with version metadata.
 * To make it as generic as possible, both version and
 * user are stored as String.
 * Date: 28.3.2005
 */
public class VersionedDocument {
    private int version;
    private int user;
    private Date commited;
    private String document;
    private String diff;
    private String description;

    /**
     * @return document
     */
    public String getDocument() {
        return document;
    }

    /**
     * Sets document.
     * @param document
     */
    public void setDocument(String document) {
        this.document = document;
    }

    /**
     * @return diff against previous version
     */
    public String getDiff() {
        return diff;
    }

    /**
     * Sets diff against previous version
     * @param diff
     */
    public void setDiff(String diff) {
        this.diff = diff;
    }

    /**
     * @return version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets version.
     * @param version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return identificator of the user, who commited this version
     */
    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    /**
     * @return time, when this version has been commited
     */
    public Date getCommited() {
        return commited;
    }

    public void setCommited(Date commited) {
        this.commited = commited;
    }

    /**
     * @return description (comment) of the changes
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionedDocument)) return false;

        final VersionedDocument versionedDocument = (VersionedDocument) o;

        if (user != versionedDocument.user) return false;
        if (version != versionedDocument.version) return false;
        if (diff != null ? ! diff.equals(versionedDocument.diff) : versionedDocument.diff != null) return false;
        if (document != null ? ! document.equals(versionedDocument.document) : versionedDocument.document != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = version;
        result = 29 * result + user;
        result = 29 * result + (document != null ? document.hashCode() : 0);
        result = 29 * result + (diff != null ? diff.hashCode() : 0);
        return result;
    }
}
