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

import java.util.*;

/**
 * Discussion is a container for comments.
 */
public class Discussion {
    private List threads;
    private int size = 0;
    private int greatestId;
    private int id;
    private int relationId;
    private Integer lastRead;
    private List unreadComments;
    private Set blacklist;
    private boolean frozen;
    private boolean monitored;
    private int monitorSize;

    public Discussion() {
    }

    public void init(DiscussionRecord record) {
        size = record.getTotalComments();
        threads = record.getThreads();
        greatestId = record.getMaxCommentId();
    }

    /**
     * @return list of toplevel threads for this discussion
     */
    public List getThreads() {
        return (threads == null) ? Collections.EMPTY_LIST : threads;
    }

    /**
     * @return id of discussion item for this discussion
     */
    public int getId() {
        return id;
    }

    /**
     * Sets id of discussion item for this discussion
     * @param id id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return relation od of this discussion
     */
    public int getRelationId() {
        return relationId;
    }

    /**
     * Sets relation id of this discussion
     * @param relationId relation id
     */
    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    /**
     * @return tru if discussion is frozen and new comments are not allowed
     */
    public boolean isFrozen() {
        return frozen;
    }

    /**
     * Mark discussion as frozen
     * @param frozen true if new comments are not allowed
     */
    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    /**
     * @return number of all comments (not threads)
     */
    public int getSize() {
        return size;
    }

    /**
     * @return id of last comment
     */
    public int getGreatestId() {
        return greatestId;
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
    public void setUnreadComments(Integer lastRead) {
        this.lastRead = lastRead;
        if (! getHasUnreadComments())
            return;

        int lastId = lastRead.intValue();
        int unreadSize = size - lastId;
        if (unreadSize <= 0)
            unreadSize = 10;
        unreadComments = new ArrayList(unreadSize);
        Comment current = null;
        LinkedList stack = new LinkedList(threads);
        while (stack.size() > 0) {
            current = (Comment) stack.removeFirst();
            if (current.getChildren() != null)
                stack.addAll(0, current.getChildren());
            if (current.getId() > lastId)
                unreadComments.add(new Integer(current.getId()));
        }
    }

    /**
     * @return true if user has seen this discussion and there are unread comments
     */
    public boolean getHasUnreadComments() {
        return lastRead != null && greatestId > lastRead.intValue();
    }

    /**
     * @return true if user has seen this discussion and this comment is new for him
     */
    public boolean isUnread(Comment comment) {
        return lastRead != null && comment.getId() > lastRead.intValue();
    }

    /**
     * @return id of first (having smallest id) unread comment
     */
    public Integer getFirstUnread() {
        if (lastRead == null)
            return null;
        return (Integer) unreadComments.get(0);
    }

    /**
     * @return id of next unread comment or null
     */
    public Integer getNextUnread(Comment comment) {
        if (unreadComments == null || lastRead == null)
            return null;
        int position = unreadComments.indexOf(new Integer(comment.getId()));
        if (position < 0 || (position + 1 == unreadComments.size()))
            return null;
        return (Integer) unreadComments.get(position+1);
    }

    /**
     * @return list of ids (Integer) of unread comments
     */
    public List getUnreadComments() {
        return unreadComments;
    }

    /**
     * @return list of users (Integer) that are in blacklist for current user
     */
    public List getBlacklist() {
        return new ArrayList(blacklist);
    }

    /**
     * Sets list of blacklisted users (id - Integer) for current user.
     * @param blacklist (Users) not null
     */
    public void setBlacklist(Set blacklist) {
        this.blacklist = blacklist;
    }

    /**
     * Tests if author of this comment is in current user's blacklist.
     * @param comment
     * @return true if user does not wish to see it
     */
    public boolean isBlacklisted(Comment comment) {
        if (comment.getAuthor() == null)
            return false;
        if (blacklist == null)
            return false;
        return blacklist.contains(comment.getAuthor());
    }

    /**
     * @return true, if current user has set up watch for this discussion
     */
    public boolean isMonitored() {
        return monitored;
    }

    /**
     * Marks discussion as monitored by current user.
     * @param monitored true if user is interested in notifications for this discussion
     */
    public void setMonitored(boolean monitored) {
        this.monitored = monitored;
    }

    /**
     * @return number of users monitoring this discussion
     */
    public int getMonitorSize() {
        return monitorSize;
    }

    /**
     * Sets number of users monitoring this discussion
     * @param monitorSize count of monitors
     */
    public void setMonitorSize(int monitorSize) {
        this.monitorSize = monitorSize;
    }
}
