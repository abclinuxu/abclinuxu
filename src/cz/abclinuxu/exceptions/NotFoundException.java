/*
 * User: literakl
 * Date: Dec 25, 2002
 * Time: 9:17:49 PM
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * This exception is thrown, when object is not found.
 */
public class NotFoundException extends AbcException {

    /**
     * This exception is thrown, when object is not found.
     */
    public NotFoundException(String desc, Exception e) {
        super(desc, e);
    }

    /**
     * This exception is thrown, when object is not found.
     */
    public NotFoundException(String desc) {
        super(desc);
    }
}
