/*
 * User: literakl
 * Date: 7.11.2003
 * Time: 8:13:25
 */
package cz.abclinuxu.utils.monitor;

import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.data.Item;
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
public class ItemDecorator implements Decorator, Configurable {
    public static final String PREF_ACTION_ADD = "action.add";
    public static final String PREF_ACTION_EDIT = "action.edit";
    public static final String PREF_ACTION_REMOVE = "action.remove";

    String subject, actionAdd, actionEdit, actionRemove;

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
        env.put(EmailSender.KEY_TEMPLATE, "/mail/monitor/notif_item.ftl");

        if ( action.url!=null )
            env.put(VAR_URL, action.url);
        env.put(VAR_ACTOR, action.actor);
        env.put(VAR_PERFORMED, action.performed);

        String changeMessage = "";
        if ( UserAction.ADD.equals(action.action) )
            changeMessage = actionAdd;
        else if ( UserAction.EDIT.equals(action.action) )
            changeMessage = actionEdit;
        else if ( UserAction.REMOVE.equals(action.action) )
            changeMessage = actionRemove;
        env.put(VAR_ACTION, changeMessage);

        String name = (String) action.getProperty(PROPERTY_NAME);
        if (name==null) {
            Persistance persistance = PersistanceFactory.getPersistance();
            Item driver = (Item) persistance.findById(action.object);
            name = driver.getData().selectSingleNode("/data/name").getText();
        }
        env.put(VAR_NAME,name);

        return env;
    }

    public ItemDecorator() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT,"AbcMonitor");
        actionAdd = prefs.get(PREF_ACTION_ADD, "");
        actionEdit = prefs.get(PREF_ACTION_EDIT, "");
        actionRemove = prefs.get(PREF_ACTION_REMOVE, "");
    }
}
