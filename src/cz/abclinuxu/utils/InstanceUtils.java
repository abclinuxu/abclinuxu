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
import cz.abclinuxu.AbcException;
import cz.abclinuxu.servlets.view.ViewUser;
import cz.abclinuxu.servlets.view.ViewRelation;
import cz.abclinuxu.servlets.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Utilities related to classes and objects.
 */
public class InstanceUtils {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstanceUtils.class);
    static Map deprecatedParams;
    static RE impliedParam;

    static {
        deprecatedParams = new HashMap();
        deprecatedParams.put(ViewUser.PARAM_USER_SHORT, ViewUser.PARAM_USER);
        deprecatedParams.put(ViewRelation.PARAM_RELATION_SHORT, ViewRelation.PARAM_RELATION);
        try {
            impliedParam = new RE("/([0-9]+)$");
        } catch (RESyntaxException e) {
            log.error("Regexp cannot be compiled!", e);
        }
    }

    /**
     * Retrieves parameter <code>name</code> from <code>params</code>. If it is not
     * defined, it seeks, whether there is deprecated variant of the name and uses it.
     * If still the value of param is not retrieved, the attempt to extract it from request
     * URL is made. If the param is empty, null is returned.<p>
     * Next step is to convert the param to integer, instantiate the class and call
     * setId(int) method on new object with parameter converted to int.
     */
    public static GenericObject instantiateParam(String name, Class clazz, Map params, HttpServletRequest request) {
        String tmp = (String) params.get(name);
        if ( tmp==null && request!=null) {
            String url = ServletUtils.combinePaths(request.getServletPath(), request.getPathInfo());
            if ( impliedParam.match(url) ) {
                tmp = impliedParam.getParen(1);
            }
        }
        if ( tmp==null ) {
            Object oldName = deprecatedParams.get(name);
            if (oldName!=null)
                tmp = (String) params.get(oldName);
        }
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
