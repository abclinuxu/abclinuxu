/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 4:37:42 PM
 */
package cz.abclinuxu.utils.config;

/**
 * Thrown, when cinfiguration cannot proceed (missing properties etc).
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
