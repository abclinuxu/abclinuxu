/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Record;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.servlets.html.view.ViewUser;
import cz.abclinuxu.servlets.html.view.ShowObject;
import cz.abclinuxu.servlets.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;

/**
 * Utilities related to classes and objects.
 */
public class InstanceUtils {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstanceUtils.class);
    static Map deprecatedParams;
    static REProgram impliedParam;

    static {
        deprecatedParams = new HashMap();
        deprecatedParams.put(ViewUser.PARAM_USER_SHORT, ViewUser.PARAM_USER);
        deprecatedParams.put(ShowObject.PARAM_RELATION_SHORT, ShowObject.PARAM_RELATION);
        try {
            impliedParam = new RECompiler().compile("/(\\d+)($|#|;jsessionid)");
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
            RE regexp = new RE(impliedParam);
            if ( regexp.match(url) ) {
                tmp = regexp.getParen(1);
                params.put(name, tmp);
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
     * Creates new list with initialized objects only.
     * @return list with initialized objects
     */
    public static <T extends GenericObject> List<T> skipMissing(List<T> objects) {
        List<T> list = new ArrayList<T>(objects.size());
        for (T object : objects) {
            if (object.isInitialized())
                list.add(object);
        }
        return list;
    }

    /**
     * Verifies, that given object is derived of specific class and optionally
     * is specific type.
     */
    public static boolean checkType(GenericObject obj, Class aClass, int type) {
        if ( ! obj.getClass().isAssignableFrom(aClass) )
            return false;
        if (obj instanceof Item)
            return ((Item) obj).getType() == type;
        if (obj instanceof Record)
            return ((Record) obj).getType() == type;

        return true;
    }

    /**
     * Finds first child of item, that is Record and has specified type.
     * If there is no such child, null is returned.
     * @return initialized relation to Record of specified type or null
     */
    public static Relation getFirstChildRecordRelation(Item item, int type) {
        Record record;
        for (Relation relation : item.getChildren()) {
            if (relation.getChild() instanceof Record) {
                Tools.sync(relation);
                record = (Record) relation.getChild();
                if (record.getType() == type)
                    return relation;
            }
        }
        return null;
    }

    /**
     * Finds first child of obj, that is Item and has specified type.
     * If there is no such child, null is returned.
     * @return initialized relation to Item of specified type or null
     */
    public static Relation getFirstItemRelation(GenericObject obj, int type) {
        Item item;
        for (Relation relation : obj.getChildren()) {
            if (relation.getChild() instanceof Item) {
                Tools.sync(relation);
                item = (Item) relation.getChild();
                if (item.getType() == type)
                    return relation;
            }
        }
        return null;
    }

    /**
     * Finds first child, that is Category and has specified type.
     * If there is no such child, null is returned.
     * @return initialized relation to category of specified type or null
     */
    public static Relation getFirstCategoryRelation(GenericObject obj, int type) {
        Category category;
        for (Relation relation : obj.getChildren()) {
            if (relation.getChild() instanceof Category) {
                Tools.sync(relation);
                category = (Category) relation.getChild();
                if (category.getType() == type)
                    return relation;
            }
        }
        return null;
    }

    /**
     * Creates instances of users and initializes them.
     *
     * @param identifiers list of ids
     * @return initialized User instances
     */
    public static List<User> createUsers(List<Integer> identifiers) {
        List<User> userObjects = new ArrayList<User>(identifiers.size());
        for (Integer id : identifiers) {
            userObjects.add(new User(id));
        }
        Tools.syncList(userObjects);
        return userObjects;
    }
}
