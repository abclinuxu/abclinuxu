package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.exceptions.InternalException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Simple class that just renders ftl template passed in parameter.
 * User: literakl
 * Date: 1.1.2009
 */
public class ShowPage implements AbcAction {
    public static final String PARAM_TEMPLATE = "template";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String ftlTemplate = (String) params.get(PARAM_TEMPLATE);
        if (ftlTemplate == null)
            throw new InternalException("Parametr " + PARAM_TEMPLATE + " není definován!");

        return FMTemplateSelector.select(ftlTemplate, env, request);
    }
}
