/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.Date;

/**
 * Category is a node of the tree (not leaf) */
 */
public class Category extends GenericObject {
    /** identifier of owner of this object */
    protected int owner;
    /** creation date or last update of this object */
    protected Date updated;
    /** XML with data or this object */
    protected String data;
    /** tells, whether normal users (non-administrators) can add items to this category */
    protected boolean open;


    public Category(int id) {
        super(id);
    }

    /**
     * @return owner's id
     */
    public int getOwner() {
        return owner;
    }

    /**
     * sets owner's id
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * @return last updated (or creation) date
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * sets last updated (or creation) date
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * @return data of this object in XML
     */
    public String getData() {
        return data;
    }

    /**
     * sets data of this object in XML
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return whether normal users may add content to this category
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * sets whether normal users may add content to this category
     */
    public void setOpen(boolean open) {
        this.open = open;
    }
}
