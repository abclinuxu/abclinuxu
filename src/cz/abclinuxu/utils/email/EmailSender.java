/*
 * User: literakl
 * Date: Jan 13, 2002
 * Time: 8:41:38 PM
 * (c)2001-2002 Tinnio
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
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * Helper class for sending emails.
 */
public class EmailSender implements Configurable {
    static Logger log = org.apache.log4j.Logger.getLogger(EmailSender.class);

    /** preferences key for SMTP server, which will send emails */
    public static final String PREF_SMTP_SERVER = "smtp.server";
    /** if from is missing, this will be default sender of emails */
    public static final String PREF_ADMIN_EMAIL_ADDRESS = "admin.address";

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new EmailSender());
    }

    /** FROM attribute of Email */
    public static final String KEY_FROM = "FROM";
    /** TO attribute of Email */
    public static final String KEY_TO = "TO";
    /** SUBJECT attribute of Email */
    public static final String KEY_SUBJECT = "SUBJECT";
    /** content of Email */
    public static final String KEY_BODY = "BODY";
    /** CC attribute of Email */
    public static final String KEY_CC = "CC";
    /** REPLYTO attribute of Email */
    public static final String KEY_REPLYTO = "REPLYTO";
    /** template to be rendered and used as content of Email */
    public static final String KEY_TEMPLATE = "TEMPLATE";

    static String smtpServer, defaultFrom;

    /**
     * Sends an email. Params shall hold neccessary atributes like
     * KEY_FROM, KEY_TO, KEY_SUBJECT and KEY_BODY or KEY_TEMPLATE.
     * @return true, when message has been successfully sent.
     */
    public static boolean sendEmail(Map params) {
        Properties props = new Properties();
        String from = (String) params.get(KEY_FROM), to = (String) params.get(KEY_TO);
        if ( from==null || from.length()==0 )
            from = defaultFrom;

        Session session = Session.getDefaultInstance(props,null);
        session.setDebug(false);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setSubject((String) params.get(KEY_SUBJECT));
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setText(getEmailBody(params));

            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer,null,null);
            message.saveChanges();
            transport.sendMessage(message,message.getAllRecipients());
            transport.close();

            if ( log.isDebugEnabled() ) log.debug("Email sent from "+from+" to "+to+".");
            return true;
        } catch (MessagingException e) {
            log.error("Cannot send email sent from "+from+" to "+to+".",e);
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
    public static int sendEmailToUsers(Map params, List users) {
        if ( users.size()==0 )
            return 0;

        Persistance persistance = PersistanceFactory.getPersistance();
        String from = (String) params.get(KEY_FROM), subject = (String) params.get(KEY_SUBJECT);
        if ( from==null || from.length()==0 )
            from = defaultFrom;

        Session session = Session.getDefaultInstance(new Properties(), null);
        session.setDebug(false);

        int count = 0, total = users.size();
        User user = new User();
        log.info("sendEmailToUsers(): about to send email to "+total+" users.");
        log.info("sendEmailToUsers(): from="+from+", subject="+subject);

        try {
            Transport transport = session.getTransport("smtp");
            transport.connect(smtpServer, null, null);

            MimeMessage message = new MimeMessage(session);
            message.setSubject(subject);
            message.setFrom(new InternetAddress(from));

            for ( Iterator iter = users.iterator(); iter.hasNext(); ) {
                try {
                    user.setId(((Integer) iter.next()).intValue());
                    user = (User) persistance.findById(user);
                    params.put(Constants.VAR_USER, user);
                    String to = user.getEmail();

                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                    message.setText(getEmailBody(params));
                    message.saveChanges();

                    transport.sendMessage(message, message.getAllRecipients());
                    count++;
                    if ( log.isDebugEnabled() ) log.debug("Email "+count+"/"+total+" sent to "+to+".");
                } catch (Exception e) {
                    log.warn("Cannot send email to user "+user.getId()+", TO="+user.getEmail(),e);
                }
            }
            transport.close();
        } catch (MessagingException e) {
            log.error("Error - is JavaMail set up correctly?", e);
        }
        log.info("sendEmailToUsers(): sent "+count+" emails.");

        return count;
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
            throw new NotFoundException("Nemohu zpracovat ¹ablonu "+template,e);
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
    }
}
