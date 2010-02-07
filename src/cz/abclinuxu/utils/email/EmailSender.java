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
package cz.abclinuxu.utils.email;

import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.InternalException;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.User;
import cz.abclinuxu.scheduler.UpdateStatistics;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.InstanceUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.prefs.Preferences;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.dom4j.Element;

/**
 * Helper class for sending emails.
 * todo create variant of sendEmails toUsers, which doesn't read database, but relies on its arguments.
 * todo it shall extract neccessary data (emails) using reflection
 * todo Use it in ForumSender
 */
public class EmailSender implements Configurable {
    static Logger log = org.apache.log4j.Logger.getLogger(EmailSender.class);

    /** preferences key for SMTP server, which will send emails */
    public static final String PREF_SMTP_SERVER = "smtp.server";
    /** if from is missing, this will be default sender of emails */
    public static final String PREF_ADMIN_EMAIL_ADDRESS = "admin.address";
    public static final String PREF_DEBUG_SMTP = "debug.smtp";

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureAndRememberMe(new EmailSender());
    }

    /** FROM attribute of Email */
    public static final String KEY_FROM = "FROM";
    /** Name of sender */
    public static final String KEY_SENDER_NAME = "SENDER";
    /** TO attribute of Email */
    public static final String KEY_TO = "TO";
    /** SUBJECT attribute of Email */
    public static final String KEY_SUBJECT = "SUBJECT";
    /** content of Email */
    public static final String KEY_BODY = "BODY";
    /** CC attribute of Email */
    public static final String KEY_CC = "CC";
    /** BCC attribute of Email */
    public static final String KEY_BCC = "BCC";
    /** REPLYTO attribute of Email */
    public static final String KEY_REPLYTO = "REPLYTO";
    /** template to be rendered and used as content of Email */
    public static final String KEY_TEMPLATE = "TEMPLATE";
    /** message header sent date */
    public static final String KEY_SENT_DATE = "SENT";
    /** message header message-id */
    public static final String KEY_MESSAGE_ID = "MESSAGE_ID";
    /** message header references */
    public static final String KEY_REFERENCES = "REFERENCES";
    /** the key to statistics for thsi kind of email */
    public static final String KEY_STATS_KEY = "STATS_KEY";
    /** the uid of the recepient */
    public static final String KEY_RECEPIENT_UID = "RECEPIENT";
    /** SMTP header with id of the recepient */
    public static final String HEADER_ABC_RECEPIENT = "X-ABC-Recepient";

    static String smtpServer, defaultFrom;
    static boolean debugSMTP;
    private static final UpdateStatistics stats = UpdateStatistics.getInstance();

    /**
     * Opens new mail session and SMTP transport. The caller is responsible for closing this session.
     * @param properties optional
     * @return instance holding the session and the transport
     * @throws InternalException mail session cannot be opened
     */
    public static MailSession openSession() throws InternalException {
        try {
            Session session = Session.getDefaultInstance(new Properties(), null);
            session.setDebug(debugSMTP);
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer, null, null);
            return new MailSession(session, transport);
        } catch (MessagingException e) {
            throw new InternalException("Failed to open email session!", e);
        }
    }

    /**
     * Closes the transport from the mailSession.
     * @param mailSession bean containing the session and the transport to be closed
     * @throws InternalException
     */
    public static void closeSession(MailSession mailSession) throws InternalException {
        try {
            mailSession.getTransport().close();
        } catch (MessagingException e) {
            throw new InternalException("Failed to close email session!", e);
        }
    }

    /**
     * Sends an email. Params shall hold neccessary atributes like
     * KEY_FROM, KEY_TO, KEY_SUBJECT and KEY_BODY or KEY_TEMPLATE.
     * @return true, when message has been successfully sent.
     */
    public static boolean sendEmail(Map params) {
        MailSession mailSession = openSession();
        AbcEmail message = null;
        try {
            message = new AbcEmail(mailSession.getSession());
            setMessageProperties(message, params);
            message.setText(getEmailBody(params));

            message.saveChanges();
            mailSession.getTransport().sendMessage(message,message.getAllRecipients());
            closeSession(mailSession);

            if (log.isDebugEnabled())
                log.debug("Email sent " + message);

            String statsKey = getStatisticsType(params);
            stats.recordView(statsKey, 1);

            return true;
        } catch (MessagingException e) {
            log.error("Cannot send email " + message, e);
            return false;
        }
    }

    /**
     * Sends email to user.
     * @param from sender information
     * @param to recepient information
     * @param subject subject of the email
     * @param content message itself
     * @return true, if message has been sent successfully
     */
    public static boolean sendEmail(String from, String to, String subject, String content) {
        Map map = new HashMap();
        map.put(KEY_FROM, from);
        map.put(KEY_TO, to);
        map.put(KEY_SUBJECT, subject);
        map.put(KEY_BODY, content);
        return sendEmail(map);
    }

    /**
     * Sends email to users from given list. Users are taken sequentally from database
     * and inserted into map defaults under key Constants.VAR_USER.
     * @param params map with parameters
     * @param users list of Integers - ids of users.
     * @return number of sent emails.
     */
    public synchronized static int sendEmailToUsers(Map params, Collection<Integer> users) {
        if (users.isEmpty())
            return 0;

        int count = 0, total = users.size();
        if (log.isDebugEnabled())
            log.debug("Sending email to " + total + " users");

        List<Integer> workingSet;
        MailSession mailSession = openSession();
        try {
            for (int i = 0; i < total;) {
                workingSet = Tools.sublist(users, i, 50);
                i += workingSet.size();
                List<User> userObjects = InstanceUtils.createUsers(workingSet);
                for (User user : userObjects) {
                    try {
                        // check, that user has valid email
                        Element tagEmail = (Element) user.getData().selectSingleNode("/data/communication/email");
                        if (!"yes".equals(tagEmail.attribute("valid").getText())) {
                            log.debug("Skipping user " + user.getId() + ", his email is set as invalid.");
                            continue;
                        }

                        AbcEmail message = new AbcEmail(mailSession.getSession());
                        params.put(KEY_TO, user.getEmail());
                        setMessageProperties(message, params);
                        params.put(Constants.VAR_USER, user);
                        message.setText(getEmailBody(params));

                        message.saveChanges();
                        mailSession.getTransport().sendMessage(message, message.getAllRecipients());
                        count++;
                        if (log.isDebugEnabled())
                            log.debug("Sent email " + count + " / " + total);
                    } catch (Exception e) {
                        log.warn("Cannot send email to user " + user.getId() + ", TO=" + user.getEmail(), e);
                    }
                }
            }

            closeSession(mailSession);

            String statsKey = getStatisticsType(params);
            stats.recordView(statsKey, count);
        } catch (Exception e) {
            log.error("Exception while sending bulk emails", e);
        }

        return count;
    }

    /**
     * Sends email from given list. MailSession is not closed in this call.
     * @param messages prepared messages
     * @param params map with parameters
     * @param mailSession initialized MailSession
     * @return number of sent emails
     */
    public synchronized static int sendEmailToUsers(List<MimeMessage> messages, Map params, MailSession mailSession) {
        if (messages.isEmpty())
            return 0;

        int count = 0, total = messages.size();
        if (log.isDebugEnabled())
            log.debug("Sending " + total + " emails");

        try {
            AbcEmail message = null;
            for (MimeMessage mimeMessage : messages) {
                try {
                    message = new AbcEmail(mimeMessage);
                    setMessageProperties(message, params);

                    message.saveChanges();
                    mailSession.getTransport().sendMessage(message, message.getAllRecipients());
                    count++;
                    if (log.isDebugEnabled())
                        log.debug("Sent email " + count + " / " + total);
                } catch (Exception e) {
                    log.warn("Cannot send email " + message, e);
                }
            }

            String statsKey = getStatisticsType(params);
            stats.recordView(statsKey, count);
        } catch (Exception e) {
            log.error("Exception while sending bulk emails", e);
        }

        return count;
    }

    private static String getStatisticsType(Map params) {
        String statsKey = (String) params.get(KEY_STATS_KEY);
        if (statsKey == null)
            statsKey = Constants.EMAIL_UNKNOWN;
        return statsKey;
    }

    private static void setMessageProperties(AbcEmail message, Map params) throws MessagingException {
        if (message.getFrom() != null) {
            Object from = params.get(KEY_FROM);
            if (from instanceof Address)
                message.setFrom((Address) from);
            else {
                if (from == null || ((String) from).length() == 0)
                    from = defaultFrom;
                String senderName = (String) params.get(KEY_SENDER_NAME);
                if (senderName != null) {
                    senderName = Misc.removeDiacritics(senderName);
                    try {
                        message.setFrom(new InternetAddress((String) from, senderName));
                    } catch (UnsupportedEncodingException e) {
                        log.debug("Setting sender name failed on '" + senderName + "'", e);
                    }
                } else
                    message.setFrom(new InternetAddress((String) from));
            }
        }

        String to = (String) params.get(KEY_TO);
        if (to != null)
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));

        String cc = (String) params.get(KEY_CC);
        if (cc != null)
            message.setRecipient(Message.RecipientType.CC, new InternetAddress(cc));

        String bcc = (String) params.get(KEY_BCC);
        if (bcc != null)
            message.setRecipient(Message.RecipientType.BCC, new InternetAddress(bcc));

        String subject = (String) params.get(KEY_SUBJECT);
        if (subject != null)
            message.setSubject(subject);

        Date date = getSentDate(params);
        if (date != null)
            message.setSentDate(date);

        String messageId = (String) params.get(KEY_MESSAGE_ID);
        if (messageId != null)
            message.setMessageId(messageId);

        String references = (String) params.get(KEY_REFERENCES);
        if (references != null)
            message.setReferences(references);

        String recepient = (String) params.get(KEY_RECEPIENT_UID);
        if (recepient != null)
            message.setHeader(HEADER_ABC_RECEPIENT, recepient);
    }

    /**
     * Date, when this email was (shall have been) sent.
     */
    private static Date getSentDate(Map params) {
        Date sentDate = (Date) params.get(KEY_SENT_DATE);
        return (sentDate!=null)? sentDate : new Date();
    }

    /**
     * Finds content of email in params.
     */
    private static String getEmailBody(Map params) throws MissingArgumentException, NotFoundException {
        String body = (String) params.get(KEY_BODY);
        if (body != null && body.length() > 0)
            return body;

        String template = (String) params.get(KEY_TEMPLATE);
        try {
            body = FMUtils.executeTemplate(template,params);
        } catch (Exception e) {
            throw new NotFoundException("Nemohu zpracovat šablonu " + template, e);
        }

        if (body != null && body.length() > 0)
            return body;
        throw new MissingArgumentException("Email content is missing in map!");
    }

    public static void setSmtpServer(String smtpServer) {
        EmailSender.smtpServer = smtpServer;
    }

    public static String getDefaultFrom() {
        return defaultFrom;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        smtpServer = prefs.get(PREF_SMTP_SERVER, null);
        defaultFrom = prefs.get(PREF_ADMIN_EMAIL_ADDRESS, null);
        debugSMTP = prefs.getBoolean(PREF_DEBUG_SMTP,false);
    }
}
