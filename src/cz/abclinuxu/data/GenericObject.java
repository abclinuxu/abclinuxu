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

import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.persistence.Nursery;

import java.util.List;

/**
 * Superclass for all classes in this website
 */
public abstract class GenericObject implements Cloneable {
    /** unique identifier of this object */
    protected int id;
    /** tells, whether this object was already initialized by Persistance */
    protected boolean initialized = false;


    public GenericObject() {
        id = 0;
    }

    public GenericObject(int id) {
        this.id = id;
    }

    /**
     * @return identifier of this object
     */
    public int getId() {
        return id;
    }

    /**
     * Sets id. (only Persistance may call this method!)
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Adds relation to new child. This object must equal to parent in the relation.
     */
    public void addChildRelation(Relation relation) {
        if (!relation.getParent().equals(this))
            throw new InternalException("Cannot add "+relation+" as child of "+this);
        Nursery.getInstance().addChild(relation);
    }

    /**
     * Removes child relation. This object must equal to parent in the relation.
     */
    public void removeChildRelation(Relation relation) {
        if (!relation.getParent().equals(this))
            throw new InternalException("Cannot remove "+relation+" as child of "+this);
        Nursery.getInstance().removeChild(relation);
    }

    /**
     * @return list of child Relations
     */
    public List<Relation> getChildren() {
        return Nursery.getInstance().getChildren(this);
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( obj==this ) return;
        id = obj.getId();
        initialized = obj.isInitialized();
    }

    /**
     * Creates light clone of this object. Such clone is not
     * initialized except id, so its method equals will work.
     * @return same object, but only id is set
     */
    public GenericObject makeLightClone() {
        try {
            GenericObject o = (GenericObject) this.getClass().newInstance();
            o.setId(id);
            return o;
        } catch (Exception e) {
            throw new InternalException("Cannot create light clone of "+this,e);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append(": id="+id);
        return sb.toString();
    }

    /**
     * @return True, if object was initialized by Persistance. E.g., create, find or synchronize() method was called.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Sets initialization flag. To be used by Persistance.
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * This is usual equals() implementation. It shall be used in unit tests, because it is
     * more complete than equal. For equals, we just need to test class and PK equality, but
     * that's not enough for unit test.
     */
    public boolean preciseEquals(Object obj) {
        return super.equals(obj);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests equality of two GenericObjects. Only class and PK comparison is done. Needed
     * by HashMaps and Lists (especially in cache).
     */
    public boolean equals(Object obj) {
        if ( !getClass().isInstance(obj) ) return false;
        return id==((GenericObject)obj).getId();
    }

    public int hashCode() {
        String tmp = "GenericObject"+id;
        return tmp.hashCode();
    }
}
