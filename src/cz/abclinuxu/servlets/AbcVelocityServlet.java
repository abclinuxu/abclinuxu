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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.PersistanceException;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.servlets.utils.UrlUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Base class for all servlets. It provides several useful
 * methods for accessing Context, session manipulation
 * and check of access rights.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_PREFIX</code></dt>
 * <dd>Specifies prefix of URL (/hardware,/software,/clanky). It distinguishes context.</dd>
 * <dt><code>USER</code></dt>
 * <dd>instance of User, if any.</dd>
 * <dt><code>ERRORS</code></dt>
 * <dd>Map of error messages. For form validation, use field name as key.</dd>
 * <dt><code>MESSAGES</code></dt>
 * <dd>List of informational messages.</dd>
 * </dl>
 * <u>Parameters used by AbcServlet's descendants</u>
 * <dl>
 * <dt><code>ACTION</code></dt>
 * <dd>Contains shorthand of method to be executed. E.g. STEP1 for doStep1 or FINISH for doFinish.</dd>
 * </dl>
 */
public class AbcServlet extends VelocityServlet {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbcServlet.class);

    /** Name of key in HttpServletsRequest, used for context chaining. */
    public static final String ATTRIB_CONTEXT = "CONTEXT";
    public static final String VAR_PREFIX = "PREFIX";
    public static final String VAR_USER = "USER";
    public static final String VAR_ERRORS = "ERRORS";
    public static final String VAR_MESSAGES = "MESSAGES";
    public static final String PARAM_ACTION = "action";

    public static final String PREFIX_HARDWARE = "/hardware";
    public static final String PREFIX_SOFTWARE = "/software";
    public static final String PREFIX_CLANKY = "/clanky";

    /** Public access is granted or user's right are sufficient. */
    public static final int ACCESS_GRANTED = 0;
    /** Public access is not granted, user must log in first. */
    public static final int LOGIN_REQUIRED = 1;
    /** Public access is not granted, session contains invalid user. */
    public static final int USER_UNKNOWN = 2;
    /** Public access is not granted, session contains user with invalid password. */
    public static final int USER_BAD_PASSWORD = 3;
    /** Public access is not granted, session contains user, whose rights are not adequate enough. */
    public static final int USER_INSUFFICIENT_RIGHTS = 4;

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

            String url = request.getRequestURI();
            if ( url.startsWith(AbcServlet.PREFIX_HARDWARE) ) {
                context.put(AbcServlet.VAR_PREFIX,new UrlUtils(AbcServlet.PREFIX_HARDWARE));
            } else if ( url.startsWith(AbcServlet.PREFIX_SOFTWARE) ) {
                context.put(AbcServlet.VAR_PREFIX,new UrlUtils(AbcServlet.PREFIX_SOFTWARE));
            } else if ( url.startsWith(AbcServlet.PREFIX_CLANKY) ) {
                context.put(AbcServlet.VAR_PREFIX,new UrlUtils(AbcServlet.PREFIX_CLANKY));
            } else {
                context.put(AbcServlet.VAR_PREFIX,new UrlUtils(null));
            }
        }
        request.setAttribute(AbcServlet.ATTRIB_CONTEXT,context);
        return context;
    }

    /**
     * Checks, whether there is a session. If it is not, the method creates new session, it searches
     * for cookie with user id and password hash and creates new User. This instance is then stored
     * in both session and context. <p>
     * Cookie is name <code>AbcServlet.USER</code> and contains user's id, comma and password hash.<p>
     * It is mandatory to use this method at beginning of <code>handleRequest()</code>,
     * before <code>checkAccess()</code> is called.
     * @return one of constants <code>ACCESS_GRANTED</code>, <code>USER_UNKNOWN</code> and
     * <code>USER_BAD_PASSWORD</code>.
     * @todo delete bad cookie
     */
    protected int validateUserSession(HttpServletRequest request, HttpServletResponse response, Context context) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(AbcServlet.VAR_USER);
        if ( user==null ) {
            Cookie[] cookies = request.getCookies();
            for (int i = 0; cookies!=null && i<cookies.length; i++) {
                Cookie cookie = cookies[i];
                if ( cookie.getName().equals(AbcServlet.VAR_USER) ) {
                    try {
                        String value = cookie.getValue();
                        int position = value.indexOf(',');
                        int id = Integer.parseInt(value.substring(0,position));
                        int hash = Integer.parseInt(value.substring(position));

                        try {
                            user = (User) PersistanceFactory.getPersistance().findById(new User(id));
                        } catch (PersistanceException e) {
                            return AbcServlet.USER_UNKNOWN;
                        }

                        if ( user.getPassword().hashCode() != hash ) {
                            return AbcServlet.USER_BAD_PASSWORD;
                        }
                        session.setAttribute(AbcServlet.VAR_USER,user);
                        break;
                    } catch (Exception e) {
                        log.error("Cannot load user information from cookie "+cookie.getValue(),e);
                    }
                }
            }
        }
        context.put(AbcServlet.VAR_USER,user);//todo: do not put nulls!
        return AbcServlet.ACCESS_GRANTED;
    }

    /**
     * Checks session for <code>USER</code>, than returns, whether users rights for <code>obj</code>
     * are sufficient for desired method. For <code>method</code>, use constant <code>METHOD_*</code>.
     * @return one of constants <code>ACCESS_GRANTED</code>, <code>LOGIN_REQUIRED</code>,
     * <code>USER_UNKNOWN</code>, <code>USER_BAD_PASSWORD</code> and <code>USER_INSUFFICIENT_RIGHTS</code>.
     */
    protected int checkAccess(GenericObject obj, int method, Context context) {
        if ( method==AbcServlet.METHOD_VIEW ) {
            return AbcServlet.ACCESS_GRANTED;
        }
        User user = (User) context.get(AbcServlet.VAR_USER);
        if ( user==null || user.isInitialized()==false ) {
            return AbcServlet.LOGIN_REQUIRED;
        }
        if ( obj.isManagedBy(user) )  {
            return AbcServlet.ACCESS_GRANTED;
        }
        return AbcServlet.USER_INSUFFICIENT_RIGHTS;
    }

    /**
     * Adds message to <code>VAR_ERRORS</code> map.
     */
    protected void addErrorMessage(String key, String errorMessage, Context context) {
        Map errors = (Map) context.get(VAR_ERRORS);
        if ( errors==null ) errors = new HashMap(4);
        errors.put(key,errorMessage);
    }

    /**
     * Adds message to <code>VAR_MESSAGES</code> list.
     */
    protected void addMessage(String message, Context context) {
        List errors = (List) context.get(VAR_MESSAGES);
        if ( errors==null ) errors = new ArrayList(3);
        errors.add(message);
    }

    /**
     * Redirects to desired URL, keeping session and prefix.
     */
    public void redirect(String url, HttpServletResponse response, Context context) throws IOException {
        UrlUtils urlUtils = (UrlUtils) context.get(AbcServlet.VAR_PREFIX);
        String url2 = urlUtils.constructRedirectURL(url,response);
        response.sendRedirect(url2);
    }

    /**
     * Dispatches to desired URL, keeping prefix.
     */
    public void dispatch(String url, HttpServletRequest request, HttpServletResponse response, Context context) throws ServletException, IOException {
        UrlUtils urlUtils = (UrlUtils) context.get(AbcServlet.VAR_PREFIX);
        url = urlUtils.constructDispatchURL("/ViewCategory");
        RequestDispatcher dispatcher = request.getRequestDispatcher(url);
        dispatcher.forward(request,response);
    }

    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     */
    protected void error(HttpServletRequest request, HttpServletResponse response, Exception cause) throws ServletException, IOException {
        String message = "Stránku nelze zobrazit!";
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
