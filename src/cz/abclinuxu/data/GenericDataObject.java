/*
 * User: literakl
 * Date: Dec 11, 2001
 * Time: 7:53:13 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

import java.util.Date;

/**
 * This class serves as base class for Item, Category and Record,
 * which have very similar functionality and usage.
 */
public abstract class GenericDataObject extends GenericObject {
    /** identifier of owner of this object */
    protected int owner;
    /** creation date or last update of this object */
    protected Date updated;
    /** XML with data or this object */
    protected String data;


    public GenericDataObject() {
    }

    public GenericDataObject(int id) {
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

    public boolean equals(Object o) {
        if ( !( o instanceof GenericDataObject) ) return false;
        GenericDataObject p = (GenericDataObject) o;
        if ( id==p.id && owner==p.owner && data.equals(p.data) ) return true;
        return false;
    }
}
