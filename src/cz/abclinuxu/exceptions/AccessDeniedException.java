/*
 * User: literakl
 * Date: Dec 25, 2002
 * Time: 9:28:23 PM
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * This exception is thrown, when user cannot access the object.
 */
public class AccessDeniedException extends AbcException {
    boolean ipAddressBlocked;

    /**
     * This exception is thrown, when user cannot access the object.
     */
    public AccessDeniedException(String desc, boolean ipAddressBlocked) {
        super(desc);
        this.ipAddressBlocked = ipAddressBlocked;
    }

    /**
     * Whether his exceptionw as thrown because of blocked IP address.
     */
    public boolean isIpAddressBlocked() {
        return ipAddressBlocked;
    }
}
