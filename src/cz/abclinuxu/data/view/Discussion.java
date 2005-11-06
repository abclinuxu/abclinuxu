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
package cz.abclinuxu.data.view;

import java.util.List;
import java.util.ArrayList;

/**
 * Discussion is a container for comments.
 */
public class Discussion {
    private List threads;
    private int size = 0;
    private int greatestId;
    private Integer lastRead;
    private Integer firstUnread;
    private boolean hasUnreadComments;
    // mozna List prectenych a neprectenych, pak by JavaScript mohl schovavat prectene komentare

    public Discussion() {
        threads = new ArrayList(3);
    }

    public Discussion(int size) {
        threads = new ArrayList(size);
    }

    /**
     * Appends comment to the list of threads as new toplevel thread.
     * @param comment
     */
    public void addThread(Comment comment) {
        threads.add(comment);
    }

    /**
     * @return list of toplevel threads for this discussion
     */
    public List getThreads() {
        return threads;
    }

    /**
     * @return number of all comments (not threads)
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets number of all comments
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return id of last comment
     */
    public int getGreatestId() {
        return greatestId;
    }

    /**
     * Sets id of last comment
     * @param greatestId
     */
    public void setGreatestId(int greatestId) {
        this.greatestId = greatestId;
    }

    /**
     * @return id of first (having smallest id) unread comment
     */
    public Integer getFirstUnread() {
        return firstUnread;
    }

    /**
     * Sets id of first unread comment
     * @param firstUnread
     */
    public void setFirstUnread(Integer firstUnread) {
        this.firstUnread = firstUnread;
    }

    /**
     * @return id of last comment that user has read
     */
    public Integer getLastRead() {
        return lastRead;
    }

    /**
     * Sets id of last comment that user has read
     * @param lastRead
     */
    public void setLastRead(Integer lastRead) {
        this.lastRead = lastRead;
    }

    /**
     * @return true if user has seen this discussion and there are unread comments
     */
    public boolean getHasUnreadComments() {
        return hasUnreadComments;
    }

    /**
     * Sets whether there are unread comments
     * @param hasUnreadComments
     */
    public void setHasUnreadComments(boolean hasUnreadComments) {
        this.hasUnreadComments = hasUnreadComments;
    }
}
