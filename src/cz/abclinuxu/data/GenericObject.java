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
        id = obj.getId();
        content = obj.getContent();
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
     * @param user initialized User
     * @return True, if this user may manage this resource
     */
    public boolean isManagedBy(User user) {
        if ( user==null || user.getId()==0 ) return false;

        // search use for admin flag
        if ( user.isInitialized() ) {
            for (Iterator iter = user.getContent().iterator(); iter.hasNext();) {
                GenericObject obj = (GenericObject) iter.next();
                if ( obj instanceof AccessRights ) {
                    return ((AccessRights)obj).isAdmin();
                }
            }
        }

        // use introspection to find owner field
        try {
            BeanInfo info = Introspector.getBeanInfo(this.getClass());
            PropertyDescriptor[] pd = info.getPropertyDescriptors();
            for ( int i=0; i<pd.length; i++ ) {
                Method method = pd[i].getReadMethod();
                if ( method.getName().equals("getOwner") ) {
                    int owner = ((Integer)method.invoke(this,new Object[0])).intValue();
                    if ( owner==user.getId() ) return true;
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
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
