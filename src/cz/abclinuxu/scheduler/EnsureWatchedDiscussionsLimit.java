/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
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
