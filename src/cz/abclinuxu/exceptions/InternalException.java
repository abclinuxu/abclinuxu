/*
 * User: literakl
 * Date: 22.7.2003
 * Time: 7:06:22
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * Indicates error in code.
 */
public class InternalException extends AbcException {
    public InternalException(String desc) {
        super(desc);
    }

    public InternalException(String desc, Exception e) {
        super(desc, e);
    }
}
