/*
 * Copyright Leos Literak 2001
 */
package abc;

import java.util.*;

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
    }

    public GenericObject(int id) {
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
    public void addContent(Relation object) {
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
    public void synchronizeWith(GenericObject obj) {};

    public String toString() {
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
     * @param user initialized User
     * @return True, if this user may manage this resource
     */
    public boolean isManagedBy(User user) {
        return false;
    }

    public int hashCode() {
        return id;
    }
}
