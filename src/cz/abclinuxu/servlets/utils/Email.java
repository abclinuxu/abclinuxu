/*
 * User: literakl
 * Date: Jan 13, 2002
 * Time: 8:41:38 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Properties;
import java.util.Map;

/**
 * Helper class for sending emails.
 */
public class Email {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Email.class);

    /**
     * Sends email to user.
     * @param from sender information
     * @param to recepient information
     * @param subject subject of the email
     * @param content message itself
     * @return true, if message has been sent successfully
     */
    public static boolean sendEmail(String from, String to, String subject, String content) {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Sending email. To: "+to+", From: "+from+", Subject: "+subject+"\n"+content);
            }

            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props,null);
            session.setDebug(false);

            MimeMessage message = new MimeMessage(session);
            message.setSubject(subject);
            message.setText(content);

            Address toAddress = new InternetAddress(to);
            Address fromAddress = new InternetAddress(from);

            message.setFrom(fromAddress);
            message.setRecipient(Message.RecipientType.TO,toAddress);

            Transport transport = session.getTransport("smtp");
            transport.connect("localhost",null,null);
            message.saveChanges();
            transport.sendMessage(message,message.getAllRecipients());

            if ( log.isInfoEnabled() ) {
                log.info("Email sent. To: "+to+", From: "+from+", Subject: "+subject);
            }
            return true;
        } catch (MessagingException e) {
            log.error("Cannot send email to user "+to,e);
            return false;
        }
    }

    /**
     * Sends bulk email.
     * @param from sender information
     * @param subject subject of the email
     * @param data Map holding recepients/content. Recepient's email is a key for hist content.
     * @return number of successfully sent emails.
     */
    public static int sendBulkEmail(String from, String subject, Map data) {
        return 0;
    }
}
