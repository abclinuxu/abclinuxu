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

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.*;
import java.util.prefs.Preferences;
import java.io.IOException;

import freemarker.template.TemplateException;

/**
 * Helper class for sending emails.
 */
public class EmailSender implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmailSender.class);

    /** preferences key for SMTP server, we shall used to send emails */
    public static final String PREF_SMTP_SERVER = "smtp.server";
    public static final String DEFAULT_SMTP_SERVER = "localhost";

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

    static String smtpServer;

    /**
     * Sends an email. Params shall hold neccessary atributes like
     * KEY_FROM, KEY_TO, KEY_SUBJECT and KEY_BODY or KEY_TEMPLATE.
     * @return true, when message has been successfully sent.
     */
    public static boolean sendEmail(Map params) {
        Properties props = new Properties();
        String from = (String) params.get(KEY_FROM), to = (String) params.get(KEY_TO);
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
        map.put(KEY_FROM,from); map.put(KEY_TO,to);
        map.put(KEY_SUBJECT,subject); map.put(KEY_BODY,content);
        return sendEmail(map);
    }

    /**
     * Sends bulk email.
     * @param from sender information
     * @param subject subject of the email
     * @param data Map holding recepients/content. Recepient's email is a key for his content.
     * @return number of successfully sent emails.
     */
    public static int sendBulkEmail(String from, String subject, Map data) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props,null);
        session.setDebug(false);
        Transport transport = null;
        MimeMessage message = null;

        try {
            transport = session.getTransport("smtp");
            transport.connect(smtpServer,null,null);

            message = new MimeMessage(session);
            Address fromAddress = new InternetAddress(from);
            message.setFrom(fromAddress);
            message.setSubject(subject);
        } catch (MessagingException e) {
            log.error("Cannot contact SMTP server!",e);
            return 0;
        }

        Set keys = data.keySet();
        int i = 0;
        for (Iterator iter = keys.iterator(); iter.hasNext();i++) {
            String to = (String) iter.next();
            String content = (String) data.get(to);
            try {
                message.setText(content);
                Address toAddress = new InternetAddress(to);
                message.setRecipient(Message.RecipientType.TO,toAddress);
                message.saveChanges();

                transport.sendMessage(message,message.getAllRecipients());

                if ( log.isDebugEnabled() ) log.debug("Email sent from "+from+" to "+to+".");
            } catch (MessagingException e) {
                log.error("Cannot send email to "+to,e);
                i--;
            }
        }
        return i;
    }

    /**
     * Finds content of email in params.
     */
    static String getEmailBody(Map params) throws MissingArgumentException, NotFoundException {
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
        smtpServer = prefs.get(PREF_SMTP_SERVER,DEFAULT_SMTP_SERVER);
    }
}
