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

import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Decorator for Personality items.
 */
public class PersonalityDecorator implements Decorator, Configurable {
    public static final String PREF_ACTION_ADD = "action.add";
    public static final String PREF_ACTION_EDIT = "action.edit";
    public static final String PREF_ACTION_REMOVE = "action.remove";

    String subject, actionEdit, actionRemove, actionAdd;

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
        env.put(EmailSender.KEY_TEMPLATE, "/mail/monitor/notif_personality.ftl");

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
        else if ( UserAction.ADD.equals(action.action) )
            changeMessage = actionAdd;
        env.put(VAR_ACTION, changeMessage);

        String name = (String) action.getProperty(PROPERTY_NAME);
        if ( name==null ) {
            Persistence persistence = PersistenceFactory.getPersistence();
            Item obj = (Item) persistence.findById(action.relation.getChild());
            name = Tools.childName(obj);
        }
        env.put(VAR_NAME, name);

        return env;
    }

    public PersonalityDecorator() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT,"AbcMonitor");
        actionAdd = prefs.get(PREF_ACTION_ADD, "");
        actionEdit = prefs.get(PREF_ACTION_EDIT, "");
        actionRemove = prefs.get(PREF_ACTION_REMOVE, "");
    }
}
