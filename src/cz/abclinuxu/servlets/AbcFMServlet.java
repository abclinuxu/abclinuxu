/*
 * User: Leos Literak
 * Date: Dec 8, 2002
 * Time: 5:50:12 PM
 */
package cz.abclinuxu.servlets;

import freemarker.template.*;
import freemarker.ext.beans.BeansWrapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.NotAuthorizedException;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.UrlUtils;

/**
 * Superclass for all servlets. It does some initialization
 * and defines common contract for its children.
 */
public abstract class AbcFMServlet extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcFMServlet.class);

    /** freemarker's main class */
    private Configuration config;


    /**
     * Put your processing here. Return null, if you have redirected browser to another URL.
     * @param env holds all variables, that shall be available in template, when it is being processed.
     * It may also contain VAR_USER and VAR_PARAMS objects.
     * @return name of template to be executed or null
     */
    protected abstract String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception;

    /**
     * Entry point of request's handling.
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map data = new HashMap();
            performInit(request,response,data);
            String templateName = process(request,response,data);
            if ( Misc.empty(templateName) )
                return;

            Template template = config.getTemplate(templateName);
            response.setContentType("text/html; charset=ISO-8859-2");
            Writer writer = response.getWriter();
            template.process(data,writer);
            writer.flush();
        } catch (Exception e) {
            error(request,response,e);
        }
    }

    /**
     * This step consolidates common initialization tasks like parsing parameters, autenthification etc.
     */
    private void performInit(HttpServletRequest request, HttpServletResponse response, Map env) {
        Map params = ServletUtils.putParamsToMap(request);
        env.put(Constants.VAR_PARAMS,params);
        env.put(Constants.VAR_URL_UTILS,new UrlUtils(request.getRequestURI(), response));
        ServletUtils.handleMessages(request,env);
        ServletUtils.handleLogin(request,response,env);
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
    private void error(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("text/html; charset=ISO-8859-2");
        Writer writer = response.getWriter();
        String  url = ServletUtils.getURL(request);
        Template template = null;

        if ( e instanceof NotFoundException ) {
            log.error("Not found: "+url);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            template = config.getTemplate("errors/notfound.ftl");
        } else if ( e instanceof NotAuthorizedException ) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            template = config.getTemplate("errors/denied.ftl");
        } else {
            log.error("Unknown error: "+url,e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            template = config.getTemplate("errors/generic.ftl");
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
