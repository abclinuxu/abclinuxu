/*
 * User: literakl
 * Date: 22.7.2003
 * Time: 7:06:22
 */
package cz.abclinuxu.exceptions;

/**
 * Persistance subexception indicating, that key is already in use. 
 */
public class DuplicateKeyException extends PersistanceException {
    public DuplicateKeyException(String desc) {
        super(desc);
    }

    public DuplicateKeyException(String desc, Exception e) {
        super(desc, e);
    }
}
