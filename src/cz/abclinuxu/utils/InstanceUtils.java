/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.GenericObject;

import java.util.Map;

/**
 * Utilities related to classes and objects.
 */
public class InstanceUtils {

    /**
     * Compares two objects.
     * @return true, if both parameters are null or are equal.
     */
    public static boolean same(Object first, Object second) {
        if ( first!=null ) {
            if ( second==null ) return false;
            return first.equals(second);
        }
        return second==null;
    }

    /**
     * Retrieves parameter <code>name</code> from <code>params</code>. If it is not
     * defined, it returns null. Then it tries convert it to integer. If it is not
     * successful, it returns null again. Then it tries to create new instance
     * os <code>clazz</code>. If it fails, it returns null. Finally it calls setId
     * with retrieved in as argument and returns created instance.
     */
    public static GenericObject instantiateParam(String name, Class clazz, Map params) {
        String tmp = (String) params.get(name);
        if ( tmp==null || tmp.length()==0 ) return null;
        try {
            int id = Integer.parseInt(tmp);
            if ( ! GenericObject.class.isAssignableFrom(clazz) ) return null;
            GenericObject obj = (GenericObject) clazz.newInstance();
            obj.setId(id);
            return obj;
        } catch (Exception e) {
            return null;
        }
    }
}
