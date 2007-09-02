/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.User;

import java.util.List;
import java.util.ArrayList;

/**
 * View object encapsulating /data/versioning/revisions element.
 * @author literakl
 * @since 2.9.2007
 */
public class RevisionInfo {
    private int lastRevision;
    private User creator, lastCommiter;
    private List<User> committers;

    public RevisionInfo(int lastRevision) {
        this.lastRevision = lastRevision;
    }

    /**
     * @return user that is author of the first revision
     */
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    /**
     * @return user that is author of the last revision or null, if there is first revision only
     */
    public User getLastCommiter() {
        return lastCommiter;
    }

    public void setLastCommiter(User lastCommiter) {
        this.lastCommiter = lastCommiter;
    }

    /**
     * @return latest revision value
     */
    public int getLastRevision() {
        return lastRevision;
    }

    /**
     * List of last committers. The author of the first and the last revisions are ommitted.
     * The list is limited in its size.
     * @return list of last committers
     */
    public List<User> getCommitters() {
        return committers;
    }

    public void setCommitters(List<User> committers) {
        this.committers = committers;
    }

    public void addCommitter(User user) {
        if (committers == null)
            committers = new ArrayList<User>(3);
        committers.add(user);
    }
}
