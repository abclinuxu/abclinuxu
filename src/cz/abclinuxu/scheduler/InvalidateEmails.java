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

import cz.abclinuxu.utils.Misc;
import org.apache.log4j.Logger;

import java.util.TimerTask;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.prefs.Preferences;
import java.io.IOException;

import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags;

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
    public static final String PREF_REGEXP_ANTISPAM = "regexp.antispam";
    public static final String PREF_DEBUG_MAIL = "debug";

    String server, mailServerType, folderName, user, password;
    Pattern reRetry, reVacation, reAntispam;
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
            folder.open(Folder.READ_WRITE);
            Message messages[] = folder.getMessages();
            if (messages != null) {
                for (int i = 0; i < messages.length; i++) {
                    Message message = messages[i];
                    processMessage(message);
                }
            }

            folder.close(true);
            store.close();

            log.debug(getClass().getName() + " finished");
        } catch (Throwable e) {
            log.error("Invalidated emails failed", e);
        }
    }

    private void processMessage(Message message) throws MessagingException, IOException {
        String subject = message.getSubject();
        if (subject == null)
            subject = "";
        subject = Misc.removeDiacritics(subject);
        if (reVacation.matcher(subject).find()) {
            message.setFlag(Flags.Flag.DELETED, true);
            return;
        }
        if (reRetry.matcher(subject).find()) {
            message.setFlag(Flags.Flag.DELETED, true);
            return;
        }
        if (reAntispam.matcher(subject).find()) {
            message.setFlag(Flags.Flag.DELETED, true);
            return;
        }

        System.out.println(subject);
        System.out.println(message.getContent());
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
        reRetry = Pattern.compile(re, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
        re = prefs.get(PREF_REGEXP_VACATION, null);
        reVacation = Pattern.compile(re, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
        re = prefs.get(PREF_REGEXP_ANTISPAM, null);
        reAntispam = Pattern.compile(re, Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
    }
}
