/*
 * User: literakl
 * Date: 8.2.2004
 * Time: 21:28:44
 */
package cz.abclinuxu.servlets;

import cz.abclinuxu.servlets.utils.URLMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class is responsible for selection of logic
 * based on URL mapping.
 */
public class Controller extends AbcFMServlet {

    /**
     * Based on URL of the request it chooses AbcAction implementation,
     * that fullfills the request.
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        AbcAction action = URLMapper.getInstance().findAction(request);
        return action.process(request, response, env);
    }
}
