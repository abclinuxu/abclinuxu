/*
 * User: literakl
 * Date: Feb 21, 2002
 * Time: 8:42:42 PM
 * (c)2001-2002 Tinnio
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
                    created = Constants.isoFormat.parse(value);
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
