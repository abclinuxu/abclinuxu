/*
 * User: literakl
 * Date: 22.7.2003
 * Time: 7:06:22
 */
package cz.abclinuxu.exceptions;

import cz.abclinuxu.AbcException;

/**
 * Thrown, when there is some error while accessing persistant storage.
 */
public class PersistanceException extends AbcException {
    public PersistanceException(String desc) {
        super(desc);
    }

    public PersistanceException(String desc, Exception e) {
        super(desc, e);
    }
}
