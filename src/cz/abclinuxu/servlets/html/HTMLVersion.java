/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.servlets.html;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.exceptions.NotFoundException;
import cz.abclinuxu.exceptions.MissingArgumentException;
import cz.abclinuxu.exceptions.NotAuthorizedException;
import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.data.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.util.prefs.Preferences;
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
public class HTMLVersion implements Configurable {
    static Logger log = Logger.getLogger(HTMLVersion.class);
    static Logger logTemplate = Logger.getLogger("template");

    public static final String PREF_DEFAULT_CSS_STYLE = "default.css.uri";

    private static String defaultCss;

    static {
        HTMLVersion instance = new HTMLVersion();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    public static void process(HttpServletRequest request, HttpServletResponse response, Map env) throws IOException {
        try {
            URLMapper urlMapper = URLMapper.getInstance(URLMapper.Version.HTML);
            if ( urlMapper.redirectDeprecated(request, response) )
                return;
            setLayout(request, env);

            long start = System.currentTimeMillis();

            AbcAction action = urlMapper.findAction(request, response, env);
            if (action == null)
                return;

            String templateName = action.process(request, response, env);
            if ( Misc.empty(templateName) )
                return;

            Template template = FMUtils.getConfiguration().getTemplate(templateName);
            response.setContentType("text/html; charset=UTF-8");
            Writer writer = response.getWriter();

            response.setDateHeader("Last-Modified", new Date().getTime());
            response.setHeader("Expires", "Fri, 22 Dec 2000 05:00:00 GMT");
            response.setHeader("Cache-Control", "no-cache, must-revalidate");
            response.setHeader("Pragma", "no-cache");

            template.process(env, writer);

            long end = System.currentTimeMillis();
            if (log.isDebugEnabled())
                log.debug("Processing of "+ env.get(Constants.VAR_REQUEST_URI) +" took "+(end-start)+" ms.");

            writer.flush();
        } catch (Exception e) {
            error(request, response, e);
        }
    }

    /**
     * Sets up css file and template variant to be used.
     */
    public static void setLayout(HttpServletRequest request, Map env) {
        User user = (User) env.get(Constants.VAR_USER);
        String css = null;
        if (user != null)
            css = Tools.xpath(user.getData(), "/data/settings/css");
        if (css == null)
            css = defaultCss;
        env.put(Constants.VAR_CSS_URI, css);

        String serverName = request.getServerName();
        int i = serverName.indexOf(AbcConfig.getDomain());
        if ( i > 0 ) {
            String server = serverName.substring(0, i - 1);
            if ( server.startsWith("www") || ! FMTemplateSelector.layoutExists(server) )
                return;
            request.setAttribute(TemplateSelector.PARAM_VARIANTA, server);
        }
    }

    /**
     * Displays error page.
     */
    public static void error(HttpServletRequest request, HttpServletResponse response, Throwable e) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        Writer writer = response.getWriter();
        String url = ServletUtils.getURL(request);
        Template template = null;

        Configuration config = FMUtils.getConfiguration();
        if ( e instanceof NotFoundException ) {
//            log.error("Not found: "+url);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            template = config.getTemplate("/errors/notfound.ftl");
        } else if ( e instanceof MissingArgumentException || e instanceof InvalidInputException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            template = config.getTemplate("/errors/badinput.ftl");
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
        root.put("EXCEPTION_MESSAGE", e.getMessage());
        try {
            template.process(root, writer);
        } catch (TemplateException e1) {
            log.error("Cannot display error screen!", e);
        }
        writer.flush();
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        defaultCss = prefs.get(PREF_DEFAULT_CSS_STYLE, "/styles.css");
    }
}
