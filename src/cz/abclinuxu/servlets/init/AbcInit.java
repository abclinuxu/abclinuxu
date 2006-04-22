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
package cz.abclinuxu.servlets.init;

import cz.abclinuxu.scheduler.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.news.NewsCategories;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.email.monitor.InstantSender;
import cz.abclinuxu.utils.email.forum.CommentSender;
import freemarker.template.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;


/**
 * This servlet initializes web application
 */
public class AbcInit extends HttpServlet implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcInit.class);

    public static final String PREF_START_RSS_MONITOR = "start.rss.monitor";
    public static final String PREF_START_RSS_GENERATOR = "start.rss.generator";
    public static final String PREF_START_RSS_KERNEL = "start.rss.kernel";
    public static final String PREF_START_RSS_UNIXSHOP = "start.rss.unixshop";
    public static final String PREF_START_RSS_OKSYSTEM = "start.rss.oksystem";
    public static final String PREF_START_VARIABLE_FETCHER = "start.variable.fetcher";
    public static final String PREF_START_ARTICLE_POOL_MONITOR = "start.article.pool.monitor";
    public static final String PREF_START_ABC_MONITOR = "start.abc.monitor";
    public static final String PREF_START_FORUM_MAIL_GATEWAY = "start.forum.mail.gateway";
    public static final String PREF_START_WEEKLY_EMAILS = "start.weekly.emails";
    public static final String PREF_START_WEEKLY_SUMMARY = "start.weekly.summary";
    public static final String PREF_START_UPDATE_DATETOOL = "start.datetool.service";
    public static final String PREF_START_WATCHED_DISCUSSIONS_CLEANER = "start.watched.discussions.cleaner";
    public static final String PREF_START_UPDATE_STATISTICS = "start.update.statistics";

    Timer scheduler;
    VariableFetcher fetcher;
    private Map services = new HashMap(16, 1.0f);
    static AbcInit instance;

    public AbcInit() {
        if (instance != null)
            log.fatal("AbcInit can have single instance only!");
        else
            instance = this;
    }

    public static AbcInit getInstance() {
        return instance;
    }

    public void init() throws ServletException {
        String tmp = getInitParameter("PREFERENCES");
        ConfigurationManager.init(tmp);
        ConfigurationManager.getConfigurator().configureMe(this);

        fetcher = VariableFetcher.getInstance();
        fetcher.run();

        String path = getServletContext().getRealPath("/")+"/";
        configureFreeMarker(path);
        tmp = getInitParameter("TEMPLATES");
        try {
            FMTemplateSelector.initialize(path+tmp);
        } catch (Exception e) {
            log.fatal("Nemohu inicializovat systém ¹ablon!", e);
        }

        startTasks();
        log.info("Inicializace je hotova");
    }

    /**
     * Starts all tasks.
     * TODO bug 447 - neni to tak snadne, instance Timer musi byt vytvoreny znovu, stejne tak i Thready.
     */
    public void startTasks() {
        if (scheduler != null) {
            log.info("Vypinam bezici ulohy");
            scheduler.cancel();
            scheduler = null;
        }
        log.info("Startuji ulohy");
        scheduler = new Timer(true);
        startFetchingVariables();
        startUpdateStatistics();
        startKernelUpdate();
        startLinksUpdate();
        startUnixshopUpdate();
        startOKSystemUpdate();
        startGenerateLinks();
        startArticlePoolMonitor();
        startSendingWeeklyEmails();
        startWeeklySummary();
        startObjectMonitor();
        startForumSender();
        startDateToolUpdateService();
        startWatchedDiscussionsCleaner();
        log.info("Ulohy jsou nastartovany");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

    /**
     * Update links each three hours.
     */
    protected void startLinksUpdate() {
        if ( !isSet(PREF_START_RSS_MONITOR) ) {
            log.info("RSS monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS monitor");
        scheduler.schedule(UpdateLinks.getInstance(), 60 * 1000, 3 * 60 * 60 * 1000);
    }

    /**
     * Update kernel versions each hour, starting after one minute
     */
    protected void startKernelUpdate() {
        if ( !isSet(PREF_START_RSS_KERNEL) ) {
            log.info("RSS kernel monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS kernel monitor");
        scheduler.schedule(UpdateKernel.getInstance(), 60*1000, 1*60*60*1000);
    }

    /**
     * Update unixshop RSS each two hours, starting after two minutes
     */
    protected void startUnixshopUpdate() {
        if ( !isSet(PREF_START_RSS_UNIXSHOP) ) {
            log.info("RSS Unixshop monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS unixshop monitor");
        scheduler.schedule(new UnixshopFetcher(), 2*60*1000, 2*60*60*1000);
    }

    /**
     * Update oksystem RSS each hour, starting after two minutes
     */
    protected void startOKSystemUpdate() {
        if ( !isSet(PREF_START_RSS_OKSYSTEM) ) {
            log.info("RSS OKSystem monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS OKSystem monitor");
        scheduler.schedule(new OKSystemFetcher(), 1*60*1000, 2*60*60*1000);
    }

    /**
     * Generate file with newest links each hour, starting 30 seconds later
     */
    protected void startGenerateLinks() {
        if ( !isSet(PREF_START_RSS_GENERATOR) ) {
            log.info("RSS generator configured not to run");
            return;
        }
        log.info("Scheduling RSS generator");
        scheduler.schedule(new GenerateLinks(), 30*1000, 1*60*60*1000);
    }

    /**
     * Fetches some context variables each 30 seconds, starting now.
     * It must be called before Freemarker's init.
     */
    protected void startFetchingVariables() {
        if ( !isSet(PREF_START_VARIABLE_FETCHER) ) {
            log.info("Template variable fetcher configured not to run");
            return;
        }
        log.info("Scheduling template variables fetcher");
        scheduler.schedule(fetcher, 0, 30*1000);
    }

    /**
     * Fetches some context variables each minute, starting now.
     */
    protected void startUpdateStatistics() {
        if ( !isSet(PREF_START_UPDATE_STATISTICS) ) {
            log.info("Batch update of statistics configured not to run");
            return;
        }
        log.info("Scheduling batch update of statistics");
        UpdateStatistics task = UpdateStatistics.getInstance();
        scheduler.schedule(task, 0, 60*1000);
        task.setBatchMode(true);
    }

    /**
     * Cleaner starts after 5 minute from startup and repeats each six hours.
     */
    protected void startWatchedDiscussionsCleaner() {
        if ( !isSet(PREF_START_WATCHED_DISCUSSIONS_CLEANER) ) {
            log.info("Watched discussion cleaner configured not to run");
            return;
        }
        log.info("Scheduling watched discussion cleaner");
        scheduler.schedule(EnsureWatchedDiscussionsLimit.getInstance(), 2*60*1000, 6*60*60*1000);
    }

    /**
     * Monitors article pool and moves articles to new articles, when they are ready.
     * Start one minute later with period of 3 minutes.
     */
    protected void startArticlePoolMonitor() {
        if ( !isSet(PREF_START_ARTICLE_POOL_MONITOR) ) {
            log.info("Article pool monitor configured not to run");
            return;
        }
        log.info("Scheduling ArticlePool monitor");
        scheduler.schedule(new ArticlePoolMonitor(), 1*60*1000, 3*60*1000);
    }

    /**
     * Sends notifications, when monitored object is changed.
     */
    protected void startObjectMonitor() {
        if ( !isSet(PREF_START_ABC_MONITOR) ) {
            log.info("Abc monitor configured not to run");
            return;
        }
        InstantSender monitor = InstantSender.getInstance();
        monitor.start();
    }

    /**
     * Sends notifications, when monitored object is changed.
     */
    protected void startForumSender() {
        if ( !isSet(PREF_START_FORUM_MAIL_GATEWAY) ) {
            log.info("Forum email gateway configured not to run");
            return;
        }
        CommentSender sender = CommentSender.getInstance();
        sender.start();
    }

    /**
     * Send weekly emails each saturday noon.
     */
    private void startSendingWeeklyEmails() {
        if ( !isSet(PREF_START_WEEKLY_EMAILS) ) {
            log.info("Weekly email sender configured not to run");
            return;
        }
        log.info("Scheduling WeeklyEmail sender");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if ( new Date().after(calendar.getTime()) ) {
            calendar.add(Calendar.DAY_OF_WEEK, 7);
        }

        scheduler.scheduleAtFixedRate(new WeeklyEmail(), calendar.getTime(), 7*24*60*60*1000);
    }

    /**
     * Send weekly emails each saturday noon.
     */
    private void startWeeklySummary() {
        if ( !isSet(PREF_START_WEEKLY_SUMMARY) ) {
            log.info("Weekly summary configured not to be generated");
            return;
        }
        log.info("Scheduling Weekly summary generator");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        if ( new Date().after(calendar.getTime()) ) {
            calendar.add(Calendar.DAY_OF_WEEK, 7);
        }

        scheduler.scheduleAtFixedRate(new WhatHappened(), calendar.getTime(), 7*24*60*60*1000);
    }

    /**
     * Send weekly emails each saturday noon.
     */
    private void startDateToolUpdateService() {
        if ( !isSet(PREF_START_UPDATE_DATETOOL) ) {
            log.info("Weekly summary configured not to be generated");
            return;
        }
        log.info("Scheduling DateTool update service");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 10);
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        scheduler.scheduleAtFixedRate(new UpdateDateTool(), calendar.getTime(), 24*60*60*1000);
    }

    /**
     * set ups freemarker
     */
    void configureFreeMarker(String path) {
        log.info("Inicializuji FreeMarker");
        Configuration cfg = FMUtils.getConfiguration();

        try {
            cfg.setSharedVariable(Constants.VAR_TOOL,new Tools());
            cfg.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
            cfg.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
            cfg.setSharedVariable(Constants.VAR_FETCHER, VariableFetcher.getInstance());
            cfg.setSharedVariable(Constants.VAR_CATEGORIES, NewsCategories.getInstance());

            log.info("Inicializace FreeMarkeru je hotova");
        } catch (Exception e) {
            log.error("cannot store shared variable!", e);
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        services.put(PREF_START_ABC_MONITOR, new Boolean(prefs.getBoolean(PREF_START_ABC_MONITOR, true)));
        services.put(PREF_START_ARTICLE_POOL_MONITOR, new Boolean(prefs.getBoolean(PREF_START_ARTICLE_POOL_MONITOR, true)));
        services.put(PREF_START_FORUM_MAIL_GATEWAY, new Boolean(prefs.getBoolean(PREF_START_FORUM_MAIL_GATEWAY, true)));
        services.put(PREF_START_RSS_GENERATOR, new Boolean(prefs.getBoolean(PREF_START_RSS_GENERATOR, true)));
        services.put(PREF_START_RSS_KERNEL, new Boolean(prefs.getBoolean(PREF_START_RSS_KERNEL, true)));
        services.put(PREF_START_RSS_MONITOR, new Boolean(prefs.getBoolean(PREF_START_RSS_MONITOR, true)));
        services.put(PREF_START_RSS_UNIXSHOP, new Boolean(prefs.getBoolean(PREF_START_RSS_UNIXSHOP, true)));
        services.put(PREF_START_VARIABLE_FETCHER, new Boolean(prefs.getBoolean(PREF_START_VARIABLE_FETCHER, true)));
        services.put(PREF_START_WEEKLY_EMAILS, new Boolean(prefs.getBoolean(PREF_START_WEEKLY_EMAILS, true)));
        services.put(PREF_START_WEEKLY_SUMMARY, new Boolean(prefs.getBoolean(PREF_START_WEEKLY_SUMMARY, true)));
        services.put(PREF_START_UPDATE_DATETOOL, new Boolean(prefs.getBoolean(PREF_START_UPDATE_DATETOOL, true)));
        services.put(PREF_START_WATCHED_DISCUSSIONS_CLEANER, new Boolean(prefs.getBoolean(PREF_START_WATCHED_DISCUSSIONS_CLEANER, true)));
        services.put(PREF_START_RSS_OKSYSTEM, new Boolean(prefs.getBoolean(PREF_START_RSS_OKSYSTEM, true)));
        services.put(PREF_START_UPDATE_STATISTICS, new Boolean(prefs.getBoolean(PREF_START_UPDATE_STATISTICS, true)));
    }

    /**
     * Test, whether give service is set to true.
     */
    protected boolean isSet(String name) {
        Boolean aBoolean = (Boolean) services.get(name);
        if (aBoolean!=null)
            return aBoolean.booleanValue();
        return false;
    }
}
