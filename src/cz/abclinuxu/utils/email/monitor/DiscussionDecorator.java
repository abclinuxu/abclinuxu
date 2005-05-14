/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:13:25
 */
package cz.abclinuxu.utils.email.monitor;

import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Decorator for Discussions.
 */
public class DiscussionDecorator implements Decorator, Configurable {
    public static final String PREF_ACTION_ADD = "action.add";
    public static final String PREF_ACTION_REMOVE = "action.remove";
    public static final String PREF_ACTION_CENSORE = "action.censore";

    public static final String PROPERTY_CONTENT = "CONTENT";

    String subject, actionAdd, actionRemove, actionCensore;

    /**
     * Creates environment for given MonitorAction. This
     * environment will be used by template engine to
     * render notification for the user.
     * @param action MonitorAction to be decorated into Map.
     * @return environment
     */
    public Map getEnvironment(MonitorAction action) {
        Map env = new HashMap();
        if (action.url!=null)
            env.put(VAR_URL, action.url);
        env.put(VAR_ACTOR, action.actor);
        env.put(VAR_PERFORMED, action.performed);
        env.put(EmailSender.KEY_SENT_DATE, action.performed);
        env.put(VAR_NAME, action.getProperty(PROPERTY_NAME));

        String changeMessage = "";
        if (UserAction.ADD.equals(action.action)) {
            changeMessage = actionAdd;
            env.put(PROPERTY_CONTENT, action.getProperty(PROPERTY_CONTENT));
        } else if (UserAction.REMOVE.equals(action.action))
            changeMessage = actionRemove;
        else if (UserAction.CENSORE.equals(action.action))
            changeMessage = actionCensore;

        env.put(VAR_ACTION,changeMessage);
        env.put(EmailSender.KEY_SENDER_NAME, action.actor);
        env.put(EmailSender.KEY_SUBJECT, (String) action.getProperty(PROPERTY_NAME));
        env.put(EmailSender.KEY_TEMPLATE, "/mail/monitor/notif_discussion.ftl");

        return env;
    }

    public DiscussionDecorator() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT, "AbcMonitor");
        actionAdd = prefs.get(PREF_ACTION_ADD, "");
        actionRemove = prefs.get(PREF_ACTION_REMOVE, "");
        actionCensore = prefs.get(PREF_ACTION_CENSORE, "");
    }
}
