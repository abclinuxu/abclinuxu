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
import cz.abclinuxu.data.Category;
import cz.abclinuxu.utils.search.CreateIndex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.io.File;

/**
 * When called, clears content of cache of default persistance.
 */
public class AdminServlet extends AbcFMServlet {
    public static final String ACTION_CLEAR_CACHE = "clearCache";
    public static final String ACTION_PERFROM_CHECK = "performCheck";

    public static final String VAR_DATABASE_STATE = "DATABASE_VALID";
    public static final String VAR_FULLTEXT_STATE = "FULLTEXT_VALID";

    protected String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if (ACTION_CLEAR_CACHE.equals(action) )
            return clearCache(request,env);
        if (ACTION_PERFROM_CHECK.equals(action) )
            return performCheck(request,env);

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

    /**
     * Utility method for monitoring health of portal. Used by Broadnet.
     * Content of page is defined in web/freemarker/print/misc/admin_check.ftl
     * todo add other checks, e.g. fulltext, jobs and threads
     */
    private final String performCheck(HttpServletRequest request, Map env) throws Exception {
        Persistance persistance = PersistanceFactory.getPersistance();
        try {
            persistance.findById(new Category(Constants.CAT_HARDWARE));
            env.put(VAR_DATABASE_STATE, Boolean.TRUE);
        } catch (Exception e) {
            env.put(VAR_DATABASE_STATE,Boolean.FALSE);
        }

        boolean ok = false;
        File file = CreateIndex.getLastRunFile();
        if (file.exists()) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR,-25);
            Date lastModified = new Date(file.lastModified());
            if (lastModified.after(cal.getTime()))
                ok = true;
        }
        env.put(VAR_FULLTEXT_STATE, Boolean.valueOf(ok));

        return FMTemplateSelector.select("Admin", "check", env, request);
    }
}
