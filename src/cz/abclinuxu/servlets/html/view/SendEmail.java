/*
 * User: literakl
 * Date: 6.7.2004
 * Time: 13:23:10
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.User;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.email.EmailSender;
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
        User user = (User) env.get(Constants.VAR_USER);
        if ( user!=null ) {
            Map params = (Map) env.get(Constants.VAR_PARAMS);
            params.put(PARAM_SENDER, user.getEmail());
        }

        Integer kod = new Integer(new Random().nextInt(10000));
        request.getSession().setAttribute(VAR_KOD, kod);
        env.put(VAR_KOD, kod);

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
                ServletUtils.addError(VAR_KOD, "Vyplòte správný kód!", env, null);
                chyba = true;
            }
        } catch (Exception e) {
            ServletUtils.addError(VAR_KOD, "Vyplòte správný kód!", env, null);
            chyba = true;
        }
        String from = (String) params.get(PARAM_SENDER);
        if ( from==null || from.length()<6 || from.indexOf('@')==-1 || from.indexOf('.')==-1 ) {
            ServletUtils.addError(PARAM_SENDER, "Zadejte platnou adresu!", env, null);
            chyba = true;
        }
        String subject = (String) params.get(PARAM_SUBJECT);
        if ( subject==null || subject.length()==0 ) {
            ServletUtils.addError(PARAM_SUBJECT, "Zadejte pøedmìt!", env, null);
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
        data.put(EmailSender.KEY_SUBJECT, subject);
        data.put(EmailSender.KEY_BODY, message);

        // prekopiruj vsechny promenne ze session, ktere zacinaji prefixem
        for ( Enumeration e = session.getAttributeNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            if (name.startsWith(PREFIX))
                data.put(name.substring(PREFIX.length()), session.getAttribute(name));
        }

        if ( EmailSender.sendEmail(data) )
            ServletUtils.addMessage("Va¹e zpráva byla odeslána.", env, session);
        else
            ServletUtils.addMessage("Litujeme, ale do¹lo k chybì pøi odesílání va¹i zprávy.", env, session);

        UrlUtils urlUtils = (UrlUtils) env.get(Constants.VAR_URL_UTILS);
        urlUtils.redirect(response, "/");
        return null;
    }
}
