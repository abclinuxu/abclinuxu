/*
 * User: literakl
 * Date: 4.2.2004
 * Time: 18:44:11
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
