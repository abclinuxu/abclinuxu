/*
 * Created by IntelliJ IDEA.
 * User: literakl
 * Date: Jan 2, 2002
 * Time: 4:14:49 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package cz.abclinuxu.servlets;

import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.context.Context;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.view.SelectIcon;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Base class for all servlets. It provides several useful
 * methods for accessing Context, session manipulation
 * and check of access rights.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dl><code>VAR_PERSISTANCE</code></dl>
 * <dd>Instance of actual persistance. To be used for synchronization purposes.</dd>
 * <dt><code>VAR_URL_UTILS</code></dt>
 * <dd>Helper class for manipulating URLs. It remembers current context and session id.</dd>
 * <dt><code>VAR_USER</code></dt>
 * <dd>instance of User, if any. It is stored in both Session and Context.</dd>
 * <dt><code>VAR_ERRORS</code></dt>
 * <dd>Map of error messages. For form validation, use field name as key.</dd>
 * <dt><code>VAR_MESSAGES</code></dt>
 * <dd>List of informational messages.</dd>
 * <dt><code>VAR_PARAMS</code></dt>
 * <dd>Combined version of request's params and session's params. Use it only, if
 * view can be merged after SelectIcon.</dd>
 * <dt><code>VAR_HELPER</code></dt>
 * <dd>Instance of VelocityHelper.</dd>
 * </dl>
 * <u>Parameters used by AbcServlet's descendants</u>
 * <dl>
 * <dt><code>PARAM_ACTION</code></dt>
 * <dd>Contains shorthand of method to be executed. E.g. STEP1 for doStep1 or FINISH for doFinish.</dd>
 * <dt><code>PARAM_LOG_USER</code></dt>
 * <dd>Contains login of user wishing to log in.</dd>
 * <dt><code>PARAM_LOG_PASSWORD</code></dt>
 * <dd>Contains password of user wishing to log in.</dd>
 * </dl>
 */
