/*
 * User: literakl
 * Date: Aug 11, 2002
 * Time: 7:47:54 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import org.apache.velocity.context.Context;

import javax.servlet.http.*;
import java.util.*;
import java.io.UnsupportedEncodingException;

import cz.abclinuxu.servlets.AbcVelocityServlet;

/**
 * Class to hold useful methods related to servlets
 * environment.
 */
public class ServletUtils {

    /**
     * Adds all parameters from request to specified map and returns it back.
     * If map is null, new HashMap is created. <p>
     * If there is only one value for a parameter, it will be stored directly
     * associated with parameter's name. But if there are at least two values,
     * they will be stored in list associated with parameter's name.
     */
    public static Map putParamsToMap(HttpServletRequest request, Map map) {
        if ( map==null ) map = new HashMap();
        Enumeration names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);

            if ( values.length==1 ) {
                String value = request.getParameter(name);
                try { value = new String(value.getBytes("ISO-8859-1")); } catch (UnsupportedEncodingException e) {}
                map.put(name,value.trim());

            } else {
                List list = new ArrayList(values.length);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    try { value = new String(value.getBytes("ISO-8859-1")); } catch (UnsupportedEncodingException e) {}
                    list.add(value.trim());
                }
                map.put(name,list);
            }
        }
        return map;
    }

    /**
     * Removes cookie from browser
     */
    public static void deleteCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Adds message to <code>VAR_ERRORS</code> map.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    public static void addError(String key, String errorMessage, Context context, HttpSession session) {
        Map errors = (Map) context.get(AbcVelocityServlet.VAR_ERRORS);

        if ( errors==null ) {
            errors = new HashMap(5);
            context.put(AbcVelocityServlet.VAR_ERRORS,errors);
        }
        if ( session!=null ) session.setAttribute(AbcVelocityServlet.VAR_ERRORS,errors);
        errors.put(key,errorMessage);
    }

    /**
     * Adds message to <code>VAR_MESSAGES</code> list.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    public static void addMessage(String message, Context context, HttpSession session) {
        boolean created = false;
        List messages = (List) context.get(AbcVelocityServlet.VAR_MESSAGES);

        if ( messages==null ) {
            messages = new ArrayList(5);
            context.put(AbcVelocityServlet.VAR_MESSAGES,messages);
        }
        if ( session!=null ) session.setAttribute(AbcVelocityServlet.VAR_MESSAGES,messages);
        messages.add(message);
    }
}
