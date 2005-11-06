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

import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.mail.MessagingException;

/**
 * This MimeMessage subclass preserves Message-ID.
 */
public class AbcEmail extends MimeMessage {
    public static final String MESSAGE_ID = "Message-ID";
    public static final String REFERENCES = "References";

    String messageId;
    String references;

    public AbcEmail(Session session) {
        super(session);
    }

    /**
     * Set Message-ID for the email. You dont need to surround it with braces,
     * it is done automatically.
     * @param messageId
     * todo this can be array too
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Set References for the email. You dont need to surround it with braces,
     * it is done automatically.
     * @param references
     */
    public void setReferences(String references) {
        this.references = references;
    }

    protected void updateHeaders() throws MessagingException {
        super.updateHeaders();
        if (messageId!=null)
            setHeader(MESSAGE_ID, "<"+messageId+">");
        if (references!=null)
            setHeader(REFERENCES, "<"+references+">");
    }
}
