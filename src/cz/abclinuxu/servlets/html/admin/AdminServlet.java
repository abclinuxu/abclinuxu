/*
 * User: Leos Literak
 * Date: May 27, 2003
 * Time: 7:55:28 AM
 */
package cz.abclinuxu.servlets.html.admin;

import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.feeds.FeedGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Calendar;
import java.util.Date;
import java.io.File;

import freemarker.template.Configuration;

/**
 * Various administrative tasks.
 */
public class AdminServlet implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";

    public static final String ACTION_CLEAR_CACHE = "clearCache";
    public static final String ACTION_PERFORM_CHECK = "performCheck";
    public static final String ACTION_RECREATE_RSS = "refreshRss";

    public static final String VAR_DATABASE_STATE = "DATABASE_VALID";
    public static final String VAR_FULLTEXT_STATE = "FULLTEXT_VALID";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_PERFORM_CHECK.equals(action) )
            return performCheck(request, env);
        if ( ACTION_RECREATE_RSS.equals(action) )
            return refreshRss(request, env);

        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !(user.isMemberOf(Constants.GROUP_ADMINI)) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (action==null)
            return FMTemplateSelector.select("Admin", "show", env, request);

        if (ACTION_CLEAR_CACHE.equals(action) )
            return clearCache(request,env);

        return null;
    }

    /**
     * Clears all caches.
     */
    private final String clearCache(HttpServletRequest request, Map env) throws Exception {
        PersistanceFactory.getPersistance().clearCache();
        AbcInit.setServerLinksAstSharedVariables();
        TemplateSelector.initialize(null);
        ConfigurationManager.reconfigureAll();
        Configuration.getDefaultConfiguration().clearTemplateCache();
	    DateTool.calculateTodayTimes();

        ServletUtils.addMessage("Cache byla promazána.",env,null);
        return FMTemplateSelector.select("Admin", "show", env, request);
    }

    /**
     * Utility method for monitoring health of portal. Used by Broadnet.
     * Content of page is defined in web/freemarker/print/misc/admin_check.ftl
     * todo add other checks, e.g. jobs and threads
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

    /**
     * Creates all RSS from scratch again.
     */
    private final String refreshRss(HttpServletRequest request, Map env) throws Exception {
        FeedGenerator.updateArticles();
        FeedGenerator.updateBlog(null);
        FeedGenerator.updateDrivers();
        FeedGenerator.updateForum();
        FeedGenerator.updateHardware();
        FeedGenerator.updateNews();
        return FMTemplateSelector.select("Admin", "show", env, request);
    }
}
