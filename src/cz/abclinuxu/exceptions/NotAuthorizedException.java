/*
 * User: literakl
 * Date: Dec 25, 2002
 * Time: 9:28:23 PM
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * This exception is thrown, when user tries to perform
 * action, where his rights are not sufficient.
 */
public class NotAuthorizedException extends AbcException {

    /**
     * This exception is thrown, when user tries to perform
     * action, where his rights are not sufficient.
     */
    public NotAuthorizedException(String desc) {
        super(desc);
    }

    /**
     * This exception is thrown, when user tries to perform
     * action, where his rights are not sufficient.
     */
    public NotAuthorizedException(String desc, Exception e) {
        super(desc, e);
    }
}
