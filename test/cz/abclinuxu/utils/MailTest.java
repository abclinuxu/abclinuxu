/*
 * User: literakl
 * Date: Jan 13, 2002
 * Time: 5:08:07 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.utils;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import java.util.Properties;

public class MailTest {

    public static void main(String[] args) {

        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props,null);
            session.setDebug(true);

            MimeMessage message = new MimeMessage(session);
            message.setSubject("javamail works! ");
            message.setText("wow, it really works! Yabadabadoo");

            Address to = new InternetAddress("literakl@centrum.cz");
            Address from = new InternetAddress("literakl@abclinuxu.cz");

            message.setFrom(from);
            message.setRecipient(Message.RecipientType.TO,to);

            Transport transport = session.getTransport("smtp");
            transport.connect("localhost",null,null);
            message.saveChanges();
            transport.sendMessage(message,message.getAllRecipients());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
