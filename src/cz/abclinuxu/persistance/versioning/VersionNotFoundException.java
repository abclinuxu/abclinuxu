package cz.abclinuxu.persistance.versioning;

/**
 * Indicates that either document or specified version was not found.
 * User: literakl
 * Date: 28.3.2005
 */
public class VersionNotFoundException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public VersionNotFoundException(String message) {
        super(message);
    }
}
