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
     */
    public PersistanceException(String desc, int code) {
        super(desc, code);
    }
}
