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
import cz.abclinuxu.persistence.Nursery;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.init.AbcInit;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.servlets.utils.template.TemplateSelector;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.search.CreateIndex;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.security.AdminLogger;
import cz.abclinuxu.security.Roles;
import cz.abclinuxu.security.ActionProtector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

/**
 * Various administrative tasks.
 */
public class AdminServlet implements AbcAction {
    public static final String PARAM_RELATION_SHORT = "rid";
    public static final String PARAM_USER = "uid";

    public static final String ACTION_CLEAR_CACHE = "clearCache";
    public static final String ACTION_PERFORM_CHECK = "performCheck";
    public static final String ACTION_RECREATE_RSS = "refreshRss";
    public static final String ACTION_RESTART_TASKS = "restartTasks";
    public static final String ACTION_SWITCH_MAINTAINANCE = "switchMaintainance";
    public static final String ACTION_SWITCH_USER = "su";

    public static final String VAR_DATABASE_STATE = "DATABASE_VALID";
    public static final String VAR_FULLTEXT_STATE = "FULLTEXT_VALID";
    public static final String VAR_QUERIES = "QUERIES";

    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String action = (String) params.get(PARAM_ACTION);

        if ( ACTION_PERFORM_CHECK.equals(action) )
            return performCheck(request, env);

        User user = (User) env.get(Constants.VAR_USER);
        if ( user == null )
            return FMTemplateSelector.select("ViewUser", "login", env, request);
        if ( ! user.isMemberOf(Constants.GROUP_ADMINI) )
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_RECREATE_RSS.equals(action)) {
            ActionProtector.ensureContract(request, AdminServlet.class, true, false, false, true);
            return refreshRss(request, env);
        }

        if (ACTION_CLEAR_CACHE.equals(action) ) {
            ActionProtector.ensureContract(request, AdminServlet.class, true, false, false, true);
            return clearCache(request,env);
        }

        if (ACTION_RESTART_TASKS.equals(action) ) {
            ActionProtector.ensureContract(request, AdminServlet.class, true, false, false, true);
            return restartTasks(request,env);
        }

        if (action == null)
            return FMTemplateSelector.select("Admin", "show", env, request);

        if (!user.hasRole(Roles.ROOT))
            return FMTemplateSelector.select("ViewUser", "forbidden", env, request);

        if (ACTION_SWITCH_MAINTAINANCE.equals(action)) {
            ActionProtector.ensureContract(request, AdminServlet.class, true, false, false, true);
            return switchMaintainance(request, env);
        }

        if (ACTION_SWITCH_USER.equals(action)) {
            ActionProtector.ensureContract(request, AdminServlet.class, true, true, true, false);
            return switchUser(request, response, env);
        }

        return null;
    }

    /**
     * Clears all caches.
     */
    private String clearCache(HttpServletRequest request, Map env) throws Exception {
        Nursery.getInstance().clearCache();
        PersistenceFactory.getPersistence().clearCache();
        TemplateSelector.initialize(null);
        FMUtils.getConfiguration().clearTemplateCache();
        ConfigurationManager.reconfigureAll();
        AbcInit.getInstance().configureFreeMarker();
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
        ServletUtils.addMessage("Režim údržby " + ((mode) ? "vypnut" : "zapnut"),env,null);
        User user = (User) env.get(Constants.VAR_USER);
        AdminLogger.logEvent(user, ((mode) ? "vypnul" : "zapnul") + " režim údržby");
        return FMTemplateSelector.select("Admin", "show", env, request);
    }

    private String switchUser(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {
        Map params = (Map) env.get(Constants.VAR_PARAMS);
        String s = (String) params.get(PARAM_USER);
        if (Misc.empty(s)) {
            ServletUtils.addError(Constants.ERROR_GENERIC, "Zadejte číslo uživatele", env, null);
            return FMTemplateSelector.select("Admin", "show", env, request);
        }

        int uid = Integer.parseInt(s);
        Persistence persistence = PersistenceFactory.getPersistence();
        User user = (User) persistence.findById(new User(uid));

        env.put(Constants.VAR_USER, user);
        HttpSession session = request.getSession();
        session.setAttribute(Constants.VAR_USER, user);

        response.sendRedirect("/");
        return null;
    }

    /**
     * Utility method for monitoring health of portal. Used by Broadnet.
     * Content of page is defined in web/freemarker/print/misc/admin_check.ftl
     * todo add other checks, e.g. jobs and threads
     */
    private String performCheck(HttpServletRequest request, Map env) throws Exception {
        Persistence persistence = PersistenceFactory.getPersistence();
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
        env.put(VAR_FULLTEXT_STATE, ok);

        String alias = ProxoolFacade.getAliases()[0];
        SnapshotIF snapshot = ProxoolFacade.getSnapshot(alias);
        env.put(VAR_QUERIES, snapshot.getServedCount());

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
        FeedGenerator.updatePersonalities();
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
