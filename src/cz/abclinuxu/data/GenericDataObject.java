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
package cz.abclinuxu.data;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

/**
 * This class serves as base class for Item, Category and Record,
 * which have very similar functionality and usage.
 */
public abstract class GenericDataObject extends CommonObject {
    static Logger log = Logger.getLogger(GenericDataObject.class);

    /** identifier of owner of this object */
    protected int owner;
    /** Type of the object. You must set it before storing with Persistance! */
    protected int type = 0;
    /** subtype of this object. String, max. length is 30 */
    protected String subType;
    /** when this object was created */
    protected Date created;
    /** last update of this object */
    protected Date updated;

    /**
     * Helper (non-persistant) String for findByExample(),
     * which works as argument to search in <code>data</code>.
     **/
    protected String searchString;
    /** some object related to this instance, for any use */
    protected Object custom;

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
     * @return Subtype of this object
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets subtype of this object.
     * @param subType max size is 30
     */
    public void setSubType(String subType) {
        if (subType!=null && subType.length()>30)
            throw new java.lang.IllegalArgumentException("Subtype is too long: "+subType);
        this.subType = subType;
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
     * @return some object associated with this instance
     */
    public Object getCustom() {
        return custom;
    }

    /**
     * Associate custom object with this instance.
     *
     * @param custom some object, its type typically depends of type
     */
    public void setCustom(Object custom) {
        this.custom = custom;
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
        subType = b.subType;
//        documentHandler = (XMLHandler) b.documentHandler.clone();
        documentHandler = b.documentHandler;
        created = b.created;
        updated = b.updated;
        properties = b.properties;
        custom = b.custom;
    }

    public Object clone() {
        GenericDataObject clone = (GenericDataObject) super.clone();
        if (documentHandler != null)
            clone.documentHandler = (XMLHandler) documentHandler.clone();

        if (properties != null) {
            clone.properties = new HashMap<String, Set<String>>();
            for (Map.Entry<String, Set<String>> entry : properties.entrySet()) {
                clone.properties.put(entry.getKey(), new HashSet<String>((Collection) entry.getValue()));
            }
        }

        if (custom != null) {
            try {
                Method m = custom.getClass().getDeclaredMethod("clone", (Class[]) null);
                m.setAccessible(true);
                clone.custom = m.invoke(custom, (Object[]) null);
            } catch (Exception e) {
                clone.custom = custom;
                log.error("Cannot clone "+custom+" in "+this, e);
            }
        }
        return clone;
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
