/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:27:09 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.AbcException;

/**
 * Exception related to persistance
 */
public class PersistanceException extends AbcException {

    /**
     * constructs new exception and logs relevant information
     * @param desc description of exception
     * @param code error code of exception
     * @param sinner if known or relevant, object, which caused this exception (or null, if unknown)
     * @param e caught exception or null
     */
    public PersistanceException(String desc, int code, Object sinner, Exception e) {
        super(desc, code, sinner, e);
    }
}
