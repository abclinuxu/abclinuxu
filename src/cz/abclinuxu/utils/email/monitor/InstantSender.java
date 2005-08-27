/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:10:43
 */
package cz.abclinuxu.utils.email.monitor;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.email.EmailSender;

import java.util.prefs.Preferences;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Element;

/**
 * InstantSender is used to process MonitorActions. E.g. to find
 * recepients, format their notifications and send emails.
 */
public class InstantSender extends Thread implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstantSender.class);

    /** how long to sleep in seconds, if there are no actions to be performed */
    public static final String PREF_SLEEP_INTERVAL = "sleep";

    static InstantSender singleton = new InstantSender();
    static Decorator discussionDecorator = new DiscussionDecorator();
    static Decorator faqDecorator = new FaqDecorator();
    static Decorator driverDecorator = new DriverDecorator();
    static Decorator itemDecorator = new ItemDecorator();

    int waitInterval;

    private InstantSender() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
        setName("InstantSender");
    }

    /**
     * Gives access to singleton.
     */
    public static InstantSender getInstance() {
        return singleton;
    }

    public void run() {
        log.info("starting");
        MonitorPool pool = MonitorPool.getInstance();
        MonitorAction action = null;

        while (true) {
            try {
                while ( pool.isEmpty()) {
                    try { sleep(waitInterval); } catch (InterruptedException e) { log.error("interrupted!");}
                }
                action = pool.getFirst();

                if (log.isDebugEnabled())
                    log.debug("Processing action "+action.getAction()+" on "+action.getType()+" "+action.getObject());

                Map env = chooseDecorator(action).getEnvironment(action);
                List users = getRecepients(action);
                if (users.size()>0)
                    EmailSender.sendEmailToUsers(env,users);
                action = null;
            } catch (Exception e) {
                log.error("Unknown exception!", e);
            }
        }
    }

    /**
     * Chooses decorator based on ObjectType attribute of action.
     * @return Decorator
     */
    private Decorator chooseDecorator(MonitorAction action) {
        if (ObjectType.DRIVER.equals(action.type) )
            return driverDecorator;
        if (ObjectType.DISCUSSION.equals(action.type) )
            return discussionDecorator;
        if (ObjectType.ITEM.equals(action.type) || ObjectType.CONTENT.equals(action.type))
            return itemDecorator;
        if (ObjectType.FAQ.equals(action.type) )
            return faqDecorator;
        return null;
    }

    /**
     * Finds users, that wish to be informed about this action.
     * User, who performed this action, will be skipped.
     * @return List of Integers - keys of users
     */
    private List getRecepients(MonitorAction action) {
        Element monitor = action.getMonitor();
        List keys = monitor.elements("id");
        List users = new ArrayList(keys.size());
        Integer actor = action.getActorId();

        for ( Iterator iter = keys.iterator(); iter.hasNext(); ) {
            Element id = (Element) iter.next();
            try {
                Integer key = Integer.valueOf(id.getTextTrim());
                if ( !actor.equals(key) )
                    users.add(key);
            } catch (NumberFormatException e) {
                log.error("Error in XML in object "+action.getObject(), e);
            }
        }
        return users;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        waitInterval = prefs.getInt(PREF_SLEEP_INTERVAL,60) * 1000;
    }
}
