/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.data.Item;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 *
 * @author lubos
 */
public class ArticleDecorator implements Decorator, Configurable {
    public static final String VAR_PEREX = "PEREX";
    String subject;
    
    public ArticleDecorator() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }
    
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
        env.put(EmailSender.KEY_TEMPLATE, "/mail/monitor/notif_article.ftl");

        if ( action.url!=null )
            env.put(VAR_URL, action.url);
        env.put(VAR_PERFORMED, action.performed);
        env.put(EmailSender.KEY_SENT_DATE, action.performed);

        Persistence persistence = PersistenceFactory.getPersistence();
        Item article = (Item) persistence.findById(action.relation.getChild());
            
        String name = (String) action.getProperty(PROPERTY_NAME);
        if ( name==null )
            name = article.getTitle();
        
        env.put(VAR_PEREX, Tools.xpath(article, "/data/perex"));
        env.put(VAR_NAME, name);

        return env;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        subject = prefs.get(PREF_SUBJECT,"AbcMonitor");
    }
}
