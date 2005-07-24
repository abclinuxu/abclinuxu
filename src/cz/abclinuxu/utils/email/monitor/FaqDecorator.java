/*
 * User: literakl
 * Date: 7.11.2003
 * Time: 8:13:25
 */
package cz.abclinuxu.utils.email.monitor;

import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.email.monitor.Decorator;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Decorator for Discussions.
 */
public class FaqDecorator implements Decorator, Configurable {
    public static final String PREF_ACTION_EDIT = "action.edit";
    public static final String PREF_ACTION_REMOVE = "action.remove";

    String subject, actionEdit, actionRemove;

    /**
     * Creates environment for given MonitorAction. This
     * environment will be used by template engine to
     * render notification for the user.
     * @param action MonitorAction to be decorated into Map.
     * @return environment
     */
    public Map getEnvironment(MonitorAction action) {
        Map env = new HashMap();

        env.put(EmailSender.KEY_SUBJECT, subject);
        env.put(EmailSender.KEY_TEMPLATE, "/mail/monitor/notif_faq.ftl");

        if ( action.url!=null )
            env.put(VAR_URL, action.url);
        env.put(VAR_ACTOR, action.actor);
        env.put(VAR_PERFORMED, action.performed);
        env.put(EmailSender.KEY_SENT_DATE, action.performed);

        String changeMessage = "";
        if ( UserAction.EDIT.equals(action.action) )
            changeMessage = actionEdit;
        else if ( UserAction.REMOVE.equals(action.action) )
            changeMessage = actionRemove;
        env.put(VAR_ACTION, changeMessage);

        String name = (String) action.getProperty(PROPERTY_NAME);
        if ( name==null ) {
            Persistance persistance = PersistanceFactory.getPersistance();
            Item driver = (Item) persistance.findById(action.object);
            name = driver.getData().selectSingleNode("/data/title").getText();
        }
        env.put(VAR_NAME, name);

        return env;
    }

    public FaqDecorator() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT, "AbcMonitor");
        actionEdit = prefs.get(PREF_ACTION_EDIT, "");
        actionRemove = prefs.get(PREF_ACTION_REMOVE, "");
    }
}