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
 * This class holds information about certain version.
 * To make it as generic as possible, both version and
 * user are stored as String.
 * Date: 27.3.2005
 */
public class VersionInfo {
    private String version;
    private String user;
    private Date commited;

    /**
     * @return version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets version. Maximum length of version is 25.
     * @param version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return identificator of the user, who commited this version
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionInfo)) return false;

        final VersionInfo versionInfo = (VersionInfo) o;

//        if (!commited.equals(versionInfo.commited)) return false;
        if (!user.equals(versionInfo.user)) return false;
        if (!version.equals(versionInfo.version)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = version.hashCode();
        result = 29 * result + user.hashCode();
        result = 29 * result + commited.hashCode();
        return result;
    }
}
