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

    public GenericObject(int id) {
        this.id = id;
	content = new LinkedList();
    }

    /**
     * @return identifier of this object
     */
    public int getId() {
        return id;
    }

    /**
     * sets id (only Persistance may call this method!)
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

    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append(": id="+id);
        return sb.toString();
    }
}
