/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 20:32:24
 */
package cz.abclinuxu.utils.email.forum;

import org.apache.log4j.Logger;

import java.util.*;

import cz.abclinuxu.persistance.SQLTool;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
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
        Persistance persistance = PersistanceFactory.getPersistance();
        User user;

        log.info("Loading list of users, that subscribed email gate to forum.");
        List subscribed = sqlTool.findUsersWithForumByEmail(null);
        for ( Iterator iter = subscribed.iterator(); iter.hasNext(); ) {
            Integer id = (Integer) iter.next();
            user = (User) persistance.findById(new User(id.intValue()));
            if (log.isDebugEnabled())
                log.debug("Inserting user "+id+" "+user.getName());
            users.put(id, new Subscription(id, user.getEmail()));
        }
        log.info("done");
    }
}
