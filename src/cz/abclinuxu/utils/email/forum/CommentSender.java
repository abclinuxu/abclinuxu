/*
 * User: literakl
 * Date: 31.1.2004
 * Time: 20:25:21
 */
package cz.abclinuxu.utils.email.forum;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.email.EmailSender;

import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class CommentSender extends Thread implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommentSender.class);

    /** how long to sleep in seconds, if there are no actions to be performed */
    public static final String PREF_SLEEP_INTERVAL = "sleep";

    static CommentSender singleton;

    static {
        singleton = new CommentSender();
    }

    int waitInterval;


    private CommentSender() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
        setName("Forum CommentSender");
    }

    public static CommentSender getInstance() {
        return singleton;
    }

    public void run() {
        log.info("starting");
        Comment comment;
        List subscriptions, users;
        Map env;

        while(true) {
            try {
                while ( ForumPool.isEmpty() ) {
                    try { sleep(waitInterval); } catch (InterruptedException e) { log.error("interrupted!"); }
                }
                comment = ForumPool.removeFirst();

                if ( log.isDebugEnabled() )
                    log.debug("Processing comment ["+comment.relationId+","+comment.discussionId+","+comment.threadId+"]");

                subscriptions = SubscribedUsers.getInstance().getSubscriptions();
                if (subscriptions.size()>0) {
                    users = extractIdsFromSubscriptions(subscriptions);
                    env = CommentDecorator.getEnvironment(comment);
                    EmailSender.sendEmailToUsers(env, users);
                }
                subscriptions = null; users = null; env = null; comment = null;
            } catch (Exception e) {
                log.error("Email interface to Forum failed, recovering ..",e);
            }
        }
    }

    /**
     * Extract integer user ids from list of Subscriptions.
     */
    private List extractIdsFromSubscriptions(List subscriptions) {
        List keys = new ArrayList(subscriptions.size());
        Subscription subscription;
        for ( Iterator iter = subscriptions.iterator(); iter.hasNext(); ) {
            subscription = (Subscription) iter.next();
            keys.add(subscription.getId());
        }
        return keys;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        waitInterval = prefs.getInt(PREF_SLEEP_INTERVAL, 60)*1000;
    }
}
