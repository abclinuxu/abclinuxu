/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.data;

import java.util.*;

/**
 * superclass for all classes in this website
 */
public class GenericObject {
    /** unique identifier of this object */
    protected int id;
    /** dependant GenericObjects, just empty instances with filled <code>id</code> */
    protected List content;
    /** tells, whether this object was already intitilized by Persistance */
    protected boolean initialized = false;


    public GenericObject() {
        id = 0;
        content = new ArrayList(3);
        initialized = false;
    }

    public GenericObject(int id) {
        this.id = id;
        content = new ArrayList(3);
        initialized = false;
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
     * adds another dependant object
     */
    public void addContent(GenericObject object) {
        content.add(object);
    }

    /**
     * @return dependant objects. These objects don't contain data, except their <code>id</code>.
     * You should call Persistance.loadObject() to lookup them from persistable storage.
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
     * Sets initialization flad. To be used by Persistance.
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
