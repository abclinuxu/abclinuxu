/*
 * User: literakl
 * Date: Jun 8, 2002
 * Time: 7:44:57 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.servlets.Constants;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * This class dumps all request's parameters (except PARAM_URL) to log file.
 * @deprecated I think, that is useless now
 */
public class DumpRequest extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DumpRequest.class);
    static org.apache.log4j.Logger dumpLog = org.apache.log4j.Logger.getLogger("RequestLog");

    /** url, where flow shall be redirected */
    public static final String PARAM_URL = "url";
    /** message, that shall be displayed on redirected page */
    public static final String PARAM_MESSAGE = "msg";

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = null;
        StringBuffer sb = new StringBuffer();
        Enumeration params = request.getParameterNames();

        while ( params.hasMoreElements() ) {
            String name = (String) params.nextElement();
            String value = request.getParameter(name);

            if ( name.equals(PARAM_URL) ) {
                url = value;
                continue;
            } else if ( name.equals(PARAM_MESSAGE) ) {
                HttpSession session = request.getSession();
                ArrayList messages = new ArrayList(1);
                messages.add(value);
                session.setAttribute(Constants.VAR_MESSAGES,messages);
                continue;
            }

            sb.append(name+" = " + value+"\n");
        }
        dumpLog.info(sb.toString());

        if ( url==null || url.length()==0 ) {
            log.warn("No URL specified! Using default /Index!");
            url = "/Index";
        }

        response.sendRedirect(response.encodeRedirectURL(url));
    }
}
