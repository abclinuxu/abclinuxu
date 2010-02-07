package cz.abclinuxu.utils.email;

import javax.mail.Session;
import javax.mail.Transport;

/**
 * Encapsulates single session for sending bulk emails.
 * User: literakl
 * Date: 6.2.2010
 */
public class MailSession {
    Session session;
    Transport transport;

    public MailSession(Session session, Transport transport) {
        this.session = session;
        this.transport = transport;
    }

    public Session getSession() {
        return session;
    }

    public Transport getTransport() {
        return transport;
    }
}
