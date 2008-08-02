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

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Item;

import java.util.prefs.Preferences;
import java.util.Map;
import java.util.List;

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
    static Decorator blogDecorator = new BlogDecorator();
    static Decorator swDecorator = new SoftwareDecorator();
    static Decorator hwDecorator = new HardwareDecorator();
    static Decorator persDecorator = new PersonalityDecorator();
    static Decorator dictDecorator = new DictionaryDecorator();
    static Decorator articleDecorator = new ArticleDecorator();

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
                    log.info("Processing action "+action.getAction()+" on "+action.getType()+" "+action.getRelation());

                Map env = chooseDecorator(action).getEnvironment(action);
                List<Integer> users = action.getRecipients();
                if (users.size()>0)
                    EmailSender.sendEmailToUsers(env,users);
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
        if (ObjectType.DICTIONARY.equals(action.type) )
            return dictDecorator;
        if (ObjectType.SOFTWARE.equals(action.type) )
            return swDecorator;
        if (ObjectType.HARDWARE.equals(action.type) )
            return hwDecorator;
        if (ObjectType.PERSONALITY.equals(action.type) )
            return persDecorator;
        if (ObjectType.DISCUSSION.equals(action.type) )
            return discussionDecorator;
        if (ObjectType.ITEM.equals(action.type) || ObjectType.CONTENT.equals(action.type))
            return itemDecorator;
        if (ObjectType.FAQ.equals(action.type) )
            return faqDecorator;
        if (ObjectType.BLOG.equals(action.type) )
            return blogDecorator;
        if (ObjectType.ARTICLE.equals(action.type))
            return articleDecorator;
        return null;
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        waitInterval = prefs.getInt(PREF_SLEEP_INTERVAL,60) * 1000;
    }
}
