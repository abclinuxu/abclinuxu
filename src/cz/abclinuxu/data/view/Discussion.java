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

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Data;
import cz.abclinuxu.utils.Misc;

import java.util.*;

/**
 * Discussion is a container for comments.
 */
public class Discussion {
    private List<Comment> threads;
    private Map<Integer, Data> attachments = Collections.emptyMap();
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
    public List<Comment> getThreads() {
        if ((threads == null))
            return Collections.emptyList();
        else
            return threads;
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
        if (lastRead == null || greatestId <= lastRead)
            return;

        int lastId = lastRead;
        int unreadSize = size - lastId;
        if (unreadSize <= 0)
            unreadSize = 10;
        unreadComments = new ArrayList(unreadSize);
        Comment current = null;
        LinkedList stack = new LinkedList(threads);
        while (stack.size() > 0) {
            current = (Comment) stack.removeFirst();
            
            if (isBlacklisted(current))
                continue;
            
            if (current.getChildren() != null)
                stack.addAll(0, current.getChildren());
            if (current.getId() > lastId)
                unreadComments.add(current.getId());
        }
    }

    /**
     * @return true if user has seen this discussion and there are unread comments
     */
    public boolean getHasUnreadComments() {
        return lastRead != null && greatestId > lastRead && !Misc.empty(unreadComments);
    }

    /**
     * @return true if user has seen this discussion and this comment is new for him
     */
    public boolean isUnread(Comment comment) {
        return lastRead != null && comment.getId() > lastRead;
    }

    /**
     * @return id of first (having smallest id) unread comment
     */
    public Integer getFirstUnread() {
        if (lastRead == null || unreadComments.size() == 0)
            return null;
        return (Integer) unreadComments.get(0);
    }

    /**
     * @return id of next unread comment or null
     */
    public Integer getNextUnread(Comment comment) {
        if (unreadComments == null || lastRead == null)
            return null;
        int position = unreadComments.indexOf(comment.getId());
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
        if (blacklist == null)
            return false;
        Integer author = comment.getAuthor();
        if (author == null)
            return blacklist.contains(comment.getAnonymName());
        else
            return blacklist.contains(author);
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

    /**
     * Stores Data objects from list of relations to map.
     * @param list list of fully initialized (including children) relations
     */
    public void setAttachments(List list) {
        if (list == null || list.isEmpty())
            return;

        attachments = new HashMap<Integer, Data>(list.size(), 1.0f);
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Relation relation = (Relation) iter.next();
            attachments.put(relation.getId(), (Data) relation.getChild());
        }
    }

    public Map<Integer, Data> getAttachments() {
        return attachments;
    }

    /**
     * Finds attachment with given relation id.
     * @param relationId id of relation holding the Data object
     * @return Data object or null if not found
     */
    public Data getAttachment(String relationId) {
        int id = Misc.parseInt(relationId, -1);
        return attachments.get(id);
    }
}
