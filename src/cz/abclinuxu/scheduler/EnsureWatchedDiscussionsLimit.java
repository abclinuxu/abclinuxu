/*
 * User: literakl
 * Date: 17.11.2004
 * Time: 14:09:38
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.persistance.SQLTool;

import java.util.*;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Removes all rows of last seen comments which are beyond user limits.
 */
public class EnsureWatchedDiscussionsLimit extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(EnsureWatchedDiscussionsLimit.class);

    public static final String PREF_WATCHED_DISCUSSION_LIMIT = "watched.discussions.limit";
    static EnsureWatchedDiscussionsLimit instance;
    static {
        instance = new EnsureWatchedDiscussionsLimit();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    Set users;
    int maxWatchedDiscussions;

    public static EnsureWatchedDiscussionsLimit getInstance() {
        return instance;
    }

    /**
     * Schedules check of limits for specified user.
     */
    public static void checkLimits(int userId) {
        instance.users.add(new Integer(userId));
    }

    public EnsureWatchedDiscussionsLimit() {
        users = Collections.synchronizedSet(new HashSet());
    }

    public void run() {
        SQLTool sqlTool = SQLTool.getInstance();
        Set usersToClean;
        synchronized(users) {
            usersToClean = users;
            users = Collections.synchronizedSet(new HashSet());
        }

        log.info("Cleaning watched discussions for "+usersToClean.size()+" users.");
        for ( Iterator iter = usersToClean.iterator(); iter.hasNext(); ) {
            Integer uid = (Integer) iter.next();
            iter.remove();
            int deleted = sqlTool.deleteOldComments(uid.intValue(), maxWatchedDiscussions);
            log.debug("User "+uid.intValue()+": deleted "+deleted+" watched discussions");
        }
        log.info("Cleaning watched discussions finished");
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        maxWatchedDiscussions = prefs.getInt(PREF_WATCHED_DISCUSSION_LIMIT, 50);
    }
}
