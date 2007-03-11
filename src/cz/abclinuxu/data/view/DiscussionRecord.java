/*
 *  Copyright (C) 2006 Leos Literak
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
 * @author literakl
 * @since 19.2.2006
 */
public class DiscussionRecord implements Cloneable {
    private List threads;
    private int maxCommentId;
    private int totalComments;
    private List deletedComments;

    /**
     * Appends comment to the list of threads as new toplevel thread.
     * @param comment
     */
    public void addThread(Comment comment) {
        if (threads == null)
            threads = new ArrayList(3);
        threads.add(comment);
    }

    /**
     * Removes given top-level thread.
     * @param comment comment to be removed
     * @param updateDeteledComments if set, id of removed comments will be added to list of deleted comments
     * @return true if comment was removed
     */
    public boolean removeThread(Comment comment, boolean updateDeteledComments) {
        if (threads == null)
            return false;
        if (! threads.remove(comment))
            return false;
        if (! updateDeteledComments)
            return true;

        LinkedList stack = new LinkedList();
        stack.add(comment);
        while (stack.size() > 0) {
            comment = (RowComment) stack.removeFirst();
            addDeletedComment(((RowComment)comment).getRowId());
            stack.addAll(comment.getChildren());
        }
        return true;
    }

    /**
     * Removes all child comments.
     * @param updateDeteledComments if set, id of removed comments will be added to list of deleted comments
     */
    public void removeAllThreads(boolean updateDeteledComments) {
        if (threads == null)
            return;
        if (! updateDeteledComments) {
            threads.clear();
            return;
        }

        LinkedList stack = new LinkedList();
        stack.addAll(getThreads());
        while (stack.size() > 0) {
            RowComment comment = (RowComment) stack.removeFirst();
            addDeletedComment(comment.getRowId());
            stack.addAll(comment.getChildren());
        }
        threads.clear();
    }

    /**
     * @return list of toplevel threads for this discussion
     */
    public List getThreads() {
        return (threads == null) ? Collections.EMPTY_LIST : threads;
    }

    /**
     * Helper method that re-sorts direct child comments by their ids.
     */
    public void sortThreads() {
        Collections.sort(threads);
    }

    /**
     * Walks through the tree and returns the comment with given id
     * or null, if it was not found.
     * @param id searched comment's id
     * @return found comment or null
     */
    public Comment getComment(int id) {
        LinkedList stack = new LinkedList(threads);
        Comment comment;
        while (stack.size() > 0) {
            comment = (Comment) stack.removeFirst();
            if (comment.getId() == id)
                return comment;
            stack.addAll(comment.getChildren());
        }
        return null;
    }

    /**
     * Walks through the tree and returns the comment with largest id
     * or null, if there is no comment at all.
     * @return found comment or null
     */
    public Comment getLastComment() {
        LinkedList stack = new LinkedList(threads);
        Comment comment, lastComment = null;
        int largestId = 0;
        while (stack.size() > 0) {
            comment = (Comment) stack.removeFirst();
            if (comment.getId() > largestId) {
                largestId = comment.getId();
                lastComment = comment;
            }
            stack.addAll(comment.getChildren());
        }
        return lastComment;
    }

    /**
     * Attempts to find a comment with the same content in top level comments
     * @param c other comment
     * @return existing comment, if found
     */
    public Comment findComment(Comment c) {
        if (threads == null)
            return null;
        for (Iterator iter = threads.iterator(); iter.hasNext();) {
            Comment comment = (Comment) iter.next();
            if (comment.contentEquals(c))
                return comment;
        }
        return null;
    }

    /**
     * @return maximum id used in comments or zero, if there is no comment at all
     */
    public int getMaxCommentId() {
        return maxCommentId;
    }

    /**
     * Sets greatest id of all comments
     * @param maxCommentId value
     */
    public void setMaxCommentId(int maxCommentId) {
        this.maxCommentId = maxCommentId;
    }

    /**
     * @return total number of all comments
     */
    public int getTotalComments() {
        return totalComments;
    }

    /**
     * Sets total number of all comments
     * @param totalComments size
     */
    public void setTotalComments(int totalComments) {
        this.totalComments = totalComments;
    }

    /**
     * Ensures correct value of totalComments property.
     */
    public void calculateCommentStatistics() {
        int i = 0, max = 0;
        LinkedList stack = new LinkedList(threads);
        Comment comment;
        while (stack.size() > 0) {
            comment = (Comment) stack.removeFirst();
            stack.addAll(comment.getChildren());
            if (max < comment.getId())
                max = comment.getId();
            i++;
        }
        maxCommentId = max;
        totalComments = i;
    }

    /**
     * Adds id of deleted comment to the list of deleted comments
     * @param id row id of comment
     */
    public void addDeletedComment(int id) {
        if (deletedComments==null)
            deletedComments = new ArrayList();
        deletedComments.add(new Integer(id));
    }

    /**
     * @return list of row ids of comments that were deleted (Integer)
     */
    public List getDeletedComments() {
        return (deletedComments == null) ? Collections.EMPTY_LIST : deletedComments;
    }

    public Object clone() {
        try {
            DiscussionRecord clone = (DiscussionRecord) super.clone();
            if (clone.threads != null) {
                clone.threads = new ArrayList(threads.size());
                for (Iterator iter = threads.iterator(); iter.hasNext();) {
                    Comment comment = (Comment) iter.next();
                    clone.threads.add(comment.clone());
                }
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
