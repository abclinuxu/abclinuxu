/*
 * User: literakl
 * Date: Nov 20, 2001
 * Time: 10:25:41 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu;

import org.apache.log4j.Category;

public class AbcException extends Exception {
    static Category log = Category.getInstance(AbcException.class);

    /** indicates unknown error */
    public static final int UNKNOWN = 0;
    /** indicates run time exception, usually error in code - e.g. NullPointerException */
    public static final int RUNTIME = 1;
    /** indicates, that connection to database failed */
    public static final int DB_REFUSED = 1000;
    /** indicates, that insert sql call failed */
    public static final int DB_INSERT = 1001;
    /** indicates, that insert failed, because their key is already used */
    public static final int DB_DUPLICATE = 1002;
    /** indicates, that update sql call failed */
    public static final int DB_UPDATE = 1003;
    /** indicates, that removal of object failed */
    public static final int DB_REMOVE = 1004;
     /** indicates, that select sql call failed */
    public static final int DB_FIND = 1004;
    /** indicates, that object was not found in db */
    public static final int DB_NOT_FOUND = 1005;
    /** indicates, that unsupported class was passed as argument / retrieved from database */
    public static final int DB_UNKNOWN_CLASS = 1006;
    /** indicates, that supplied class are incomplete */
    public static final int DB_INCOMPLETE = 1007;
    /** indicates, that supplied data are in incorrect format */
    public static final int DB_WRONG_DATA = 1008;

    /**
     * code code of error. These codes may be mapped
     * by user interfaces to custom error explanations.
     */
    int code;
    /**
     * object, which caused this exception (if known)
     */
    Object sinner;

    /**
     * constructs new exception and logs relevant information
     * @param desc description of exception
     * @param code error code of exception
     * @param sinner if known or relevant, object, which caused this exception (or null, if unknown)
     * @param e caught exception or null
     */
    public AbcException(String desc, int code, Object sinner, Exception e) {
        super(desc);
        this.code = code;
        this.sinner = sinner;
        log.error("Exception: "+desc+", code="+code+", sinner="+sinner,(e==null)?this:e);
    }

    /**
     * @return code code of error. These codes may be mapped
     * by user interfaces to custom error explanations.
     */
    public int getStatus() {
        return code;
    }

    /**
     * @return object, which caused this exception (if known)
     */
    public Object getSinner() {
        return sinner;
    }
}
