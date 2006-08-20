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
package cz.abclinuxu.utils.email.forum;

import org.apache.log4j.Logger;

import java.util.*;

import cz.abclinuxu.persistence.SQLTool;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.data.User;

/**
 * Holder of subscribed users.
 */
public final class SubscribedUsers {
    static Logger log = Logger.getLogger(SubscribedUsers.class);

    static SubscribedUsers singleton;
    static {
        singleton = new SubscribedUsers();
    }

    HashMap users;

    private SubscribedUsers() {
        users = new HashMap();
        init();
    }

    public static SubscribedUsers getInstance() {
        return singleton;
    }

    /**
     * Adds new user to list of subscribed users.
     * @param id Primary key of the user.
     * @param email Email address to be used.
     * todo call this, when email is activated and forum is subscribed
     */
    public synchronized void addUser(int id, String email) {
        Integer key = new Integer(id);
        users.put(key, new Subscription(key,email));
    }

    /**
     * Remove user from the list of subscribed users.
     * @param id Primary key of the user.
     * todo call this, when email is deactived
     */
    public synchronized void removeUser(int id) {
        Integer key = new Integer(id);
        users.remove(key);
    }

    /**
     * If user is subscribed, than replace his email with new value.
     * @param id Primary key of the user.
     * @param email New email address to be used.
     * todo call it, when someone changes his email
     */
    public synchronized void replaceEmail(int id, String email) {
        Subscription subscription = (Subscription) users.get(new Integer(id));
        if (subscription!=null)
            subscription.setEmail(email);
    }

    /**
     * @return currently available Subscriptions.
     */
    public synchronized List getSubscriptions() {
        return new ArrayList(users.values());
    }

    /**
     * Loads subscribed users from database.
     */
    private void init() {
        SQLTool sqlTool = SQLTool.getInstance();
        Persistence persistence = PersistenceFactory.getPersistance();
        User user;

        log.info("Loading list of users, that subscribed email gate to forum.");
        List subscribed = sqlTool.findUsersWithForumByEmail(null);
        for ( Iterator iter = subscribed.iterator(); iter.hasNext(); ) {
            Integer id = (Integer) iter.next();
            user = (User) persistence.findById(new User(id.intValue()));
            if (log.isDebugEnabled())
                log.debug("Inserting user "+id+" "+user.getName());
            users.put(id, new Subscription(id, user.getEmail()));
        }
        log.info("done");
    }
}
