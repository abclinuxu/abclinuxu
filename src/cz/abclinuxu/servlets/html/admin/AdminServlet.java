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
package cz.abclinuxu.servlets.html.admin;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.User;
import cz.abclinuxu.persistence.Persistence;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Various administrative tasks.
 */
public class AdminServlet implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";

    public static final String ACTION_CLEAR_CACHE = "clearCache";
    public static final String ACTION_PERFORM_CHECK = "performCheck";
    public static final String ACTION_RECREATE_RSS = "refreshRss";
    public static final String ACTION_RESTART_TASKS = "restartTasks";
    public static final String ACTION_SWITCH_MAINTAINANCE = "switchMaintainance";

    public static final String VAR_DATABASE_STATE = "DATABASE_VALID";
    public static final String VAR_FULLTEXT_STATE = "FULLTEXT_VALID";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_PERFORM_CHECK.equals(action) )
            return performCheck(request, env);

        User user = (User) env.get(Constants.VAR_USER);
        if ( user==null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( !(user.isMemberOf(Constants.GROUP_ADMINI)) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_RECREATE_RSS.equals(action))
            return refreshRss(request, env);

        if (ACTION_CLEAR_CACHE.equals(action) )
            return clearCache(request,env);

        if (ACTION_RESTART_TASKS.equals(action) )
            return restartTasks(request,env);

        if (action == null)
            return FMTemplateSelector.select("Admin", "show", env, request);

        if (!user.hasRole(Roles.ROOT))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_SWITCH_MAINTAINANCE.equals(action))
            return switchMaintainance(request, env);

        return null;
    }

    /**
     * Clears all caches.
     */
    private String clearCache(HttpServletRequest request, Map env) throws Exception {
        PersistenceFactory.getPersistance().clearCache();
        TemplateSelector.initialize(null);
        ConfigurationManager.reconfigureAll();
        FMUtils.getConfiguration().clearTemplateCache();
	    DateTool.calculateTodayTimes();
        VariableFetcher.getInstance().run(); // refresh variables

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "promazal cache");
        ServletUtils.addMessage("Cache byla promazána.",env,null);
        return FMTemplateSelector.select("Admin", "show", env, request);
    }

    /**
     * Restarts all tasks. Usefull when some thread dies for some reason.
     * todo finish. It does not work, implementation issues
     */
    private String restartTasks(HttpServletRequest request, Map env) throws Exception {
        AbcInit.getInstance().startTasks();
        ServletUtils.addMessage("Úlohy byly restartovány.",env,null);
        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "restartoval ulohy");
        return FMTemplateSelector.select("Admin", "show", env, request);
    }

    /**
     * Switches maintainance mode.
     */
    private String switchMaintainance(HttpServletRequest request, Map env) throws Exception {
        boolean mode = AbcConfig.isMaintainanceMode();
        AbcConfig.setMaintainanceMode(! mode);
        ServletUtils.addMessage("Re¾im údr¾by " + ((mode) ? "vypnut" : "zapnut"),env,null);
        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, ((mode) ? "vypnul" : "zapnul") + " re¾im údr¾by");
        return FMTemplateSelector.select("Admin", "show", env, request);
    }

    /**
     * Utility method for monitoring health of portal. Used by Broadnet.
     * Content of page is defined in web/freemarker/print/misc/admin_check.ftl
     * todo add other checks, e.g. jobs and threads
     */
    private String performCheck(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistance();
        try {
            persistence.findById(new Category(Constants.CAT_HARDWARE));
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
    private String refreshRss(HttpServletRequest request, Map env) throws Exception {
        FeedGenerator.updateArticles();
        FeedGenerator.updateBazaar();
        FeedGenerator.updateBlog(null);
        FeedGenerator.updateBlogDigest();
        FeedGenerator.updateDictionary();
        FeedGenerator.updateDrivers();
        FeedGenerator.updateFAQ();
        FeedGenerator.updateForum();
        FeedGenerator.updateHardware();
        FeedGenerator.updateNews();
        FeedGenerator.updatePolls();
        FeedGenerator.updateSoftware();

        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, "pregeneroval rss");
        return FMTemplateSelector.select("Admin", "show", env, request);
    }
}
