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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for selection of logic
 * based on URL mapping.
 */
public class Controller extends HttpServlet {

    /**
     * Based on URL of the request it chooses AbcAction implementation in HTML format,
     * that fullfills the request.
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map env = new HashMap();
        performInit(request, response, env);
        String page = null;
        String server = request.getServerName();

        if (server.startsWith(URLMapper.Version.WAP.toString())) {
            page = Constants.PAGE_WAP;
            WapVersion.process(request, response, env);
        } else {
            page = detectHtmlPage((String)env.get(Constants.VAR_REQUEST_URI));
            HTMLVersion.process(request, response, env);
        }

        if (page != null)
            UpdateStatistics.getInstance().recordPageView(page);
    }

    private String detectHtmlPage(String uri) {
        if (uri.equals("/"))
            return Constants.PAGE_INDEX;
        if (uri.startsWith(UrlUtils.PREFIX_FORUM))
            return Constants.PAGE_FORUM;
        if (uri.startsWith(UrlUtils.PREFIX_CLANKY))
            return Constants.PAGE_ARTICLES;
        if (uri.startsWith(UrlUtils.PREFIX_NEWS))
            return Constants.PAGE_NEWS;
        if (uri.startsWith("/blog"))
            return Constants.PAGE_BLOGS;
        if (uri.startsWith(UrlUtils.PREFIX_HARDWARE))
            return Constants.PAGE_HARDWARE;
        if (uri.startsWith(UrlUtils.PREFIX_DICTIONARY))
            return Constants.PAGE_DICTIONARY;
        if (uri.startsWith(UrlUtils.PREFIX_DRIVERS))
            return Constants.PAGE_DRIVERS;
        if (uri.startsWith(UrlUtils.PREFIX_FAQ))
            return Constants.PAGE_FAQ;
        if (uri.startsWith(UrlUtils.PREFIX_POLLS))
            return Constants.PAGE_POLLS;
        if (uri.startsWith(UrlUtils.PREFIX_SOFTWARE))
            return Constants.PAGE_SOFTWARE;
        if (uri.startsWith("/ucebnice"))
            return Constants.PAGE_SCHOOLBOOK;
        if (uri.startsWith("/hosting"))
            return Constants.PAGE_HOSTING;
        return null;
    }

    /**
     * This step consolidates common initialization tasks like parsing parameters, autenthification etc.
     */
    protected void performInit(HttpServletRequest request, HttpServletResponse response, Map env) throws InvalidInputException {
        Map params = ServletUtils.putParamsToMap(request);
        env.put(Constants.VAR_PARAMS, params);
        String requestURI = request.getRequestURI();
        env.put(Constants.VAR_URL_UTILS, new UrlUtils(requestURI, response));
        env.put(Constants.VAR_REQUEST_URI, requestURI);
        ServletUtils.setCurrentURL(ServletUtils.getURL(request));
        ServletUtils.handleMessages(request, env);
        ServletUtils.handleLogin(request, response, env);
    }
}
