/*
 * User: literakl
 * Date: Apr 21, 2002
 * Time: 8:51:06 AM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.servlets.view;

import cz.abclinuxu.servlets.AbcServlet;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Performs search across the data.
 */
public class Search extends AbcServlet {
    /** contains relation, that match the expression */
    public static final String VAR_RESULT = "RESULT";
    /** expression to be searched */
    public static final String PARAM_EXPRESSION = "expression";
    /** action show advanced options */
    public static final String ACTION_OPTIONS = "options";
    /** action evaluate expression */
    public static final String ACTION_EVALUATE = "eval";


    protected Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        init(request,response,ctx);

        Persistance persistance = PersistanceFactory.getPersistance();
        VelocityHelper helper = new VelocityHelper();
        Map params = (Map) request.getAttribute(AbcServlet.ATTRIB_PARAMS);
        String action = (String) params.get(AbcServlet.PARAM_ACTION);

        if ( action==null || action.equals(ACTION_OPTIONS) ) {
            return getTemplate("view/search_advanced.vm");
        }
        if ( action.equals(ACTION_EVALUATE) ) {
        }
        return getTemplate("view/search_advanced.vm");
    }

    /**
     * searches for given expression
     */
    protected Template actionEval(HttpServletRequest request, HttpServletResponse response, Context ctx) throws Exception {
        return getTemplate("view/search_result.vm");
    }

}
