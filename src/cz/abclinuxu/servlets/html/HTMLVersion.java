/*
 * User: literakl
 * Date: 15.3.2004
 * Time: 21:17:03
 */
package cz.abclinuxu.servlets.html;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.URLMapper;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotAuthorizedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.io.Writer;
import java.io.IOException;

import freemarker.template.Template;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.apache.log4j.Logger;

/**
 * This class renders HTML version of portal.
 */
public class HTMLVersion {
    static Logger log = Logger.getLogger(HTMLVersion.class);
    static Logger logTemplate = Logger.getLogger("template");

    public static void process(HttpServletRequest request, HttpServletResponse response, Map env) throws IOException {
        try {
            URLMapper urlMapper = URLMapper.getInstance(URLMapper.Version.HTML);
            if ( urlMapper.redirectDeprecated(request, response) )
                return;
            setLayout(request, urlMapper);

            AbcAction action = urlMapper.findAction(request, env);
            String templateName = action.process(request, response, env);
            if ( Misc.empty(templateName) )
                return;

            Template template = Configuration.getDefaultConfiguration().getTemplate(templateName);
            response.setContentType("text/html; charset=ISO-8859-2");
            Writer writer = response.getWriter();

            response.setDateHeader("Last-Modified", new Date().getTime());
            response.setHeader("Expires", "Fri, 22 Dec 2000 05:00:00 GMT");
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");

            template.process(env, writer);
            writer.flush();
        } catch (Exception e) {
            error(request, response, e);
        }
    }

    public static void setLayout(HttpServletRequest request, URLMapper urlMapper) {
        String serverName = request.getServerName();
        int i = serverName.indexOf(urlMapper.getDomain());
        if ( i>0 ) {
            String server = serverName.substring(0, i-1);
            if ( server.startsWith("www") || !FMTemplateSelector.layoutExists(server) )
                return;
            request.setAttribute(TemplateSelector.PARAM_VARIANTA, server);
        }
    }

    /**
     * Displays error page.
     */
    public static void error(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        response.setContentType("text/html; charset=ISO-8859-2");
        Writer writer = response.getWriter();
        String url = ServletUtils.getURL(request);
        Template template = null;

        Configuration config = Configuration.getDefaultConfiguration();
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
            log.error("Unknown error at "+url, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            template = config.getTemplate("/errors/generic.ftl");
        }
        SimpleHash root = new SimpleHash();
        root.put("EXCEPTION", e.toString());
        try {
            template.process(root, writer);
        } catch (TemplateException e1) {
            log.error("Cannot display error screen!", e);
        }
        writer.flush();
    }
}
