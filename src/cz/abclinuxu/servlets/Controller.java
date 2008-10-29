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
package cz.abclinuxu.servlets;

import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.html.HTMLVersion;
import cz.abclinuxu.servlets.wap.WapVersion;
import cz.abclinuxu.scheduler.UpdateStatistics;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.forms.RichTextEditor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.regexp.REProgram;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

/**
 * This class is responsible for selection of logic
 * based on URL mapping.
 */
public class Controller extends HttpServlet implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Controller.class);
    public static final String PREF_BOTS_REGEXP = "regexp.bots";

    static REProgram reBots;
    static {
        ConfigurationManager.getConfigurator().configureAndRememberMe(new Controller());
    }

    /**
     * Based on URL of the request it chooses AbcAction implementation in HTML format,
     * that fullfills the request.
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map env = new HashMap();
        request.setAttribute(Constants.VAR_ENVIRONMENT, env);
        try {
            performInit(request, response, env);
        } catch (InvalidInputException e) {
            HTMLVersion.error(request, response, e, env);
            return;
        }
        String page = null;
        String server = request.getServerName();

//        String ua = request.getHeader("user-agent");
//        if (ua != null && ua.indexOf("Maxthon") != -1) {
//            response.getWriter().write("Utocil na nas spambot kryjici se jako prohlizec maxthon. Zkuste jiny prohlizec");
//            return;
//        }

        if (server.startsWith(URLMapper.Version.WAP.toString())) {
            page = Constants.PAGE_WAP;
            WapVersion.process(request, response, env);
        } else {
            Boolean bot = (Boolean) env.get(Constants.VAR_BOT_DETECTED);
            if (bot == null || ! bot.booleanValue()) // not interested in spiders and various bots
                page = detectHtmlPage((String)env.get(Constants.VAR_REQUEST_URI));
            HTMLVersion.process(request, response, env);
        }

        if (page != null)
            UpdateStatistics.getInstance().recordView(page, 1);
    }

    /*
    TODOs:
        1) rewrite using grammar with antlr, it will be much faster
        2) find better place, we match here even deprecated URIs that will be redirected
        or we cannot match URIs like /show/1234
    */
    private String detectHtmlPage(String uri) {
        if (uri.equals("/"))
            return Constants.PAGE_INDEX;
        if (uri.startsWith(UrlUtils.PREFIX_FORUM) || uri.startsWith("/poradna"))
            return Constants.PAGE_FORUM;
        if (uri.startsWith(UrlUtils.PREFIX_BLOG))
            return Constants.PAGE_BLOGS;
        if (uri.startsWith(UrlUtils.PREFIX_CLANKY) || uri.startsWith("/serialy") || uri.startsWith("/autori"))
            return Constants.PAGE_ARTICLES;
        if (uri.startsWith(UrlUtils.PREFIX_NEWS))
            return Constants.PAGE_NEWS;
        if (uri.startsWith(UrlUtils.PREFIX_SOFTWARE))
            return Constants.PAGE_SOFTWARE;
        if (uri.startsWith(UrlUtils.PREFIX_HARDWARE))
            return Constants.PAGE_HARDWARE;
        if (uri.startsWith("/hledani"))
            return Constants.PAGE_SEARCH;
        if (uri.startsWith(UrlUtils.PREFIX_SCREENSHOTS))
            return Constants.PAGE_SCREENSHOTS;
        if (uri.startsWith(UrlUtils.PREFIX_TAGS))
            return Constants.PAGE_TAGS;
        if (uri.startsWith(UrlUtils.PREFIX_DICTIONARY))
            return Constants.PAGE_DICTIONARY;
        if (uri.startsWith("/ucebnice"))
            return Constants.PAGE_SCHOOLBOOK;
        if (uri.startsWith(UrlUtils.PREFIX_PERSONALITIES))
            return Constants.PAGE_PERSONALITIES;
        if (uri.startsWith(UrlUtils.PREFIX_DRIVERS))
            return Constants.PAGE_DRIVERS;
        if (uri.startsWith(UrlUtils.PREFIX_FAQ))
            return Constants.PAGE_FAQ;
        if (uri.startsWith(UrlUtils.PREFIX_POLLS))
            return Constants.PAGE_POLLS;
        if (uri.startsWith(UrlUtils.PREFIX_VIDEOS))
            return Constants.PAGE_VIDEOS;
        if (uri.startsWith("/skupiny"))
            return Constants.PAGE_GROUPS;
        if (uri.startsWith(UrlUtils.PREFIX_EVENTS))
            return Constants.PAGE_EVENTS;
        if (uri.startsWith(UrlUtils.PREFIX_BAZAAR))
            return Constants.PAGE_BAZAAR;
        if (uri.startsWith("/lide") || uri.startsWith("/Profile"))
            return Constants.PAGE_BAZAAR;
        if (uri.startsWith("/hry"))
            return Constants.PAGE_GAMES;
        if (uri.startsWith("/nej"))
            return Constants.PAGE_TOP;
        if (uri.startsWith("/revize"))
            return Constants.PAGE_WIKITOOLS;
        if (uri.startsWith("/History"))
            return Constants.PAGE_HISTORY;
//        log.warn("Loguji nezname URI " + uri);
        return Constants.PAGE_UNKNOWN;
    }

    /**
     * This step consolidates common initialization tasks like parsing parameters, autenthification etc.
     */
    protected void performInit(HttpServletRequest request, HttpServletResponse response, Map env) throws InvalidInputException, IOException {
        Map params = ServletUtils.putParamsToMap(request);
        env.put(Constants.VAR_PARAMS, params);
        String requestURI = request.getRequestURI();
        env.put(Constants.VAR_URL_UTILS, new UrlUtils(requestURI, response, request));
        env.put(Constants.VAR_REQUEST_URI, requestURI);

        String ua = request.getHeader("user-agent");
        env.put(Constants.VAR_USER_AGENT, ua);
        if (ua != null && ua.length() > 3) {
            RE regexp = new RE(reBots);
            if (regexp.match(ua))
                env.put(Constants.VAR_BOT_DETECTED, Boolean.TRUE);
        }

        ServletUtils.setCurrentURL(ServletUtils.getURL(request));
        ServletUtils.handleMessages(request, env);
        ServletUtils.handleLogin(request, response, env);
        // todo optimalizace - at se to inicializuje jen v edit strankach, kde se pouziva RichTextEditor
        env.put(Constants.VAR_RICH_TEXT_EDITOR, new RichTextEditor(env));
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        String re = prefs.get(PREF_BOTS_REGEXP, null);
        try {
            reBots = new RECompiler().compile(re);
        } catch (RESyntaxException e) {
            throw new ConfigurationException("Invalid regexp: '" + re + "'!");
        }
    }
}
