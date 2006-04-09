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

import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

/**
 * Removes all rows of last seen comments which are beyond user limits.
 */
public class EnsureWatchedDiscussionsLimit extends TimerTask {
    static Logger log = Logger.getLogger(EnsureWatchedDiscussionsLimit.class);

    static EnsureWatchedDiscussionsLimit instance;
    static {
        instance = new EnsureWatchedDiscussionsLimit();
    }

    Set users;

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

        int limit = AbcConfig.getMaxWatchedDiscussionLimit();
        log.info("Cleaning watched discussions for "+usersToClean.size()+" users.");
        for ( Iterator iter = usersToClean.iterator(); iter.hasNext(); ) {
            Integer uid = (Integer) iter.next();
            iter.remove();
            int deleted = sqlTool.deleteOldComments(uid.intValue(), limit);
            log.debug("User "+uid.intValue()+": deleted "+deleted+" watched discussions");
        }
        log.info("Cleaning watched discussions finished");
    }
}
