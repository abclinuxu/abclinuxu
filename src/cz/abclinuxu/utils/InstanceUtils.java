/*
 * Copyright Leos Literak 2001
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.AbcException;

import java.util.Map;
import java.util.Iterator;

/**
 * Utilities related to classes and objects.
 */
public class InstanceUtils {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstanceUtils.class);

    /**
     * Retrieves parameter <code>name</code> from <code>params</code>. If it is not
     * defined, it returns null. Then it tries convert it to integer. If it is not
     * successful, it returns null again. Then it tries to create new instance
     * os <code>clazz</code>. If it fails, it returns null. Finally it calls setId
     * with retrieved in as argument and returns created instance.
     */
    public static GenericObject instantiateParam(String name, Class clazz, Map params) {
        String tmp = (String) params.get(name);
        if ( tmp==null || tmp.length()==0 )
            return null;
        try {
            int id = Misc.parsePossiblyWrongInt(tmp);
            GenericObject obj = (GenericObject) clazz.newInstance();
            obj.setId(id);
            return obj;
        } catch (Exception e) {
            throw new AbcException(e.getMessage(),e);
        }
    }

    /**
     * Instantiates GenericObject from <<code>params</code>. It first looks up <<code>shortcut</code>
     * and if it is not defined, it searches for <<code>name</code>.
     * @see GenericObject instantiateParam(String, Class, Map)
     */
    public static GenericObject instantiateParam(String shortcut, String name, Class clazz, Map params) {
        GenericObject obj = instantiateParam(shortcut,clazz,params);
        if ( obj!=null )
            return obj;
        obj = instantiateParam(name, clazz, params);
        return obj;
    }

    /**
     * Verifies, that given object is derived of specific class and optionally
     * is specific type.
     */
    public static boolean checkType(GenericObject obj, Class aClass, int type) {
        if ( ! obj.getClass().isAssignableFrom(aClass) )
            return false;
        if ( obj instanceof Item ) {
            if ( ((Item)obj).getType()!=type )
               return false;
            return true;
        }
        if ( obj instanceof Record ) {
            if ( ((Record)obj).getType()!=type )
               return false;
            return true;
        }
        return true;
    }

    /**
     * Finds first child of item, that is Record and has type recordType.
     * If there is no such child, null is returned. As side efect, child
     * is initialized.
     */
    public static Relation findFirstChildRecordOfType(Item item, int recordType) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Record record = null;
        for (Iterator iter = item.getContent().iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            if ( rel.getChild() instanceof Record ) {
                persistance.synchronize(rel.getChild());
                record = (Record) rel.getChild();
                if ( record.getType()==recordType )
                    return rel;
            }
        }
        return null;
    }

    /**
     * Finds first child of item, that is Item and has type itemType.
     * If there is no such child, null is returned. As side efect, child
     * is initialized.
     */
    public static Relation findFirstChildItemOfType(GenericObject obj, int itemType) {
        Persistance persistance = PersistanceFactory.getPersistance();
        Item item = null;
        for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
            Relation rel = (Relation) iter.next();
            if ( rel.getChild() instanceof Item ) {
                persistance.synchronize(rel.getChild());
                item = (Item) rel.getChild();
                if ( item.getType()==itemType )
                    return rel;
            }
        }
        return null;
    }
}
