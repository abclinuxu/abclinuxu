/*
 * User: literakl
 * Date: Dec 11, 2001
 * Time: 7:53:13 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

import org.dom4j.Document;

import java.util.Date;

/**
 * This class serves as base class for Item, Category and Record,
 * which have very similar functionality and usage.
 */
public abstract class GenericDataObject extends GenericObject implements XMLContainer {
    /** identifier of owner of this object */
    protected int owner;
    /** Type of the object. You must set it before storing with Persistance! */
    int type = 0;
    /** when this object was created */
    protected Date created;
    /** last update of this object */
    protected Date updated;
    /** XML with data of this object */
    protected XMLHandler documentHandler;
    /**
     * Helper (non-persistant) String for findByExample(),
     * which works as argument to search in <code>data</code>.
     **/
    protected String searchString;


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
     * @return Type of Record
     */
    public int getType() {
        return type;
    }

    /**
     * Sets type of Record
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return last updated date
     */
    public Date getUpdated() {
        return updated;
    }

    /**
     * sets last updated date
     */
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    /**
     * @return date, when this object was created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * sets date, when this object was created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return XML data of this object
     */
    public Document getData() {
        return (documentHandler!=null)? documentHandler.getData():null;
    }

    /**
     * @return XML data in String format
     */
    public String getDataAsString() {
        return (documentHandler!=null)? documentHandler.getDataAsString():null;
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
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass().equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (this.getClass().equals(obj.getClass())) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);

        GenericDataObject b = (GenericDataObject) obj;
        owner = b.owner;
        type = b.type;
//        documentHandler = (XMLHandler) b.documentHandler.clone();
        documentHandler = b.documentHandler;
        created = b.created;
        updated = b.updated;
    }

    /**
     * @return Helper (non-persistant) String for findByExample(), which
     * works as argument to search in <code>data</code>.
     **/
    public String getSearchString() {
        return searchString;
    }

    /**
     * Sets elper (non-persistant) String for findByExample(), which
     * works as argument to search in <code>data</code>.
     **/
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public boolean preciseEquals(Object o) {
        if ( !( o instanceof GenericDataObject) ) return false;
        GenericDataObject p = (GenericDataObject) o;
        if ( id==p.id && owner==p.owner && getDataAsString().equals(p.getDataAsString()) ) return true;
        return false;
    }
}
