/*
 * User: literakl
 * Date: 4.2.2004
 * Time: 6:48:26
 */
package cz.abclinuxu.utils;

import cz.abclinuxu.utils.email.AbcEmail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Properties;

/**
 * Tests Message-ID and References.
 */
public class MailId {

    public static void main(String[] args) throws Exception {
        String from = readString("Enter FROM:","literakl@abclinuxu.cz");
        String to = readString("Enter TO:","literakl@localhost");
        String msgId = readString("Enter Message-ID:","ABCDEF.0123456789@abclinuxu.cz");
        String references = readString("Enter References:",null);

        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(true);

            AbcEmail message = new AbcEmail(session);
            message.setSubject("test message id");
            message.setText("text of the message");

            Address toAddr = new InternetAddress(to);
            Address fromAddr = new InternetAddress(from);

            message.setFrom(fromAddr);
            message.setRecipient(Message.RecipientType.TO, toAddr);
            message.setMessageId(msgId);
            if (references!=null)
                message.setReferences(references);

            Transport transport = session.getTransport("smtp");
            transport.connect("localhost", null, null);
            message.saveChanges();
            transport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static String readString(String message, String def) throws Exception {
        System.out.print(message);
        System.out.print(" ["+def+"]: ");
        StringBuffer sb = new StringBuffer();
        int c = System.in.read();
        while (c!='\n') {
            sb.append((char)c);
            c = System.in.read();
        }
        if (sb.length()>0)
            return sb.toString();
        return def;
    }
}
