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
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.scheduler.UpdateStatistics;
import cz.finesoft.socd.analyzer.DiacriticRemover;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
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

    static String smtpServer, defaultFrom;
    static boolean debugSMTP;
    private static final UpdateStatistics stats = UpdateStatistics.getInstance();

    /**
     * Sends an email. Params shall hold neccessary atributes like
     * KEY_FROM, KEY_TO, KEY_SUBJECT and KEY_BODY or KEY_TEMPLATE.
     * @return true, when message has been successfully sent.
     */
    public static boolean sendEmail(Map params) {
        Properties props = new Properties();
        String from = (String) params.get(KEY_FROM);
        String senderName = (String) params.get(KEY_SENDER_NAME);
        String to = (String) params.get(KEY_TO);
        String cc = (String) params.get(KEY_CC);
        String bcc = (String) params.get(KEY_BCC);
        if ( from==null || from.length()==0 )
            from = defaultFrom;

        Session session = Session.getDefaultInstance(props,null);
        session.setDebug(debugSMTP);

        try {
            AbcEmail message = new AbcEmail(session);
            message.setSubject((String) params.get(KEY_SUBJECT));
            if (senderName!=null) {
                senderName = DiacriticRemover.getInstance().removeDiacritics(senderName);
                message.setFrom(new InternetAddress(from, senderName));
            } else
                message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            if (cc!=null)
                message.setRecipient(Message.RecipientType.CC,new InternetAddress(cc));
            if (bcc!=null)
                message.setRecipient(Message.RecipientType.BCC,new InternetAddress(bcc));
            message.setText(getEmailBody(params));
            Date sentDate = getSentDate(params);
            message.setSentDate(sentDate);
            message.setMessageId((String) params.get(KEY_MESSAGE_ID));
            message.setReferences((String) params.get(KEY_REFERENCES));

            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer,null,null);
            message.saveChanges();
            transport.sendMessage(message,message.getAllRecipients());
            transport.close();

            if ( log.isDebugEnabled() )
                log.debug("Email sent from "+from+" to "+to+".");

            String statsKey = (String) params.get(KEY_STATS_KEY);
            if (statsKey == null)
                statsKey = Constants.EMAIL_UNKNOWN;
            stats.recordView(statsKey, 1);

            return true;
        } catch (MessagingException e) {
            log.error("Cannot send email sent from "+from+" to "+to+".",e);
            return false;
        } catch (UnsupportedEncodingException e) {
            log.debug("Setting sender name failed on '"+senderName+"'", e);
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
        Map map = new HashMap(4);
        map.put(KEY_FROM,from);
        map.put(KEY_TO,to);
        map.put(KEY_SUBJECT,subject);
        map.put(KEY_BODY,content);
        return sendEmail(map);
    }

    /**
     * Sends email to users from given list. Users are taken sequentally from database
     * and inserted into map defaults under key Constants.VAR_USER.
     * @param params map with parameters
     * @param users list of Integers - ids of users.
     * @return number of sent emails.
     */
    public synchronized static int sendEmailToUsers(Map params, List users) {
        if ( users.size()==0 )
            return 0;

        Persistence persistence = PersistenceFactory.getPersistence();
        String subject = (String) params.get(KEY_SUBJECT);
        Address sender = null;
        String senderName = (String) params.get(KEY_SENDER_NAME);
        Object from = params.get(KEY_FROM);
        if (from==null )
            from = defaultFrom;

        Session session = Session.getDefaultInstance(new Properties(), null);
        session.setDebug(debugSMTP);

        int count = 0, total = users.size();
        User user = new User();

        if (log.isDebugEnabled()) {
            log.debug("Sending email to " + total + " users.");
            log.debug("Email header: from="+from+", subject="+subject);
        }

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer, null, null);

            AbcEmail message = new AbcEmail(session);
            message.setSubject(subject);

            if (from instanceof Address)
                sender = (Address) from;
            else if(senderName != null) {
                senderName = DiacriticRemover.getInstance().removeDiacritics(senderName);
                message.setFrom(new InternetAddress((String)from, senderName));
            } else
                sender = new InternetAddress((String) from);
            message.setFrom(sender);

            message.setMessageId((String) params.get(KEY_MESSAGE_ID));
            message.setReferences((String) params.get(KEY_REFERENCES));

            for ( Iterator iter = users.iterator(); iter.hasNext(); ) {
                try {
                    user.setId(((Integer) iter.next()).intValue());
                    user = (User) persistence.findById(user);

                    // check, that user has valid email
                    Element tagEmail = (Element) user.getData().selectSingleNode("/data/communication/email");
                    if ( ! "yes".equals(tagEmail.attribute("valid").getText()) ) {
                        log.debug("Skipping user "+user.getId()+", his email is set as invalid.");
                        continue;
                    }

                    params.put(Constants.VAR_USER, user);
                    String to = user.getEmail();

                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    message.setText(getEmailBody(params));
                    Date sentDate = getSentDate(params);
                    message.setSentDate(sentDate);
                    message.saveChanges();

                    transport.sendMessage(message, message.getAllRecipients());
                    count++;
                    if ( log.isDebugEnabled() )
                        log.debug("Email "+count+" of "+total+" sent to "+to);
                } catch (Exception e) {
                    log.warn("Cannot send email to user "+user.getId()+", TO="+user.getEmail(),e);
                }
            }
            transport.close();

            String statsKey = (String) params.get(KEY_STATS_KEY);
            if (statsKey == null)
                statsKey = Constants.EMAIL_UNKNOWN;
            stats.recordView(statsKey, count);
        } catch (MessagingException e) {
            log.error("Error - is JavaMail set up correctly?", e);
        } catch (UnsupportedEncodingException e) {
            log.debug("Setting sender name failed on '" + senderName + "'", e);
        }

        return count;
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
        if ( body!=null && body.length()>0 )
            return body;
        String template = (String) params.get(KEY_TEMPLATE);
        try {
            body = FMUtils.executeTemplate(template,params);
        } catch (Exception e) {
            throw new NotFoundException("Nemohu zpracovat šablonu "+template,e);
        }
        if ( body!=null && body.length()>0 )
            return body;
        throw new MissingArgumentException("Email content is missing in map!");
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
