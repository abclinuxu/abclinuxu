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
public class DriverDecorator implements Decorator, Configurable {
    public static final String PREF_SUBJECT = "subject";

    public static final String VAR_URL = "URL";
    public static final String VAR_NAME = "NAME";
    public static final String VAR_ACTOR = "ACTOR";
    public static final String VAR_PERFORMED = "PERFORMED";

    String subject;

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
        env.put(EmailSender.KEY_TEMPLATE, "/mail/monitor/notif_driver.ftl");

        env.put(VAR_URL,action.getUrl());
        env.put(VAR_ACTOR,action.getActor());
        env.put(VAR_PERFORMED,action.getPerformed());

        Persistance persistance = PersistanceFactory.getPersistance();
        Item driver = (Item) persistance.findById(action.getObject());
        env.put(VAR_NAME,driver.getData().selectSingleNode("/data/name").getText());

        return env;
    }

    public DriverDecorator() {
        ConfigurationManager.getConfigurator().configureMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT,"AbcMonitor");
    }
}
