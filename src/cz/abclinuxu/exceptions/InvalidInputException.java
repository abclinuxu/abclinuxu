/*
 * User: literakl
 * Date: 22.7.2003
 * Time: 7:06:22
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * Indicates, that user input is invalid.
 */
public class InvalidInputException extends AbcException {
    public InvalidInputException(String desc) {
        super(desc);
    }

    public InvalidInputException(String desc, Exception e) {
        super(desc, e);
    }
}
