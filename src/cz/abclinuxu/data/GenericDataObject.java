/*
 * User: literakl
 * Date: Dec 11, 2001
 * Time: 7:53:13 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.data;

import java.util.Date;
import java.util.Iterator;
import cz.abclinuxu.AbcException;
import org.dom4j.Document;

/**
 * This class serves as base class for Item, Category and Record,
 * which have very similar functionality and usage.
 */
public abstract class GenericDataObject extends GenericObject {

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(GenericDataObject.class);

    /** identifier of owner of this object */
    protected int owner;
    /** creation date of last update of this object */
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
    public void setData(String data) throws AbcException {
        documentHandler = new XMLHandler();
        documentHandler.setData(data);
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( ! (obj instanceof GenericDataObject) ) return;
        if ( obj==this ) return;
        super.synchronizeWith(obj);
        GenericDataObject b = (GenericDataObject) obj;
        documentHandler = new XMLHandler(b.getData());
        owner = b.getOwner();
        updated = b.getUpdated();
    }

    /**
     * @return Helper (non-persistant) String for findByExample(), which
     * works as argument to search in <code>data</code>.
     **/
    public String getSearchString() {
        return searchString;
    }

    /**
     * @param user initialized User
     * @return True, if this user may manage this resource
     */
    public boolean isManagedBy(User user) {
        if ( user==null || user.getId()==0 ) return false;

        if ( owner==user.getId() ) return true;
        if ( this instanceof Category ) {
            if ( ((Category)this).isOpen() ) return true;
        }

        // search user for admin flag
        if ( user.isInitialized() ) {
            for (Iterator iter = user.getContent().iterator(); iter.hasNext();) {
                GenericObject obj = (GenericObject) ((Relation) iter.next()).getChild();
                if ( obj instanceof AccessRights ) {
                    return ((AccessRights)obj).isAdmin();
                }
            }
        }

        return false;
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
