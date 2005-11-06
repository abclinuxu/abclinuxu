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
