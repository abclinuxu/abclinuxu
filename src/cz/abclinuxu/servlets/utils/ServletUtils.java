/*
 * User: literakl
 * Date: Aug 11, 2002
 * Time: 7:47:54 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.utils;

import org.apache.velocity.context.Context;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;

import javax.servlet.http.*;
import java.util.*;
import java.io.UnsupportedEncodingException;

import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.data.User;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;

/**
 * Class to hold useful methods related to servlets
 * environment.
 */
public class ServletUtils {
    static Logger log = Logger.getLogger(ServletUtils.class);

    /** holds username when performing login */
    public static final String PARAM_LOG_USER = "LOGIN";
    /** holds password when performing login */
    public static final String PARAM_LOG_PASSWORD = "PASSWORD";
    /** indicates, that user wishes to logout */
    public static final String PARAM_LOG_OUT = "logout";

    /**
     * Combines request's parameters with parameters stored in special session
     * attribute and returns result as map. If parameter holds single value,
     * simple String->String mapping is created. If parameter holds multiple values,
     * String->Array of Strings mapping is created. The key is always parameter's name.
     */
    public static Map putParamsToMap(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map map = (Map) session.getAttribute(Constants.VAR_PARAMS);
        if ( map!=null )
            session.removeAttribute(Constants.VAR_PARAMS);
        else
            map = new HashMap();

        Enumeration names = request.getParameterNames();
        while ( names.hasMoreElements() ) {
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
     * Sets messages and errors in env. If session contains them already,
     * values from session are used, otherwise they are created.
     */
    public static void handleMessages(HttpServletRequest request, Map env) {
        HttpSession session = request.getSession();

        Map errors = (Map) session.getAttribute(Constants.VAR_ERRORS);
        if ( errors!=null )
            session.removeAttribute(Constants.VAR_ERRORS);
        else
            errors = new HashMap(5);
        env.put(Constants.VAR_ERRORS,errors);

        List messages = (List) session.getAttribute(Constants.VAR_MESSAGES);
        if ( messages!=null )
            session.removeAttribute(Constants.VAR_MESSAGES);
        else
            messages = new ArrayList(2);
        env.put(Constants.VAR_MESSAGES,messages);
    }

    /**
     * Performs automatic login. If user wishes to log out, it does so. Otherwise
     * it searches special session attribute, request parameters or cookie for
     * login information in this order and tries user to log in. If it suceeds,
     * instance of User is stored in both session attribute and environment.
     */
    public static void handleLogin(HttpServletRequest request, HttpServletResponse response, Map env) {
        Persistance persistance = PersistanceFactory.getPersistance();
        HttpSession session = request.getSession();
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        Cookie cookie = getCookie(request,Constants.VAR_USER);

        if ( params.get(PARAM_LOG_OUT) != null ) {
            params.remove(PARAM_LOG_OUT);
            session.removeAttribute(Constants.VAR_USER);
            session.invalidate();
            if ( cookie!=null )
                deleteCookie(cookie,response);
            return;
        }

        User user = (User) session.getAttribute(Constants.VAR_USER);
        if ( user!=null ) {
            env.put(Constants.VAR_USER,user);
            return;
        }

        String login = (String) params.get(PARAM_LOG_USER);
        if ( ! Misc.empty(login) ) {
            user = new User();
            user.setLogin(login);
            List searched = new ArrayList(1);
            searched.add(user);

            List found = persistance.findByExample(searched,null);
            if ( found.size()==0 ) {
                ServletUtils.addError(PARAM_LOG_USER,"Pøihla¹ovací jméno nenalezeno!",env, null);
                return;
            }
            user = (User) found.get(0);
            persistance.synchronize(user);

            String password = (String) params.get(PARAM_LOG_PASSWORD);
            if ( !user.validatePassword(password) ) {
                ServletUtils.addError(PARAM_LOG_PASSWORD,"©patné heslo!",env, null);
                return;
            }

            handleLoggedIn(user,false,response);

        } else if ( cookie!=null ) {
            LoginCookie loginCookie = new LoginCookie(cookie);
            try {
                user = (User) persistance.findById(new User(loginCookie.id));
                handleLoggedIn(user,true,null);
            } catch (PersistanceException e) {
                deleteCookie(cookie,response);
                addError(Constants.ERROR_GENERIC,"Nalezena cookie s neznámým u¾ivatelem!",env, null);
                return;
            }

            if ( user.getPassword().hashCode()!=loginCookie.hash ) {
                deleteCookie(cookie,response);
                addError(Constants.ERROR_GENERIC,"Nalezena cookie se ¹patným heslem!",env, null);
                return;
            }
        } else {
            return;
        }

        session.setAttribute(Constants.VAR_USER,user);
        env.put(Constants.VAR_USER,user);

        // todo: remove it, when new security model is finished
        for (Iterator iter = user.getContent().iterator(); iter.hasNext();) {
            GenericObject obj = ((Relation) iter.next()).getChild();
            persistance.synchronize(obj);
        }
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
     * Finds cookie with given name in request.
     * return Cookie, if found, otherwise null.
     */
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if ( cookies==null )
            return null;

        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ( name.equals(cookie.getName()) )
                return cookie;
        }
        return null;
    }

    /**
     * Adds message to <code>VAR_ERRORS</code> map.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    public static void addError(String key, String errorMessage, Context context, HttpSession session) {
        Map errors = (Map) context.get(Constants.VAR_ERRORS);
        if ( errors==null ) {
            errors = new HashMap(5);
            context.put(Constants.VAR_ERRORS,errors);
        }

        errors.put(key,errorMessage);
        if ( session!=null )
            session.setAttribute(Constants.VAR_ERRORS,errors);
    }

    /**
     * Adds message to <code>VAR_MESSAGES</code> list.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    public static void addMessage(String message, Context context, HttpSession session) {
        List messages = (List) context.get(Constants.VAR_MESSAGES);
        if ( messages==null ) {
            messages = new ArrayList(2);
            context.put(Constants.VAR_MESSAGES,messages);
        }

        messages.add(message);
        if ( session!=null )
            session.setAttribute(Constants.VAR_MESSAGES,messages);
    }

    /**
     * Adds message to <code>VAR_ERRORS</code> map.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    public static void addError(String key, String errorMessage, Map env, HttpSession session) {
        Map errors = (Map) env.get(Constants.VAR_ERRORS);
        errors.put(key,errorMessage);
        if ( session!=null )
            session.setAttribute(Constants.VAR_ERRORS,errors);
    }

    /**
     * Adds message to <code>VAR_MESSAGES</code> list.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    public static void addMessage(String message, Map env, HttpSession session) {
        List messages = (List) env.get(Constants.VAR_MESSAGES);
        messages.add(message);
        if ( session!=null )
            session.setAttribute(Constants.VAR_MESSAGES,messages);
    }

    /**
     * Constructs URL, as it was called.
     */
    public static String getURL(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        if ( !Misc.empty(query) ) {
            url.append('?');
            url.append(query);
        }
        return url.toString();
    }

    /**
     * Handles situation, when user logs in. It checks his
     * setting, whether it shall create new cookie to simplify
     * next login (and how long cookie shall be valid) and
     * records time of last login.
     * @param user logged in user
     * @param cookieExists if true, it makes no sense to create cookie
     * @param response cookie will be placed here
     */
    private static void handleLoggedIn(User user, boolean cookieExists, HttpServletResponse response) {
        String now = Constants.isoFormat.format(new Date());
        DocumentHelper.makeElement(user.getData(), "data/system/last_login_date").setText(now);
        PersistanceFactory.getPersistance().update(user);

        if ( !cookieExists ) {
            String tmp = Tools.xpath(user,"/data/settings/login_cookie_validity");
            int valid = Misc.parseInt(tmp, 6 * 30 * 24 * 3600); // six months
            if ( valid!=0 ) {
                Cookie cookie = new LoginCookie(user).getCookie();
                cookie.setMaxAge(valid);
                response.addCookie(cookie);
            }
        }
    }

    /**
     * This class holds logic for login cookie.
     */
    static class LoginCookie {
        public int id=-1, hash;

        /**
         * Initializes cookie from user.
         */
        public LoginCookie(User user) {
            id = user.getId();
            hash = user.getPassword().hashCode();
        }

        /**
         * Initializes cookie from cookie. (used for reverse operation)
         */
        public LoginCookie(Cookie cookie) {
            String sid="",shash="";
            String value = cookie.getValue();
            if ( value==null || value.length()<6 ) return;

            int position = value.indexOf(':');
            if ( position!=-1 ) {
                sid = value.substring(0,position);
                shash = value.substring(position+1);
            } else {
                position = value.indexOf("%3A");
                if ( position!=-1 ) {
                    sid = value.substring(0,position);
                    shash = value.substring(position+3);
                } else
                    return;
            }
            id = Integer.parseInt(sid);
            hash = Integer.parseInt(shash);
        }

        /**
         * Creates cookie from already supplied information.
         */
        public Cookie getCookie() {
            String content = id+":"+hash;
            Cookie cookie = new Cookie(Constants.VAR_USER,content);
            cookie.setPath("/");
            return cookie;
        }
    }
}
