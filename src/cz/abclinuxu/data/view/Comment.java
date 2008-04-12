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

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import cz.abclinuxu.data.XMLHandler;
import cz.abclinuxu.utils.Misc;

/**
 * Comment is a holder of one reaction in discussion.
 */
public abstract class Comment implements Cloneable, Comparable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Comment.class);

    /**
     * XML with data of this object
     */
    protected XMLHandler documentHandler;
    List<Comment> children;
    int id;
    Integer parent;

    protected Comment() {
    }

    /**
     * @return XML data of this object
     */
    public Document getData() {
        return (documentHandler != null) ? documentHandler.getData() : null;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        return (documentHandler != null) ? documentHandler.getDataAsString() : null;
    }

    /**
     * sets XML data of this object
     */
    public void setData(Document data) {
        documentHandler = new XMLHandler(data);
    }

    /**
     * sets XML data of this object in String format
     */
    public void setData(String data) {
        documentHandler = new XMLHandler();
        documentHandler.setData(data);
    }

    /**
     * @return id of the comment
     */
    public int getId() {
        return id;
    }

    public abstract void setId(int id);

    /**
     * @return id of an author, or null, if he was anonymous
     */
    public abstract Integer getAuthor();

    public abstract void setAuthor(Integer author);

    /**
     * @return name of anonymous user or null
     */
    public String getAnonymName() {
        Element element = (Element) getData().selectSingleNode("/data/author");
        if (element != null)
            return element.getText();
        return null;
    }

    /**
     * @return id of comment, that is parent of this comment or null, if it is top-level comment.
     */
    public Integer getParent() {
        return parent;
    }

    public abstract void setParent(Integer parent);

    /**
     * @return date, when this comment was created
     */
    public abstract Date getCreated();

    public abstract void setCreated(Date created);

    /**
     * @return title for this comment
     */
    public String getTitle() {
        Element element = (Element) getData().selectSingleNode("/data/title");
        if (element!=null)
            return element.getText();
        return null;
    }

    public void setTitle(String title) {
        DocumentHelper.makeElement(getData().getRootElement(), "title").setText(title);
    }

    /**
     * @return text for this comment
     */
    public String getText() {
        Element element = (Element) getData().selectSingleNode("/data/text");
        if (element != null)
            return element.getText();
        return null;
    }

    /**
     * Finds all attachments associated with this comment.
     * @return list (never null) of relation identifiers.
     */
    public List<Integer> getAttachments() {
        List elements = (List) getData().selectNodes("/data/attachment");
        if (elements == null || elements.isEmpty())
            return Collections.emptyList();

        List<Integer> relations = new ArrayList<Integer>();
        for (Iterator iter = elements.iterator(); iter.hasNext();) {
            Element element = (Element) iter.next();
            relations.add(Misc.parseInt(element.getText(), -1));
        }
        return relations;
    }

    /**
     * Appends reply to this comment.
     * @param comment comment to be added as child of this comment
     */
    public void addChild(Comment comment) {
        if (children == null)
            children = new ArrayList<Comment>();
        children.add(comment);
    }

    /**
     * Removes given direct child.
     * @param comment comment to be removed
     * @param discussionRecord if set, id of removed comment will be added to list of deleted comments
     * @return true if comment was removed
     */
    public boolean removeChild(Comment comment, DiscussionRecord discussionRecord) {
        if (children == null)
            return false;
        if (! children.remove(comment))
            return false;
        if (discussionRecord != null) {
            LinkedList stack = new LinkedList();
            stack.add(comment);
            while (stack.size() > 0) {
                comment = (RowComment) stack.removeFirst();
                discussionRecord.addDeletedComment(((RowComment) comment).getRowId());
                stack.addAll(comment.getChildren());
            }
        }
        return true;
    }

    /**
     * Removes all child comments.
     * @param discussionRecord if set, id of removed comments will be added to list of deleted comments
     */
    public void removeAllChildren(DiscussionRecord discussionRecord) {
        if (children == null)
            return;
        if (discussionRecord == null) {
            children.clear();
            return;
        }

        LinkedList stack = new LinkedList();
        stack.addAll(getChildren());
        children.clear();
        while (stack.size() > 0) {
            RowComment comment = (RowComment) stack.removeFirst();
            discussionRecord.addDeletedComment(comment.getRowId());
            stack.addAll(comment.getChildren());
        }
    }

    /**
     * @return Child comments of this node.
     */
    public List<Comment> getChildren() {
        return (children != null) ? children : Collections.EMPTY_LIST;
    }

    /**
     * Attempts to find a comment with the same content in child comments
     * @param c other comment
     * @return existing comment, if found
     */
    public Comment findComment(Comment c) {
        if (children == null)
            return null;
        for (Comment comment : children) {
            if (comment.contentEquals(c))
                return comment;        }
        return null;
    }

    /**
     * Helper method that re-sorts direct child comments by their ids.
     */
    public void sortChildren() {
        Collections.sort(children);
    }

    public int hashCode() {
        return getId();
    }

    public boolean equals(Object obj) {
        if ( !(obj instanceof Comment) ) return false;
        return getId() == ((Comment)obj).getId();
    }

    public boolean contentEquals(Comment c) {
        if ( ! Misc.same(c.getAuthor(), getAuthor()))
            return false;
        if ( ! Misc.same(c.getAnonymName(), getAnonymName()))
            return false;
        if ( ! Misc.same(c.getTitle(), getTitle()))
            return false;
        return Misc.same(c.getText(), getText());
    }

    public int compareTo(Object o) {
        return getId() - ((Comment)o).getId();
    }

    public String toString() {
        return getId()+ " - " + getTitle();
    }

    public Object clone() {
        try {
            Comment clone = (Comment) super.clone();
            if (documentHandler != null)
                clone.documentHandler = (XMLHandler) documentHandler.clone(true);
            if (clone.children != null) {
                clone.children = new ArrayList<Comment>(children.size());
                for (Iterator iter = children.iterator(); iter.hasNext();) {
                    Comment comment = (Comment) iter.next();
                    clone.children.add((Comment) comment.clone());
                }
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
