/*
 * User: literakl
 * Date: Nov 20, 2001
 * Time: 10:25:41 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu;

/**
 * Base class for all exceptions.
 */
public class AbcException extends RuntimeException {

    /**
     * Superclass for all exceptions from AbcLinuxu.
     * @param desc description of exception
     */
    public AbcException(String desc) {
        super(desc);
    }

    /**
     * Superclass for all exceptions from AbcLinuxu.
     * @param desc description of exception
     */
    public AbcException(String desc, Exception e) {
        super(desc,e);
    }
}
