/*
 * User: literakl
 * Date: Nov 20, 2001
 * Time: 10:25:41 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu;

public class AbcException extends RuntimeException {

    /** indicates unknown error */
    public static final int UNKNOWN = 0;
    /** indicates run time exception, usually error in code - e.g. NullPointerException */
    public static final int RUNTIME = 1;
    /** data has invalid value */
    public static final int WRONG_DATA = 2;
    /** indicates, that supplied data are in incorrect format */
    public static final int WRONG_FORMAT = 3;
    /** argument is missing */
    public static final int MISSING_ARGUMENT = 4;
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
    /** indicates, that command supplied to findByCommand method was wrong */
    public static final int DB_WRONG_COMMAND = 1009;

    /**
     * code code of error. These codes may be mapped
     * by user interfaces to custom error explanations.
     */
    int code;

    /**
     * constructs new exception and logs relevant information
     * @param desc description of exception
     * @param code error code of exception
     */
    public AbcException(String desc, int code) {
        super(desc);
        this.code = code;
    }

    /**
     * @return code code of error. These codes may be mapped
     * by user interfaces to custom error explanations.
     */
    public int getStatus() {
        return code;
    }
}
