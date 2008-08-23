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

import cz.abclinuxu.utils.Misc;

/**
 * This class serves as base class for Item, Category and Record,
 * which have very similar functionality and usage.
 */
public abstract class GenericDataObject extends CommonObject {
    static Logger log = Logger.getLogger(GenericDataObject.class);

    /** identifier of owner of this object */
    protected int owner;
	/** identifier of the group that owns this object */
	protected int group;
	/** permission mask for this object */
	protected int permissions;
    /** Type of the object. You must set it before storing with Persistance! */
    protected int type = 0;
    /** subtype of this object. String, max. length is 30 */
    protected String subType;
    /** title for this object, it may be null */
    protected String title;
    /** when this object was created */
    protected Date created;
    /** last update of this object */
    protected Date updated;
    // generic purpose properties
    protected Integer numeric1, numeric2;
    protected String string1, string2;
    protected Date date1, date2;

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
     * @return group's id
     */
    public int getGroup() {
        return group;
    }

    /**
     * sets group's id
     */
    public void setGroup(int group) {
        this.group = group;
    }

	/**
	 * @return object's permissions
	 */
	public int getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions New object's permissions
	 */
	public void setPermissions(int permissions) {
		this.permissions = permissions;
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
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @return first generic purpose number property
     */
    public Integer getNumeric1() {
        return numeric1;
    }

    public void setNumeric1(Integer numeric1) {
        this.numeric1 = numeric1;
    }

    /**
     * @return second generic purpose number property
     */
    public Integer getNumeric2() {
        return numeric2;
    }

    public void setNumeric2(Integer numeric2) {
        this.numeric2 = numeric2;
    }

    /**
     * @return first generic purpose string property
     */
    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    /**
     * @return second generic purpose string property
     */
    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    /**
     * @return first generic purpose date property
     */
    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    /**
     * @return second generic purpose string property
     */
    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
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
		group = b.group;
		permissions = b.permissions;
        type = b.type;
        title = b.title;
        subType = b.subType;
        created = b.created;
        updated = b.updated;
        custom = b.custom;
    }

    public Object clone() {
        GenericDataObject clone = (GenericDataObject) super.clone();

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
        if ( !( o instanceof GenericDataObject) )
            return false;
        GenericDataObject p = (GenericDataObject) o;
        if ( id != p.id )
            return false;
        if ( type != p.type )
            return false;
        if ( ! Misc.same(title, p.title) )
            return false;
        if ( ! Misc.same(subType, p.subType) )
            return false;
        if ( ! Misc.same(string1, p.string1) )
            return false;
        if ( ! Misc.same(string2, p.string2) )
            return false;
        if ( ! Misc.same(numeric1, p.numeric1) )
            return false;
        if ( ! Misc.same(numeric2, p.numeric2) )
            return false;
        if ( ! Misc.same(date1, p.date1) )
            return false;
        if ( ! Misc.same(date2, p.date2) )
            return false;
        if ( ! Misc.sameXml(getDataAsString(), p.getDataAsString()) )
            return false;
        return true;
    }

    /**
     * Compares content fields of this and that GenericObject. The argument
     * must be instance of same class and have same content properties.
     * @param obj compared class
     * @return true if both instances have same content
     */
    public boolean contentEquals(GenericObject obj) {
        if (obj == this)
            return true;
        if (! super.contentEquals(obj))
            return false;
        GenericDataObject p = (GenericDataObject) obj;
        if (type != p.type)
            return false;
        if (!Misc.same(title, p.title))
            return false;
        if (!Misc.same(subType, p.subType))
            return false;
        if (!Misc.same(string1, p.string1))
            return false;
        if (!Misc.same(string2, p.string2))
            return false;
        if (!Misc.same(numeric1, p.numeric1))
            return false;
        if (!Misc.same(numeric2, p.numeric2))
            return false;
        if (!Misc.same(date1, p.date1))
            return false;
        if (!Misc.same(date2, p.date2))
            return false;
        return Misc.same(custom, p.custom);
    }
}
