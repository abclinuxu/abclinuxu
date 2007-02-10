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
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.User;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.email.EmailSender;
import cz.abclinuxu.utils.Misc;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Servlet used to send emails. It can be configured by session.
 */
public class SendEmail implements AbcAction {
    static Logger log = Logger.getLogger(SendEmail.class);

    public static final String PREFIX = "EMAIL_";

    public static final String VAR_KOD = "KOD";

    public static final String PARAM_SENDER = "sender";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_MESSAGE = "message";
    public static final String PARAM_CC = "cc";
    public static final String PARAM_BCC = "bcc";
    public static final String PARAM_URL = "url";
    public static final String PARAM_DISABLE_CODE = "disableCode";

    public static final String ACTION_SEND = "finish";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_SEND.equals(action) )
            return handleSendEmail2(request, response, env);

        return handleSendEmail(request, env);
    }

    /**
     * shows send email form
     */
    protected String handleSendEmail(HttpServletRequest request, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        User user = (User) env.get(Constants.VAR_USER);
        if ( user!=null ) {
            params.put(PARAM_SENDER, user.getEmail());
        }

        Integer kod = new Integer(new Random().nextInt(10000));
        request.getSession().setAttribute(VAR_KOD, kod);
        env.put(VAR_KOD, kod);

        HttpSession session = request.getSession();
        for ( Enumeration e = session.getAttributeNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            if ( name.startsWith(PREFIX) ) {
                String name2 = name.substring(PREFIX.length());
                if ( EmailSender.KEY_CC.equals(name2)) {
                    params.put(PARAM_CC, session.getAttribute(name));
                    session.removeAttribute(name);

                } else if ( EmailSender.KEY_BCC.equals(name2)) {
                    params.put(PARAM_BCC, session.getAttribute(name));
                    session.removeAttribute(name);

                } else if ( EmailSender.KEY_SUBJECT.equals(name2)) {
                    params.put(PARAM_SUBJECT, session.getAttribute(name));
                    session.removeAttribute(name);

                } else if ( SendEmail.PARAM_DISABLE_CODE.equals(name2)) {
                    params.put(VAR_KOD, kod);
                    session.removeAttribute(name);
                }
            }
        }

        return FMTemplateSelector.select("SendEmail", "show", env, request);
    }

    /**
     * sends email
     */
    protected String handleSendEmail2(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        HttpSession session = request.getSession();

        boolean chyba = false;

        String kod = (String) params.get(VAR_KOD);
        try {
            Integer ulozenyKod = (Integer) session.getAttribute(VAR_KOD);
            env.put(VAR_KOD, ulozenyKod);
            Integer nalezenyKod = Integer.valueOf(kod);
            if ( !nalezenyKod.equals(ulozenyKod) ) {
                ServletUtils.addError(VAR_KOD, "Vyplňte správný kód!", env, null);
                chyba = true;
            }
        } catch (Exception e) {
            ServletUtils.addError(VAR_KOD, "Vyplňte správný kód!", env, null);
            chyba = true;
        }
        String from = (String) params.get(PARAM_SENDER);
        if ( from==null || from.length()<6 || from.indexOf('@')==-1 || from.indexOf('.')==-1 ) {
            ServletUtils.addError(PARAM_SENDER, "Zadejte platnou adresu!", env, null);
            chyba = true;
        }
        String cc = (String) params.get(PARAM_CC);
        if ( !Misc.empty(cc) && (cc.length()<6 || cc.indexOf('@')==-1 || cc.indexOf('.')==-1 )) {
            ServletUtils.addError(PARAM_CC, "Zadejte platnou adresu!", env, null);
            chyba = true;
        }
        String bcc = (String) params.get(PARAM_BCC);
        if ( !Misc.empty(bcc) && (bcc.length()<6 || bcc.indexOf('@')==-1 || bcc.indexOf('.')==-1 )) {
            ServletUtils.addError(PARAM_BCC, "Zadejte platnou adresu!", env, null);
            chyba = true;
        }
        String subject = (String) params.get(PARAM_SUBJECT);
        if ( subject==null || subject.length()==0 ) {
            ServletUtils.addError(PARAM_SUBJECT, "Zadejte předmět!", env, null);
            chyba = true;
        }
        String message = (String) params.get(PARAM_MESSAGE);
        if ( message==null || message.length()==0 ) {
            ServletUtils.addError(PARAM_MESSAGE, "Zadejte zprávu!", env, null);
            chyba = true;
        }
        if ( chyba )
            return FMTemplateSelector.select("SendEmail", "show", env, request);

        Map data = new HashMap();
        data.put(EmailSender.KEY_FROM, from);
        if ( !Misc.empty(cc) )
            data.put(EmailSender.KEY_CC, cc);
        if ( !Misc.empty(bcc))
            data.put(EmailSender.KEY_BCC, bcc);
        data.put(EmailSender.KEY_SUBJECT, subject);
        data.put(EmailSender.KEY_BODY, message);

        // prekopiruj vsechny promenne ze session, ktere zacinaji prefixem
        for ( Enumeration e = session.getAttributeNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            if (name.startsWith(PREFIX))
                data.put(name.substring(PREFIX.length()), session.getAttribute(name));
        }

        if ( EmailSender.sendEmail(data) )
            ServletUtils.addMessage("Vaše zpráva byla odeslána.", env, session);
        else
            ServletUtils.addMessage("Litujeme, ale došlo k chybě při odesílání vaši zprávy.", env, session);

        String url = (String) params.get(PARAM_URL);
        if (url==null || url.length()==0) url = "/";
        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, url);
        return null;
    }
}
