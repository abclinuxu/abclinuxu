/*
 * User: literakl
 * Date: 13.9.2003
 * Time: 21:29:10
 */
package cz.abclinuxu.security;

import cz.abclinuxu.data.User;

/**
 * Used to track administrator's actions.
 */
public class AdminLogger {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AdminLogger.class);

    /**
     * Used to log administrator's actions.
     * @param user admin name, who has made the change
     * @param message description of his activity
     */
    public static void logEvent(User user, String message) {
        log.info(user.getName()+" | "+message);
    }
}
