/*
 * User: literakl
 * Date: Dec 25, 2002
 * Time: 9:28:23 PM
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * This exception is thrown, when some argument is missing.
 */
public class MissingArgumentException extends AbcException {

    /**
     * This exception is thrown, when some argument is missing.
     */
    public MissingArgumentException(String desc) {
        super(desc);
    }
}
