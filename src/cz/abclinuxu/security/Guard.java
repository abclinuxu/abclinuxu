/*
 * User: literakl
 * Date: Jan 30, 2002
 * Time: 2:44:43 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.security;

import cz.abclinuxu.data.*;

/**
 * Class, that is responsible for checking access to resources.
 */
public final class Guard {

    public static final int OPERATION_ADD = 1;
    public static final int OPERATION_EDIT = 2;
    public static final int OPERATION_REMOVE = 3;
    public static final int OPERATION_MOVE = 4;
    public static final int OPERATION_VIEW = 5;

    /** action was confirmed */
    public static final int ACCESS_OK = 10;
    /** action forbidden, login required */
    public static final int ACCESS_LOGIN = 11;
    /** action forbidden, insufficient rights */
    public static final int ACCESS_DENIED = 12;


    /**
     * Method, that checks access right of the user for selected operation.
     * @param actor User, whose rights we are checking. It may be null.
     * @param subject Affected object. It cannot be null.
     * @param operation Constant, describing operation, that actor is willing
     * to perform on subject.
     * @param param param Additional information, that me be needed to decide,
     * whether operation will be granted. For <code>OPERATION_ADD</code>, it is
     * a Class of object to be added.
     * @return Constant, describing result.
     */
    public static int check(User actor, GenericObject subject, int operation, Object param) {
        if ( operation==OPERATION_VIEW ) return ACCESS_OK;
        if ( actor==null || !actor.isInitialized() ) return ACCESS_LOGIN;
        if ( actor.isAdmin() ) return ACCESS_OK;

        if ( operation==OPERATION_ADD ) {
            return checkAdd(actor,subject,param);
        } else if ( operation==OPERATION_EDIT ) {
            return checkEdit(actor,subject,param);
        } else if ( operation==OPERATION_REMOVE ) {
        }

        return ACCESS_DENIED;
    }

    /** non administrator is adding something */
    private static int checkAdd(User actor, GenericObject subject, Object param) {
        if ( subject instanceof Category ) {
            Class clazz = (Class) param;
            Category category = (Category) subject;

            // owner may add anything to category
            if ( category.getOwner()==actor.getId() ) return ACCESS_OK;
            // subcategory may add only admin or owner
            if ( clazz==Category.class ) return ACCESS_DENIED;
            // anybody may add anything (except category) to open category
            if ( category.isOpen() ) return ACCESS_OK;
            // non admin, not owner, closed category
            return ACCESS_DENIED;
        }
        if ( subject instanceof Item ) {
            Item item = (Item) subject;
            Class clazz = (Class) param;

            // owner may add anything
            if ( item.getOwner()==actor.getId() ) return ACCESS_OK;
            // articles are restricted to owners and adminsitrators
            if ( item.getType()==Item.ARTICLE ) return ACCESS_DENIED;
            // anybody may add record to item
            if ( clazz==Record.class ) return ACCESS_OK;
            // anything else is restricted
            return ACCESS_DENIED;
        }
        return ACCESS_DENIED;
    }

    /** non administrator is editing something */
    private static int checkEdit(User actor, GenericObject subject, Object param) {
        if ( subject instanceof GenericDataObject ) {
            GenericDataObject data = (GenericDataObject) subject;
            if ( data.getOwner()==actor.getId() ) return ACCESS_OK;
            return ACCESS_DENIED;
        }
        if ( subject instanceof User ) {
            String password = (String) param;
            if ( actor.getId()!=subject.getId() ) return ACCESS_DENIED;
            if ( ((User)actor).validatePassword(password) ) return ACCESS_OK;
            return ACCESS_DENIED;
        }
        // Link
        // Data
        return ACCESS_DENIED;
    }
}
