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
}
