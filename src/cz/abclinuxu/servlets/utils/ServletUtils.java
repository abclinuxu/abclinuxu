/*
 * User: literakl
 * Date: Aug 11, 2002
 * Time: 7:47:54 AM
 */
package cz.abclinuxu.servlets.utils;

import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.exceptions.InvalidInputException;
import org.apache.log4j.Logger;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.io.File;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Class to hold useful methods related to servlets
 * environment.
 */
public class ServletUtils implements Configurable {
    static Logger log = Logger.getLogger(ServletUtils.class);

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureAndRememberMe(new ServletUtils());
    }

    public static final String PREF_UPLOAD_PATH = "upload.path";
    public static final String DEFAULT_UPLOAD_PATH = "/tmp/upload";
    public static final String PREF_MAX_UPLOAD_SIZE = "max.upload.size";
    public static final int DEFAULT_MAX_UPLOAD_SIZE = 50*1024;

    /** holds username when performing login */
    public static final String PARAM_LOG_USER = "LOGIN";
    /** holds password when performing login */
    public static final String PARAM_LOG_PASSWORD = "PASSWORD";
    /** indicates, that user wishes to logout */
    public static final String PARAM_LOG_OUT = "logout";

    public static final String VAR_ERROR_MESSAGE = "ERROR";

    static DefaultFileItemFactory uploadFactory;
    static int uploadSizeLimit;

    /**
     * Combines request's parameters with parameters stored in special session
     * attribute and returns result as map. If parameter holds single value,
     * simple String->String mapping is created. If parameter holds multiple values,
     * String->List of Strings mapping is created. The key is always parameter's name.
     */
    public static Map putParamsToMap(HttpServletRequest request) throws InvalidInputException {
        HttpSession session = request.getSession();
        Map map = (Map) session.getAttribute(Constants.VAR_PARAMS);
        if ( map!=null )
            session.removeAttribute(Constants.VAR_PARAMS);
        else
            map = new HashMap();

        if ( DiskFileUpload.isMultipartContent(request) ) {
            DiskFileUpload uploader = new DiskFileUpload(uploadFactory);
            uploader.setSizeMax(uploadSizeLimit);
            try {
                List items = uploader.parseRequest(request);
                for ( Iterator iter = items.iterator(); iter.hasNext(); ) {
                    FileItem fileItem = (FileItem) iter.next();
                    if ( fileItem.isFormField() ) {
                        try {
                            String value = fileItem.getString("ISO-8859-1");
                            map.put(fileItem.getFieldName(), value.trim());
                        } catch (UnsupportedEncodingException e) {}
                    } else {
                        map.put(fileItem.getFieldName(), fileItem);
                    }
                }
            } catch (FileUploadException e) {
                throw new InvalidInputException("Chyba pøi ètení dat. Není zvolený soubor pøíli¹ velký?");
            }
        } else {
            Enumeration names = request.getParameterNames();
            while ( names.hasMoreElements() ) {
                String name = (String) names.nextElement();
                String[] values = request.getParameterValues(name);

                if ( values.length==1 ) {
                    String value = values[0];
                    try {
                        value = new String(value.getBytes("ISO-8859-1"));
                    } catch (UnsupportedEncodingException e) {log.error(e);}
                    map.put(name, value.trim());

                } else {
                    List list = new ArrayList(values.length);
                    for ( int i = 0; i<values.length; i++ ) {
                        String value = values[i].trim();
                        if ( value.length()==0 )
                            continue;
                        try {
                            value = new String(value.getBytes("ISO-8859-1"));
                        } catch (UnsupportedEncodingException e) {log.error(e);}
                        list.add(value);
                    }
                    map.put(name, list);
                }
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
        HttpSession session = request.getSession();
        Map params = (Map) env.get(Constants.VAR_PARAMS);

        if ( params.get(PARAM_LOG_OUT) != null ) {
            params.remove(PARAM_LOG_OUT);
            session.removeAttribute(Constants.VAR_USER);
            session.invalidate();
            Cookie cookie = getCookie(request, Constants.VAR_USER);
            if ( cookie!=null )
                deleteCookie(cookie,response);
            return;
        }

        User user = (User) session.getAttribute(Constants.VAR_USER);
        if ( user!=null ) {
            env.put(Constants.VAR_USER,user);
            return;
        }

        Persistance persistance = PersistanceFactory.getPersistance();
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
            user = (User) persistance.findById(user);

            String password = (String) params.get(PARAM_LOG_PASSWORD);
            if ( !user.validatePassword(password) ) {
                ServletUtils.addError(PARAM_LOG_PASSWORD,"©patné heslo!",env, null);
                return;
            }

            handleLoggedIn(user,false,response);

        } else {
            Cookie cookie = getCookie(request, Constants.VAR_USER);
            if ( cookie==null )
                return;

            LoginCookie loginCookie = new LoginCookie(cookie);
            try {
                user = (User) persistance.findById(new User(loginCookie.id));
            } catch (PersistanceException e) {
                deleteCookie(cookie,response);
                log.warn("Nalezena cookie s neznámým u¾ivatelem!");
                addError(Constants.ERROR_GENERIC,"Nalezena cookie s neznámým u¾ivatelem!",env, null);
                return;
            }

            if ( user.getPassword().hashCode()!=loginCookie.hash ) {
                deleteCookie(cookie,response);
                log.warn("Nalezena cookie se ¹patným heslem!");
                addError(Constants.ERROR_GENERIC,"Nalezena cookie se ¹patným heslem!",env, null);
                return;
            }
            handleLoggedIn(user, true, null);
        }

        session.setAttribute(Constants.VAR_USER, user);
        env.put(Constants.VAR_USER, user);
    }

    /**
     * Adds cookie. This method must not be called, if header was written to response already.
     * @param cookie
     * @param response
     */
    public static void addCookie(Cookie cookie, HttpServletResponse response) {
        response.addCookie(cookie);
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
     * Displays standard error page with custom message.
     * @param message message to be displayed
     * @param env environment
     * @param request http servlet request
     * @return page to be displayed
     */
    public static String showErrorPage(String message, Map env, HttpServletRequest request) {
        env.put(VAR_ERROR_MESSAGE,message);
        return FMTemplateSelector.select("ServletUtils", "errorPage", env, request);
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
     * Concatenates servletPath and pathInfo.
     * @param servletPath
     * @param pathInfo
     * @return combination of these two strings.
     */
    public static String combinePaths(String servletPath, String pathInfo) {
        if ( servletPath==null )
            return pathInfo;
        if ( pathInfo==null )
            return servletPath;
        return servletPath.concat(pathInfo);

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
        String now;
        synchronized (Constants.isoFormat) {
            now = Constants.isoFormat.format(new Date());
        }
        DocumentHelper.makeElement(user.getData(), "data/system/last_login_date").setText(now);
        PersistanceFactory.getPersistance().update(user); // session bug here

        if ( !cookieExists ) {
            int valid = 6*30*24*3600; // six months
            Node node = user.getData().selectSingleNode("/data/settings/cookie_valid");
            if ( node!=null )
                valid = Misc.parseInt(node.getText(), valid);
            if ( valid!=0 ) {
                Cookie cookie = new LoginCookie(user).getCookie();
                cookie.setMaxAge(valid);
                addCookie(cookie,response);
            }
        }
    }

    /**
     * Callback to configure this class.
     * @param prefs
     * @throws ConfigurationException
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String uploadPath = prefs.get(PREF_UPLOAD_PATH,DEFAULT_UPLOAD_PATH);
        uploadSizeLimit = prefs.getInt(PREF_MAX_UPLOAD_SIZE, DEFAULT_MAX_UPLOAD_SIZE);
        File file = new File(uploadPath);
        file.mkdirs();
        uploadFactory = new DefaultFileItemFactory(1024,file);
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
