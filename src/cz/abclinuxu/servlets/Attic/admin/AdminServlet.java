/*
 * User: Leos Literak
 * Date: May 27, 2003
 * Time: 7:55:28 AM
 */
package cz.abclinuxu.servlets.admin;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.servlets.AbcFMServlet;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.data.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * When called, clears content of cache of default persistance.
 */
public class AdminServlet extends AbcFMServlet {
    public static final String ACTION_CLEAR_CACHE = "clearCache";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);
        User user = (User) env.get(Constants.VAR_USER);

        if (ACTION_CLEAR_CACHE.equals(action) ) {
            return clearCache(request,env);
        }

        return FMTemplateSelector.select("Admin", "show", env, request);
    }

    /**
     * Clears all caches.
     */
    private final String clearCache(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        persistance.clearCache();
        AbcInit.setSharedVariables();
        TemplateSelector.initialize(null);

        ServletUtils.addMessage("Cache byla promazána.",env,null);
        return FMTemplateSelector.select("Admin", "show", env, request);
    }
}
