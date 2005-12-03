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
import java.util.Date;
import java.util.ArrayList;

import org.dom4j.Element;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Item;

/**
 * Comment is a holder of one reaction in discussion.
 */
public class Comment {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Comment.class);

    List children;
    Element data;
    // computed from data
    Integer id, parent;
    User author;
    Date created;
    boolean unread;
    Integer nextUnread;
    boolean inBlacklist;

    /**
     * Creates new instance of Discussion.
     */
    public Comment(Element comment) {
        children = new ArrayList(3);
        data = comment;
    }

    /**
     * Creates new instance of Discussion.
     */
    public Comment(Element comment, Date created, Integer id, Integer parent, User author) {
        children = new ArrayList(3);
        data = comment;
        this.created = created;
        this.id = id;
        this.parent = parent;
        this.author = author;
    }

    /**
     * Creates facade of comment arround Item - typically question.
     */
    public Comment(Item item) {
        data = item.getData().getRootElement();
        created = item.getCreated();
        id = new Integer(0);
        parent = new Integer(0);
        if ( item.getOwner()!=0 )
            author = new User(item.getOwner());
    }

    /**
     * @return data of this Comemnt
     */
    public Element getData() {
        return data;
    }

    /**
     * Shortcut for identifier of the comment.
     * @return extracts id of the comment
     */
    public Integer getId() {
        if (id==null) {
            String value = data.attributeValue("id");
            if ( value!=null )
                id = Integer.valueOf(value);
        }
        return id;
    }

    /**
     * Shortcut for author of the comment, if he was logged in.
     * @return author, or null, if he is not known.
     */
    public User getAuthor() {
        if (author==null) {
            String value = data.elementText("author_id");
            if (value!=null)
                author = new User(Integer.parseInt(value));
        }
        return author;
    }

    /**
     * Shortcut for parent of this comment.
     * @return id of comment, that is parent of this comment or 0, if it is top-level comment.
     */
    public Integer getParent() {
        if ( parent==null ) {
            String value = data.elementText("parent");
            if ( value!=null )
                parent = Integer.valueOf(value);
        }
        return parent;
    }

    /**
     * Shortcut for time, when this comment was added.
     * @return date, , when this comment was added.
     */
    public Date getCreated() {
        if (created==null) {
            String value = data.elementText("created");
            try {
                if ( value!=null && value.length()>0)
                    synchronized (Constants.isoFormat) {
                        created = Constants.isoFormat.parse(value);
                    }
                else
                    log.error("Empty date in comment "+getId()+"! Comment:\n"+data.asXML());
            } catch (Exception e) {
                log.error("Malformed date in comment "+getId()+"! Value='"+value+"'. Comment:\n"+data.asXML(), e);
            }
        }
        return created;
    }

    /**
     * Add reaction to current comment.
     * @param comment comment to be added as child of current comment
     */
    public void addChild(Comment comment) {
        children.add(comment);
    }

    /**
     * @return Child comments of this node.
     */
    public List getChildren() {
        return children;
    }

    /**
     * @return true, if this comment has not been displayed to the user
     */
    public boolean isUnread() {
        return unread;
    }

    /**
     * Sets whether this comment has been displayed to the user
     * @param unread
     */
    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    /**
     * @return id of next unread comment
     */
    public Integer getNextUnread() {
        return nextUnread;
    }

    /**
     * Sets id of next unread comment
     * @param nextUnread
     */
    public void setNextUnread(Integer nextUnread) {
        this.nextUnread = nextUnread;
    }
    
    /**
     * @return true, if the author of this comment is in users blacklist 
     */
    public boolean isInBlacklist() {
        return inBlacklist;
    }

    /**
     * Sets whether the author of this comment is in users blacklist
     * @param inBlacklist
     */
    public void setInBlacklist(boolean inBlacklist) {
        this.inBlacklist = inBlacklist;
    }
    
    public int hashCode() {
        return getId().intValue();
    }

    public boolean equals(Object obj) {
        if ( !(obj instanceof Comment) ) return false;
        return getId().equals(((Comment)obj).getId());
    }

    public String toString() {
        return data.asXML();
    }
}
