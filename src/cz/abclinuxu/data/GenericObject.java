/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.*;
import java.beans.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * superclass for all classes in this website
 */
public class GenericObject {
    /** unique identifier of this object */
    protected int id;
    /** list of Relations, where relation.getParent()==this and relation.getChild().getId()!=0 */
    protected List content;
    /** tells, whether this object was already initialized by Persistance */
    protected boolean initialized = false;


    public GenericObject() {
        id = 0;
        content = new ArrayList(5);
    }

    public GenericObject(int id) {
        this.id = id;
        content = new ArrayList(5);
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
     * Adds another dependant object. If <code>object</code> has been already stored,
     * it will be replaced by this (hopefully) fresher version.
     */
    public void addContent(Relation object) {
        content.remove(object);
        content.add(object);
    }

    /**
     * @return Relations, where getParent()==this. If getChild().isInitialized()==false,
     * you shall call Persistance.synchronize() to load data from persistant storage.
     */
    public List getContent() {
        return content;
    }

    /**
     * Sets content. Null parameter is prohibited! (only Persistance may call this method!)
     */
    public void setContent(List content) {
        this.content = content;
    }

    /**
     * Removes all references to children. (only Persistance may call this method!)
     */
    public void clearContent() {
        content.clear();
    }

    /**
     * Initialize this object with values from <code>obj</code>, if
     * this.getClass.equals(obj.getClass()).
     */
    public void synchronizeWith(GenericObject obj) {
        if ( obj==this ) return;
        id = obj.getId();
        content = new ArrayList(obj.getContent().size());
        for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
            content.add(iter.next());
        }
        initialized = obj.isInitialized();
    };

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
