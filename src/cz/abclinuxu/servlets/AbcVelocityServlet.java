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
import javax.servlet.*;

import cz.abclinuxu.data.*;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.AbcException;
import cz.abclinuxu.scheduler.jobs.UpdateLinks;
import cz.abclinuxu.servlets.utils.*;
import cz.abclinuxu.servlets.view.*;

import java.io.*;
import java.util.*;

/**
 * Base class for all servlets. It provides several useful
 * methods for accessing Context, session manipulation
 * and check of access rights.<p>
 * <u>Context variables introduced by AbcServlet</u>
 * <dl>
 * <dt><code>VAR_PERSISTANCE</code></dt>
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
 * <dt><code>VAR_INT</code></dt>
 * <dd>Instance of Integer, used for conversions from string to int.</dd>
 * </dl>
 * <u>Parameters used by AbcServlet's descendants</u>
 * <dl>
 * <dt><code>PARAM_ACTION</code></dt>
 * <dd>Contains shorthand of method to be executed. E.g. STEP1 for doStep1 or FINISH for doFinish.</dd>
 * <dt><code>PARAM_LOG_USER</code></dt>
 * <dd>Contains login of user wishing to log in.</dd>
 * <dt><code>PARAM_LOG_PASSWORD</code></dt>
 * <dd>Contains password of user wishing to log in.</dd>
 * <dt><code>PARAM_LOG_OUT</code></dt>
 * <dd>Exists, if user wishes to log out.</dd>
 * <dt><code>VAR_LINKS</code></dt>
 * <dd>Map, where key is Server and value is list of Links, where link.server==server.id && link.fixed==false.</dd>
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
    /** used by template */
    public static final String VAR_RUBRIKY = "RUBRIKY";
    public static final String VAR_ABCLINUXU = "ABCLINUXU";
    public static final String VAR_ANKETA = "ANKETA";
    public static final String VAR_LINKS = "LINKS";
    public static final String VAR_COUNTS = "COUNTS";

    public static final String PARAM_ACTION = "action";
    public static final String PARAM_LOG_USER = "LOGIN";
    public static final String PARAM_LOG_PASSWORD = "PASSWORD";
    public static final String PARAM_LOG_OUT = "logout";

    /** use this value for addError, when message is not tight to form field */
    public static final String GENERIC_ERROR = "generic";


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
        Context ctx = null;
        if ( chained!=null ) {
            ctx = new VelocityContext(chained);
            ctx.put(REQUEST, request);
            ctx.put(RESPONSE, response);
        } else {
            ctx = super.createContext(request, response);

            Persistance persistance = PersistanceFactory.getPersistance();
            ctx.put(AbcServlet.VAR_PERSISTANCE,persistance);

            VelocityHelper helper = new VelocityHelper();
            ctx.put(AbcServlet.VAR_HELPER,helper);

            ctx.put(AbcServlet.VAR_URL_UTILS,new UrlUtils(request.getRequestURI(), response));

            addTemplateVariables(ctx,persistance, helper);
        }
        request.setAttribute(AbcServlet.ATTRIB_CONTEXT,ctx);
        return ctx;
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
        params = ServletUtils.putParamsToMap(request,params);
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
        boolean logout = (request.getParameter(PARAM_LOG_OUT)!=null);
        Persistance persistance = PersistanceFactory.getPersistance();

        if ( logout ) {
            user = null;
            session.removeAttribute(VAR_USER);
        }
        if ( user!=null ) {
            context.put(AbcServlet.VAR_USER,user);
            return;
        }

        String login = (String) request.getParameter(AbcServlet.PARAM_LOG_USER);
        if ( login!=null && login.length()>0 ) {
            User tmpUser = new User(); tmpUser.setLogin(login);
            List searched = new ArrayList(); searched.add(tmpUser);

            try {
                List found = (List) persistance.findByExample(searched,null);
                if ( found.size()==0 ) {
                    ServletUtils.addError(AbcServlet.PARAM_LOG_USER,"Pøihla¹ovací jméno nenalezeno!",context, null);
                    return;
                }
                user = (User) found.get(0);
                persistance.synchronize(user);
            } catch (PersistanceException e) {
                log.error("Cannot verify login info",e);
                return;
            }

            if ( !user.validatePassword((String) request.getParameter(AbcServlet.PARAM_LOG_PASSWORD)) ) {
                ServletUtils.addError(AbcServlet.PARAM_LOG_PASSWORD,"©patne heslo!",context, null);
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
                    if ( logout ) {
                        ServletUtils.deleteCookie(cookie,response);
                        break;
                    }

                    String value = cookie.getValue();
                    if ( value==null || value.length()<6 ) break;

                    int position = value.indexOf(':');
                    String sid="",shash="";
                    if ( position!=-1 ) {
                        sid = value.substring(0,position);
                        shash = value.substring(position+1);
                    } else {
                        position = value.indexOf("%3A");
                        if ( position==-1 ) {
                            log.debug("Cookie doesn't contain colon: "+value);
                            break;
                        }
                        sid = value.substring(0,position);
                        shash = value.substring(position+3);
                    }
                    int id = Integer.parseInt(sid);
                    int hash = Integer.parseInt(shash);

                    try {
                        user = (User) persistance.findById(new User(id));
                    } catch (PersistanceException e) {
                        ServletUtils.deleteCookie(cookie,response);
                        ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Nalezena cookie s neznámým u¾ivatelem!",context, null);
                        break;
                    }

                    if ( user.getPassword().hashCode() != hash ) {
                        ServletUtils.deleteCookie(cookie,response);
                        ServletUtils.addError(AbcServlet.GENERIC_ERROR,"Nalezena cookie se ¹patným heslem!",context, null);
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
                    persistance.synchronize(obj);
                } catch (PersistanceException e) {
                    log.error("Cannot initialize content of user!",e);
                }
            }

            session.setAttribute(AbcServlet.VAR_USER,user);
            context.put(AbcServlet.VAR_USER,user);
        }
    }

    /**
     * Invoked when there is an error thrown in any part of doRequest() processing.
     * @todo Find, what to use instead of deprecated HttpUtils.getRequestURL
     */
    protected void error(HttpServletRequest request, HttpServletResponse response, Exception cause) throws ServletException, IOException {
        StringBuffer url = request.getRequestURL();
        url.insert(0,"Cannot display page ");
        if ( request.getQueryString()!=null ) {
            url.append('?');
            url.append(request.getQueryString());
        }

        if ( cause instanceof AbcException ) url.append("\n Status was: "+((AbcException)cause).getStatus());
        if ( !(cause instanceof IOException) )
            log.error(url.toString(),cause);

        ServletOutputStream os = response.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(os,"ISO-8859-2");
        writer.write("<html><body bgcolor=\"#ffffff\">");
        writer.write("<h2>Stránka nebyla nalezena</h2>");
        writer.write("Omlouváme se, ale systém nebyl schopen zobrazit zvolenou stránku. ");
        writer.write("Mo¾ná byla zmìnìna její adresa.<p>");
        writer.write("Dìkujeme za pochopeni.");
        writer.write("<p>Událost byla zalogována.");
        writer.write("</body></html>");
        writer.flush();
        writer.close();
    }

    /**
     * Initializes and stores variables used by template
     */
    private void addTemplateVariables(Context ctx, Persistance persistance, VelocityHelper helper) {
        try {
            Category rubriky = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
            helper.sync(rubriky.getContent());
            ctx.put(VAR_RUBRIKY,rubriky.getContent());

            Category abc = (Category) persistance.findById(new Category(Constants.CAT_ABC));
            helper.sync(abc.getContent());
            ctx.put(VAR_ABCLINUXU,abc.getContent());

            List list = persistance.findByCommand("select max(cislo) from anketa");
            Object[] objects = (Object[]) list.get(0);
            Poll poll = new Poll(((Integer)objects[0]).intValue());
            poll = (Poll) persistance.findById(poll);
            ctx.put(VAR_ANKETA,poll);

            Category linksCategory = (Category) persistance.findById(new Category(Constants.CAT_LINKS));
            Map links = UpdateLinks.groupLinks(linksCategory,persistance);
            ctx.put(VAR_LINKS,links);

            Map counts = new HashMap(4);
            list = persistance.findByCommand("select count(cislo) from zaznam where typ=1");
            objects = (Object[]) list.get(0);
            counts.put("HARDWARE",objects[0]);

            list = persistance.findByCommand("select count(cislo) from zaznam where typ=2");
            objects = (Object[]) list.get(0);
            counts.put("SOFTWARE",objects[0]);

            list = persistance.findByCommand("select count(cislo) from polozka where typ=5");
            objects = (Object[]) list.get(0);
            counts.put("DRIVERS",objects[0]);

            Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
            counts.put("FORUM",new Integer(forum.getContent().size()));

            ctx.put(VAR_COUNTS,counts);
        } catch (PersistanceException e) {
            log.warn("Cannot get default objects!",e);
        }
    }
}
