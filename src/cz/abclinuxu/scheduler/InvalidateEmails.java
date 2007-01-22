/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.scheduler;

import org.apache.log4j.Logger;

import java.util.TimerTask;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.prefs.Preferences;

import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.Message;

/**
 * @author literakl
 * @since 21.1.2007
 */
public class InvalidateEmails extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(InvalidateEmails.class);

    public static final String PREF_USER = "mailbox.user";
    public static final String PREF_PASSWORD = "mailbox.password";
    public static final String PREF_SERVER = "server.address";
    public static final String PREF_FOLDER = "mailbox.folder";
    public static final String PREF_SERVER_TYPE = "server.type";
    public static final String PREF_REGEXP_VACATION = "regexp.vacation";
    public static final String PREF_REGEXP_RETRY = "regexp.retry";
    public static final String PREF_DEBUG_MAIL = "debug";

    String server, mailServerType, folderName, user, password;
    Pattern reRetry, reVacation;
    static boolean debugSMTP;

    public InvalidateEmails() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    public void run() {
        try {
            log.debug(getClass().getName() + " starts");
            if (AbcConfig.isMaintainanceMode()) {
                log.debug(getClass().getName() + " finished");
                return;
            }

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(debugSMTP);
            Store store = session.getStore(mailServerType);
            store.connect(server, user, password);

            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);
            Message message[] = folder.getMessages();
            if (message != null) {
                for (int i = 0; i < message.length; i++) {
                    System.out.println(i + ": " + message[i].getFrom()[0] + "\t" + message[i].getSubject());
                    System.out.println(message[i].getContent());
                }
            }

            folder.close(false);
            store.close();

            log.debug(getClass().getName() + " finished");
        } catch (Throwable e) {
            log.error("Invalidated emails failed", e);
        }
    }

    public static void main(String[] args) {
        InvalidateEmails instance = new InvalidateEmails();
        instance.run();
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        server = prefs.get(PREF_SERVER, "");
        mailServerType = prefs.get(PREF_SERVER_TYPE, "IMAP");
        folderName = prefs.get(PREF_FOLDER, "");
        user = prefs.get(PREF_USER, "");
        password = prefs.get(PREF_PASSWORD, "");
        debugSMTP = prefs.getBoolean(PREF_DEBUG_MAIL, false);
        String re = prefs.get(PREF_REGEXP_RETRY, null);
        if (re != null)
            reRetry = Pattern.compile(re);
        re = prefs.get(PREF_REGEXP_VACATION, null);
        if (re != null)
            reVacation = Pattern.compile(re);
    }
}
