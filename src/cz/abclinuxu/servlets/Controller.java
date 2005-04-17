/*
 * User: literakl
 * Date: 8.2.2004
 * Time: 21:28:44
 */
package cz.abclinuxu.servlets;

import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.url.UrlUtils;
import cz.abclinuxu.servlets.utils.url.URLMapper;
import cz.abclinuxu.servlets.html.HTMLVersion;
import cz.abclinuxu.servlets.wap.WapVersion;

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
        String server = request.getServerName();
        if (server.startsWith(URLMapper.Version.WAP.toString()))
            WapVersion.process(request, response, env);
        else
            HTMLVersion.process(request, response, env);
    }

    /**
     * This step consolidates common initialization tasks like parsing parameters, autenthification etc.
     */
    protected void performInit(HttpServletRequest request, HttpServletResponse response, Map env) throws InvalidInputException {
        Map params = ServletUtils.putParamsToMap(request);
        env.put(Constants.VAR_PARAMS, params);
        env.put(Constants.VAR_URL_UTILS, new UrlUtils(request.getRequestURI(), response));
        env.put(Constants.VAR_REQUEST_URI, request.getRequestURI());
        ServletUtils.handleMessages(request, env);
        ServletUtils.handleLogin(request, response, env);
    }
}
