/*
 * User: Leos Literak
 * Date: Dec 8, 2002
 * Time: 5:50:12 PM
 */
package cz.abclinuxu.servlets;

import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotAuthorizedException;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;
import cz.abclinuxu.servlets.utils.URLMapper;
import cz.abclinuxu.utils.Misc;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

/**
 * Superclass for all servlets. It does initialization
 * and defines common contract for its children.
 */
public abstract class AbcFMServlet extends HttpServlet implements AbcAction {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcFMServlet.class);

    /** freemarker's main class */
    private Configuration config;


    /**
     * Entry point of request's handling.
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (fixDeprecatedURL(request,response))
                return;

            Map env = new HashMap();
            performInit(request,response,env);

            long startExec = System.currentTimeMillis();
            String templateName = process(request,response,env);
            long endExec = System.currentTimeMillis();
            if ( Misc.empty(templateName) )
                return;

            Template template = config.getTemplate(templateName);
            // todo add WML support
            response.setContentType("text/html; charset=ISO-8859-2");
            Writer writer = response.getWriter();

            response.setDateHeader("Last-Modified", new Date().getTime());
            response.setHeader("Expires", "Fri, 22 Dec 2000 05:00:00 GMT");
            response.setHeader("Cache-Control","no-cache, must-revalidate");
            response.setHeader("Pragma","no-cache");

            long startRender = System.currentTimeMillis();
            template.process(env,writer);
            long endRender = System.currentTimeMillis();
            writer.flush();

            if ( log.isDebugEnabled() )
                log.debug(templateName+"- execution: "+(endExec-startExec)+" ms, rendering: "+(endRender-startRender)+" ms.");
        } catch (Throwable e) {
            error(request,response,e);
        }
    }

    /**
     * This step consolidates common initialization tasks like parsing parameters, autenthification etc.
     */
    private void performInit(HttpServletRequest request, HttpServletResponse response, Map env) throws InvalidInputException {
        Map params = ServletUtils.putParamsToMap(request);
        env.put(Constants.VAR_PARAMS,params);
        env.put(Constants.VAR_URL_UTILS,new UrlUtils(request.getRequestURI(), response));
        env.put(Constants.VAR_REQUEST_URI,request.getRequestURI());
        ServletUtils.handleMessages(request,env);
        ServletUtils.handleLogin(request,response,env);
    }

    /**
     * If URL is deprecated, redirect browser to correct location.
     * @return true, if browser has been redirected.
     */
    private boolean fixDeprecatedURL(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return URLMapper.getInstance().redirectDeprecated(request, response);
    }

    /**
     * Servlet initialization
     */
    public void init() throws ServletException {
        config = Configuration.getDefaultConfiguration();
    }

    /**
     * Displays error page.
     */
    private void error(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        response.setContentType("text/html; charset=ISO-8859-2");
        Writer writer = response.getWriter();
        String  url = ServletUtils.getURL(request);
        Template template = null;

        if ( e instanceof NotFoundException ) {
//            log.error("Not found: "+url);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            template = config.getTemplate("/errors/notfound.ftl");
        } else if ( e instanceof MissingArgumentException ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            template = config.getTemplate("/errors/generic.ftl");
        } else if ( e instanceof NotAuthorizedException ) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            template = config.getTemplate("/errors/denied.ftl");
        } else if ( e.getClass().getName().startsWith("freemarker") ) {
            log.error("Template error at "+url+", message: "+e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            template = config.getTemplate("/errors/generic.ftl");
        } else {
            log.error("Unknown error at "+url,e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            template = config.getTemplate("/errors/generic.ftl");
        }
        SimpleHash root = new SimpleHash();
        root.put("EXCEPTION",e.toString());
        try {
            template.process(root,writer);
        } catch (TemplateException e1) {
            log.error("Cannot display error screen!",e);
        }
        writer.flush();
    }
}
