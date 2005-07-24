/*
 * User: literakl
 * Date: Jan 14, 2002
 * Time: 9:20:03 PM
 */
package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This servlet renders index page of AbcLinuxu.
 */
public class ViewIndex implements AbcAction {

    /**
     * Evaluate the request.
     */
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        return FMTemplateSelector.select("ViewIndex","show",env, request);
    }
}
