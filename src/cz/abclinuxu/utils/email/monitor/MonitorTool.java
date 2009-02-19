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
package cz.abclinuxu.utils.email.monitor;

import cz.abclinuxu.data.User;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.persistence.cache.MonitorCache;
import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;

import java.util.Set;
import java.util.Collections;

/**
 * Utility class for manipulation with monitors.
 */
public class MonitorTool {
    private static MonitorCache cache = MonitorCache.getInstance();
    private static SQLTool sqlTool = SQLTool.getInstance();

    /**
     * Puts monitor on given document for selected user. Changes are persisted.
     * @param document Item or Category
     * @param user User, which wants to change his monitor settings.
     */
    public static void startMonitor(GenericDataObject document, User user) {
        Set<Integer> users = get(document);
        if (users.contains(new Integer(user.getId())))
            return;

        sqlTool.insertMonitor(document, user);
        cache.put(document, user.getId());

        MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getPersistence();
        document.setMonitorCount(document.getMonitorCount() + 1);
        persistence.storeInCache(document);
    }

    /**
     * Removes monitor on given document from selected user. Changes are persisted.
     * @param document Item or Category
     * @param user User, which wants to change his monitor settings.
     */
    public static void stopMonitor(GenericDataObject document, User user) {
        Set<Integer> users = get(document);
        if (! users.contains(new Integer(user.getId())))
            return;

        sqlTool.removeMonitor(document, user);
        if (cache.remove(document, user.getId())) {
            int monitors = document.getMonitorCount();
            if (monitors > 0) {
                MySqlPersistence persistence = (MySqlPersistence) PersistenceFactory.getPersistence();
                document.setMonitorCount(monitors - 1);
                persistence.storeInCache(document);
            }
        }
    }

    /**
     * Removes all monitors for given user. The cache of mappings is cleared, so user will not receive
     * any further notification. Number of monitors per document is not updated, so it may be wise
     * to clear persistence cache too.
     * @param user
     */
    public static void removeAllMonitors(User user) {
        sqlTool.removeAllMonitors(user);
        cache.clear();
    }

    /**
     * Resets cache.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Gets set of users (id) monitoring requested document. Returns empty set if nobody is monitoring this document.
     * @param doc monitored document, it must be initialized
     * @return set of user id
     */
    public static Set<Integer> get(GenericDataObject doc) {
        if (doc.getMonitorCount() == 0)
            return Collections.emptySet();

        Set<Integer> uids = cache.get(doc);
        if (uids == null) {
            uids = sqlTool.getMonitors(doc);
            cache.put(doc, uids);
        }
        return Collections.unmodifiableSet(uids);
    }
}
