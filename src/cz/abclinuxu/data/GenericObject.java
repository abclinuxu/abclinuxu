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
    /** dependant GenericObjects */
    protected List content;

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
     * adds another dependant object
     */
    public void addContent(GenericObject object) {
        if ( content==null ) {
            content = new LinkedList();
        }
        content.add(object);
    }

    /**
     * @return dependant objects
     */
    public List getContent() {
        return content;
    }
}
