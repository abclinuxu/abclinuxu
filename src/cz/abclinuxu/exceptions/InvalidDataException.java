/*
 * User: literakl
 * Date: 22.7.2003
 * Time: 7:06:22
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * Indicates, that some data is invalid.
 */
public class InvalidDataException extends AbcException {
    public InvalidDataException(String desc) {
        super(desc);
    }

    public InvalidDataException(String desc, Exception e) {
        super(desc, e);
    }
}
