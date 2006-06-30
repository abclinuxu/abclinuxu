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

import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.format.HtmlToTextFormatter;

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
        HtmlToTextFormatter formatter = new HtmlToTextFormatter();
        Map env = new HashMap();
        if (action.url!=null)
            env.put(VAR_URL, action.url);
        env.put(VAR_ACTOR, action.actor);
        env.put(VAR_PERFORMED, action.performed);
        env.put(VAR_NAME, action.getProperty(PROPERTY_NAME));

        String text = (String) action.getProperty(PROPERTY_CONTENT);
        if (text != null) {
            text = formatter.format(text);
            env.put(PROPERTY_CONTENT, text);
        }

        String changeMessage = "";
        if (UserAction.ADD.equals(action.action))
            changeMessage = actionAdd;
        else if (UserAction.REMOVE.equals(action.action))
            changeMessage = actionRemove;
        else if (UserAction.CENSORE.equals(action.action))
            changeMessage = actionCensore;

        env.put(VAR_ACTION,changeMessage);
//        env.put(EmailSender.KEY_SENDER_NAME, action.actor);
        env.put(EmailSender.KEY_SENT_DATE, action.performed);
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