public class AbcServlet extends VelocityServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbcServlet.class);

    /** Name of key in HttpServletsRequest, used for context chaining. */
    public static final String ATTRIB_CONTEXT = "CONTEXT";
    /** Name of key in HttpServletsRequest, used to combine request's params and session's params. */
    public static final String ATTRIB_PARAMS = "PARAMS";

    public static final String VAR_PERSISTANCE = "PERSISTANCE";
    public static final String VAR_URL_UTILS = "URL";
    public static final String VAR_USER = "USER";
    public static final String VAR_ERRORS = "ERRORS";
    public static final String VAR_MESSAGES = "MESSAGES";
    public static final String VAR_PARAMS = "PARAMS";
    public static final String VAR_HELPER = "UTIL";

    public static final String PARAM_ACTION = "action";
    public static final String PARAM_LOG_USER = "LOGIN";
    public static final String PARAM_LOG_PASSWORD = "PASSWORD";

    /** use this value for addError, when message is not tight to form field */
    public static final String GENERIC_ERROR = "generic";

    /** Public access is granted or user's right are sufficient. */
    public static final int ACCESS_GRANTED = 0;
    /** Public access is not granted, user must log in first. */
    public static final int LOGIN_REQUIRED = 1;
    /** Public access is not granted, session contains user, whose rights are not adequate enough. */
    public static final int USER_INSUFFICIENT_RIGHTS = 2;

    /** Only view access is desired to object. */
    public static final int METHOD_VIEW = 0;
    /** Modify access is desired */
    public static final int METHOD_EDIT = 1;
    /** Add access is desired */
    public static final int METHOD_ADD = 2;
    /** Remove access is desired */
    public static final int METHOD_REMOVE = 3;


    /**
     *  Returns a context suitable to pass to the handleRequest() method
     *  <br><br>
     *  If <code>request</code> contains parameter <code>CONTEXT</code>, new context
     *  will be initialized from it (context chaining), otherwise super.createContext()
     *  will be called.
     *  @param request servlet request from client
     *  @param response servlet reponse to client
     *
     *  @return context
     */
    protected Context createContext(HttpServletRequest request, HttpServletResponse response) {
        Context chained = (Context) request.getAttribute(AbcServlet.ATTRIB_CONTEXT);
        Context context = null;
        if ( chained!=null ) {
            context = new VelocityContext(chained);
            context.put(REQUEST, request);
            context.put(RESPONSE, response);
        } else {
            context = super.createContext(request, response);
            context.put(AbcServlet.VAR_PERSISTANCE,PersistanceFactory.getPersistance());
            context.put(AbcServlet.VAR_HELPER,new VelocityHelper());
            context.put(AbcServlet.VAR_URL_UTILS,new UrlUtils(request.getRequestURI(), response));
        }
        request.setAttribute(AbcServlet.ATTRIB_CONTEXT,context);
        return context;
    }

    /**
     * Performs initialization tasks. First, it checks, whether session contains <code>AbcServlet.VAR_USER</code>.
     * If not, it searches for cookie with same name. If the search was sucessful, it verifies password
     * and pushes new user to session and context.<br>
     * Cookie contains user's id, comma and password hash.<p>
     * Next it checks for parameter SelectIcon.PARAM_CHECK_SESSION. If found, it gets map
     * SelectIcon.ATTRIB_PARAMS from session and combines it with request's parameters map
     * into <code>AbcServlet.ATTRIB_PARAMS</code>. Thus you have uniform way of dealing
     * with parameters.
     * It is mandatory to use this method at the very beginning of <code>handleRequest()</code>.
     * @todo delete bad cookie
     */
    protected void init(HttpServletRequest request, HttpServletResponse response, Context context) {
        doLogin(request,response,context);

        HttpSession session = request.getSession();
        Map params = (Map) session.getAttribute(AbcServlet.ATTRIB_PARAMS);
        if ( params!=null ) {
            session.removeAttribute(AbcServlet.ATTRIB_PARAMS);
        }
        params = VelocityHelper.putParamsToMap(request,params);
        request.setAttribute(AbcServlet.ATTRIB_PARAMS,params);
        context.put(AbcServlet.VAR_PARAMS,params);

        Map errors = (Map) session.getAttribute(AbcServlet.VAR_ERRORS);
        if ( errors!=null ) {
            context.put(AbcServlet.VAR_ERRORS,errors);
            session.removeAttribute(AbcServlet.VAR_ERRORS);
        }

        List messages = (List) session.getAttribute(AbcServlet.VAR_MESSAGES);
        if ( messages!=null ) {
            context.put(AbcServlet.VAR_MESSAGES,messages);
            session.removeAttribute(AbcServlet.VAR_MESSAGES);
        }

        return;
    }

    /**
     * Checks login information. If user has not logged in yet, this method will first check
     * form parameter <code>AbcServlet.PARAM_LOG_USER</code> and next cookie <code>AbcServlet.VAR_USER</code>.
     * If user was found and approved, it is appended to context under name <code>AbcServlet.VAR_USER</code>.
     */
    protected void doLogin(HttpServletRequest request, HttpServletResponse response, Context context) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(AbcServlet.VAR_USER);

        if ( user!=null ) {
            context.put(AbcServlet.VAR_USER,user);
            return;
        }

        String login = (String) request.getParameter(AbcServlet.PARAM_LOG_USER);
        if ( login!=null ) {
            User tmpUser = new User(); tmpUser.setLogin(login);
            List searched = new ArrayList(); searched.add(tmpUser);

            try {
                List found = (List) PersistanceFactory.getPersistance().findByExample(searched,null);
                if ( found.size()==0 ) {
                    addError(AbcServlet.PARAM_LOG_USER,"P�ihla�ovac� jm�no nenalezeno!",context, null);
                    return;
                }
                user = (User) found.get(0);
            } catch (PersistanceException e) {
                log.error("Cannot verify login info",e);
                return;
            }

            if ( !user.validatePassword((String) request.getParameter(AbcServlet.PARAM_LOG_PASSWORD)) ) {
                addError(AbcServlet.PARAM_LOG_PASSWORD,"�patne heslo!",context, null);
                return;
            }

            String content = user.getId()+":"+user.getPassword().hashCode();
            Cookie cookie = new Cookie(AbcServlet.VAR_USER,content);
            cookie.setPath("/");
            cookie.setMaxAge(6*30*24*3600); // six months
            response.addCookie(cookie);
        }

        Cookie[] cookies = request.getCookies();
        for (int i = 0; user==null && cookies!=null && i<cookies.length; i++) {
            Cookie cookie = cookies[i];
            if ( cookie.getName().equals(AbcServlet.VAR_USER) ) {
                try {
                    String value = cookie.getValue();
                    int position = value.indexOf(':');
                    int id = Integer.parseInt(value.substring(0,position));
                    int hash = Integer.parseInt(value.substring(position+1));

                    try {
                        user = (User) PersistanceFactory.getPersistance().findById(new User(id));
                    } catch (PersistanceException e) {
                        addError(AbcServlet.GENERIC_ERROR,"Nalezena cookie s nezn�m�m u�ivatelem!",context, null);
                        break;
                    }

                    if ( user.getPassword().hashCode() != hash ) {
                        addError(AbcServlet.GENERIC_ERROR,"Nalezena cookie se �patn�m heslem!",context, null);
                        user = null;
                    }
                    break;
                } catch (Exception e) {
                    log.error("Cannot load user information from cookie "+cookie.getValue(),e);
                }
            }
        }

        if ( user!=null ) {
            for (Iterator iter = user.getContent().iterator(); iter.hasNext();) {
                GenericObject obj = (GenericObject) ((Relation) iter.next()).getChild();
                try {
                    PersistanceFactory.getPersistance().synchronize(obj);
                } catch (PersistanceException e) {
                    log.error("Cannot initialize content of user!",e);
                }
            }

            session.setAttribute(AbcServlet.VAR_USER,user);
            context.put(AbcServlet.VAR_USER,user);
        }
    }

    /**
     * Checks session for <code>USER</code>, than returns, whether users rights for <code>obj</code>
     * are sufficient for desired method. For <code>method</code>, use constant <code>METHOD_*</code>.
     * @return one of constants <code>ACCESS_GRANTED</code>, <code>LOGIN_REQUIRED</code>,
     * <code>USER_UNKNOWN</code>, <code>USER_BAD_PASSWORD</code> and <code>USER_INSUFFICIENT_RIGHTS</code>.
     */
    protected int checkAccess(GenericObject obj, int method, Context context) throws Exception {
        if ( method==AbcServlet.METHOD_VIEW ) {
            return AbcServlet.ACCESS_GRANTED;
        }

        User user = (User) context.get(AbcServlet.VAR_USER);
        if ( user==null || !user.isInitialized() ) {
            return AbcServlet.LOGIN_REQUIRED;
        }

        if ( obj==null ) return AbcServlet.USER_INSUFFICIENT_RIGHTS;
        if ( !obj.isInitialized() ) {
            PersistanceFactory.getPersistance().synchronize(obj);
        }

        if ( obj.isManagedBy(user) )  {
            return AbcServlet.ACCESS_GRANTED;
        }

        return AbcServlet.USER_INSUFFICIENT_RIGHTS;
    }

    /**
     * Adds message to <code>VAR_ERRORS</code> map.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    protected void addError(String key, String errorMessage, Context context, HttpSession session) {
        Map errors = (Map) context.get(VAR_ERRORS);

        if ( errors==null ) {
            errors = new HashMap(5);
            context.put(AbcServlet.VAR_ERRORS,errors);
        }
        if ( session!=null ) session.setAttribute(AbcServlet.VAR_ERRORS,errors);
        errors.put(key,errorMessage);
    }

    /**
     * Adds message to <code>VAR_MESSAGES</code> list.
     * <p>If session is not null, store messages into session. Handy for redirects and dispatches.
     */
    protected void addMessage(String message, Context context, HttpSession session) {
        boolean created = false;
        List messages = (List) context.get(VAR_MESSAGES);

        if ( messages==null ) {
            messages = new ArrayList(5);
            context.put(AbcServlet.VAR_MESSAGES,messages);
        }
        if ( session!=null ) session.setAttribute(AbcServlet.VAR_MESSAGES,messages);
        messages.add(message);
    }

    /**
     * Redirects to desired URL, keeping session and prefix.
     */
    public void redirect(String url, HttpServletResponse response, Context context) throws IOException {
        UrlUtils urlUtils = (UrlUtils) context.get(AbcServlet.VAR_URL_UTILS);
        String url2 = urlUtils.constructRedirectURL(url);
        response.sendRedirect(url2);
    }

    /**
     * Dispatches to desired URL, keeping prefix.
     */
    public void dispatch(String url, HttpServletRequest request, HttpServletResponse response, Context context) throws ServletException, IOException {
        UrlUtils urlUtils = (UrlUtils) context.get(AbcServlet.VAR_URL_UTILS);
        url = urlUtils.constructDispatchURL(url);
        RequestDispatcher dispatcher = request.getRequestDispatcher(url);
        dispatcher.forward(request,response);
    }

    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     */
    protected void error(HttpServletRequest request, HttpServletResponse response, Exception cause) throws ServletException, IOException {
        String message = "Str�nku nelze zobrazit!";
        if ( cause!=null && cause instanceof AbcException ) message = cause.getMessage();
        log.error("Exception caught in view!",cause);

        StringBuffer html = new StringBuffer();
        html.append("<html>");
        html.append("<body bgcolor=\"#ffffff\"><h2>");
        html.append( message );
        html.append("</h2>");
        html.append("<br>");
        html.append( cause );
        html.append("<br>");

        StringWriter sw = new StringWriter();
        cause.printStackTrace( new PrintWriter( sw ) );

        html.append( sw.toString()  );
        html.append("</body>");
        html.append("</html>");
        response.getOutputStream().print( html.toString() );
    }
}
