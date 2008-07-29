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
import cz.abclinuxu.servlets.ws.UserAccountServiceImpl;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.data.view.NewsCategories;
import cz.abclinuxu.data.view.DriverCategories;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.email.monitor.InstantSender;
import cz.abclinuxu.utils.email.forum.CommentSender;
import cz.abclinuxu.persistence.extra.JobOfferManager;
import cz.abclinuxu.data.view.PropertySet;
import cz.abclinuxu.utils.feeds.FeedGenerator;
import freemarker.template.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;


/**
 * This servlet initializes web application
 */
public class AbcInit extends HttpServlet implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcInit.class);

    public static final String PREF_START = "start.";
    public static final String PREF_PERIOD = ".period";
    public static final String PREF_DELAY = ".delay";
    public static final String PREF_RSS_MONITOR = "rss.monitor";
    public static final String PREF_RSS_GENERATOR = "rss.generator";
    public static final String PREF_RSS_OKSYSTEM = "rss.oksystem";
    public static final String PREF_RSS_JOBPILOT = "rss.jobpilot";
    public static final String PREF_RSS_JOBSCZ = "rss.jobcz";
    public static final String PREF_RSS_64BIT = "rss.64bit";
    public static final String PREF_VARIABLE_FETCHER = "variable.fetcher";
    public static final String PREF_POOL_MONITOR = "pool.monitor";
    public static final String PREF_ABC_MONITOR = "abc.monitor";
    public static final String PREF_FORUM_MAIL_GATEWAY = "forum.mail.gateway";
    public static final String PREF_WEEKLY_EMAILS = "weekly.emails";
    public static final String PREF_WEEKLY_SUMMARY = "weekly.summary";
    public static final String PREF_UPDATE_DATETOOL = "datetool.service";
    public static final String PREF_WATCHED_DISCUSSIONS_CLEANER = "watched.discussions.cleaner";
    public static final String PREF_UPDATE_STATISTICS = "update.statistics";
    public static final String PREF_JOB_OFFER_MANAGER = "job.offer.manager";
    public static final String PREF_USER_SCORE_SETTER = "user.score.setter";
    public static final String PREF_WEB_SERVICES = "web.services";
    public static final String PREF_USERS_DEPLOY_PATH = "deploy.path.users";

    Timer scheduler, slowScheduler;
    VariableFetcher fetcher;
    private Map<String,Boolean> services = new HashMap<String, Boolean>(20, 1.0f);
    private Map<String,Integer> delays = new HashMap<String, Integer>(20, 1.0f);
    private Map<String, Integer> periods = new HashMap<String, Integer>(20, 1.0f);
    String endpointUrlServices;
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

        startWebServices();

        fetcher = VariableFetcher.getInstance();
        fetcher.run();

        String path = getServletContext().getRealPath("/")+"/";
        configureFreeMarker();
        tmp = getInitParameter("TEMPLATES");
        try {
            FMTemplateSelector.initialize(path+tmp);
        } catch (Exception e) {
            log.fatal("Nemohu inicializovat systém šablon!", e);
        }

        TagTool.init();
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
        scheduler = new Timer("scheduler", true);
        slowScheduler = new Timer("slow scheduler", true);
        startFetchingVariables();
        startUpdateStatistics();
        startLinksUpdate();
        startOKSystemUpdate();
        startJobPilotUpdate();
        startJobsCzUpdate();
        start64bitFetcher();
        startGenerateLinks();
        startPoolMonitor();
        startSendingWeeklyEmails();
        startWeeklySummary();
        startObjectMonitor();
        startForumSender();
        startCalculateUserScoreService();
        startDateToolUpdateService();
        startJobOfferUpdateService();
        startWatchedDiscussionsCleaner();
        log.info("Ulohy jsou nastartovany");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}

    /**
     * Fetches RSS feeds. It may take even minutes or hours to complete.
     */
    protected void startLinksUpdate() {
        if ( !isSet(PREF_RSS_MONITOR) ) {
            log.info("RSS monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS monitor");
        int delay = getDelay(PREF_RSS_MONITOR);
        int period = getPeriod(PREF_RSS_MONITOR);
        slowScheduler.schedule(UpdateLinks.getInstance(), delay, period);
    }

    /**
     * Update oksystem RSS.
     */
    protected void startOKSystemUpdate() {
        if ( !isSet(PREF_RSS_OKSYSTEM) ) {
            log.info("RSS OKSystem monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS OKSystem monitor");
        int delay = getDelay(PREF_RSS_OKSYSTEM);
        int period = getPeriod(PREF_RSS_OKSYSTEM);
        slowScheduler.schedule(new OKSystemFetcher(), delay, period);
    }

    /**
     * Update JobPilot RSS.
     */
    protected void startJobPilotUpdate() {
        if ( !isSet(PREF_RSS_JOBPILOT) ) {
            log.info("JobPilot RSS monitor configured not to run");
            return;
        }
        log.info("Scheduling JobPilot RSS monitor");
        int delay = getDelay(PREF_RSS_JOBPILOT);
        int period = getPeriod(PREF_RSS_JOBPILOT);
        slowScheduler.schedule(new JobPilotFetcher(), delay, period);
    }

    /**
     * Update Jobs.cz RSS.
     */
    protected void startJobsCzUpdate() {
        if ( !isSet(PREF_RSS_JOBSCZ) ) {
            log.info("Jobs.cz RSS monitor configured not to run");
            return;
        }
        log.info("Scheduling Jobs.cz RSS monitor");
        int delay = getDelay(PREF_RSS_JOBSCZ);
        int period = getPeriod(PREF_RSS_JOBSCZ);
        slowScheduler.schedule(new JobsCzFetcher(), delay, period);
    }
    
    protected void start64bitFetcher() {
        if ( !isSet(PREF_RSS_64BIT) ) {
            log.info("64bit.cz RSS monitor configured not to run");
            return;
        }
        
        log.info("Scheduling the 64bit.cz RSS monitor");
        int delay = getDelay(PREF_RSS_64BIT);
        int period = getPeriod(PREF_RSS_64BIT);
        slowScheduler.schedule(new Shop64bitFetcher(), delay, period);
    }

    /**
     * Generate RSS files with newest links.
     */
    protected void startGenerateLinks() {
        if ( !isSet(PREF_RSS_GENERATOR) ) {
            log.info("RSS generator configured not to run");
            return;
        }
        log.info("Scheduling RSS generator");
        int delay = getDelay(PREF_RSS_GENERATOR);
        int period = getPeriod(PREF_RSS_GENERATOR);
        scheduler.schedule(new GenerateLinks(), delay, period);
    }

    /**
     * Fills the caches with prepared queries.
     * It must be called before Freemarker's init.
     */
    protected void startFetchingVariables() {
        if ( !isSet(PREF_VARIABLE_FETCHER) ) {
            log.info("Template variable fetcher configured not to run");
            return;
        }
        log.info("Scheduling template variables fetcher");
        int delay = getDelay(PREF_VARIABLE_FETCHER);
        int period = getPeriod(PREF_VARIABLE_FETCHER);
        scheduler.schedule(fetcher, delay, period);
    }

    /**
     * Stores statistics about individual services usage.
     */
    protected void startUpdateStatistics() {
        if ( !isSet(PREF_UPDATE_STATISTICS) ) {
            log.info("Batch update of statistics configured not to run");
            return;
        }
        log.info("Scheduling batch update of statistics");
        UpdateStatistics task = UpdateStatistics.getInstance();
        int delay = getDelay(PREF_UPDATE_STATISTICS);
        int period = getPeriod(PREF_UPDATE_STATISTICS);
        scheduler.schedule(task, delay, period);
        task.setBatchMode(true);
    }

    /**
     * Cleans info about watched discussions over the limit.
     */
    protected void startWatchedDiscussionsCleaner() {
        if ( !isSet(PREF_WATCHED_DISCUSSIONS_CLEANER) ) {
            log.info("Watched discussion cleaner configured not to run");
            return;
        }
        log.info("Scheduling watched discussion cleaner");
        int delay = getDelay(PREF_WATCHED_DISCUSSIONS_CLEANER);
        int period = getPeriod(PREF_WATCHED_DISCUSSIONS_CLEANER);
        slowScheduler.schedule(EnsureWatchedDiscussionsLimit.getInstance(), delay, period);
    }

    /**
     * Monitors article and news pools for objects to be published.
     */
    protected void startPoolMonitor() {
        if ( !isSet(PREF_POOL_MONITOR) ) {
            log.info("Pool monitor configured not to run");
            return;
        }
        log.info("Scheduling Pool monitor");
        int delay = getDelay(PREF_POOL_MONITOR);
        int period = getPeriod(PREF_POOL_MONITOR);
        scheduler.schedule(new PoolMonitor(), delay, period);
    }

    /**
     * Fetch job offers from praceabc.cz.
     */
    private void startJobOfferUpdateService() {
        if (!isSet(PREF_JOB_OFFER_MANAGER)) {
            log.info("Job offers configured not to be fetched");
            return;
        }
        log.info("Scheduling job offers update service");
        int delay = getDelay(PREF_JOB_OFFER_MANAGER);
        int period = getPeriod(PREF_JOB_OFFER_MANAGER);
        scheduler.schedule(new JobOfferManager(), delay, period);
    }

    /**
     * Deploys web services
     */
    private void startWebServices() {
        if (!isSet(PREF_WEB_SERVICES)) {
            log.info("Web services configured not to be deployed");
            return;
        }
        log.info("Deploying web services");
        Endpoint.publish(endpointUrlServices, new UserAccountServiceImpl());
    }

    /**
     * Sends notifications, when monitored object is changed.
     */
    protected void startObjectMonitor() {
        if ( !isSet(PREF_ABC_MONITOR) ) {
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
        if ( !isSet(PREF_FORUM_MAIL_GATEWAY) ) {
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
        if ( !isSet(PREF_WEEKLY_EMAILS) ) {
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
     * Create weekly summary article.
     */
    private void startWeeklySummary() {
        if ( !isSet(PREF_WEEKLY_SUMMARY) ) {
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
     * Calculate today/yesterday time at midnight
     */
    private void startDateToolUpdateService() {
        if ( !isSet(PREF_UPDATE_DATETOOL) ) {
            log.info("DateTool update service configured not to be generated");
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
     * Calculate user score in early morning
     */
    private void startCalculateUserScoreService() {
        if ( !isSet(PREF_USER_SCORE_SETTER) ) {
            log.info("Update user score configured not to be calculated");
            return;
        }
        log.info("Scheduling update user score service");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 11);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        scheduler.scheduleAtFixedRate(new UpdateUserScore(), calendar.getTime(), 24*60*60*1000);
    }

    /**
     * set ups freemarker
     */
    public void configureFreeMarker() {
        log.info("Inicializuji FreeMarker");
        Configuration cfg = FMUtils.getConfiguration();

        try {
            cfg.setSharedVariable(Constants.VAR_TOOL,new Tools());
            cfg.setSharedVariable(Constants.VAR_FEEDS,new FeedGenerator());
            cfg.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
            cfg.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
            cfg.setSharedVariable(Constants.VAR_FETCHER, VariableFetcher.getInstance());
            cfg.setSharedVariable(Constants.VAR_CONFIG, AbcConfig.getInstance());
            cfg.setSharedVariable(Constants.VAR_NEWS_CATEGORIES, NewsCategories.getInstance());
            cfg.setSharedVariable(Constants.VAR_DRIVER_CATEGORIES, DriverCategories.getInstance());
            cfg.setSharedVariable(Constants.VAR_UI_PROPERTY_VALUES, PropertySet.getPropertyValues(Constants.PROPERTY_USER_INTERFACE));
            cfg.setSharedVariable(Constants.VAR_LICENSE_PROPERTY_VALUES, PropertySet.getPropertyValues(Constants.PROPERTY_LICENSE));

            log.info("Inicializace FreeMarkeru je hotova");
        } catch (Exception e) {
            log.error("cannot store shared variable!", e);
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        services.put(PREF_ABC_MONITOR, prefs.getBoolean(PREF_START + PREF_ABC_MONITOR, true));
        services.put(PREF_POOL_MONITOR, prefs.getBoolean(PREF_START + PREF_POOL_MONITOR, true));
        services.put(PREF_FORUM_MAIL_GATEWAY, prefs.getBoolean(PREF_START + PREF_FORUM_MAIL_GATEWAY, true));
        services.put(PREF_RSS_GENERATOR, prefs.getBoolean(PREF_START + PREF_RSS_GENERATOR, true));
        services.put(PREF_RSS_MONITOR, prefs.getBoolean(PREF_START + PREF_RSS_MONITOR, true));
        services.put(PREF_VARIABLE_FETCHER, prefs.getBoolean(PREF_START + PREF_VARIABLE_FETCHER, true));
        services.put(PREF_WEEKLY_EMAILS, prefs.getBoolean(PREF_START + PREF_WEEKLY_EMAILS, true));
        services.put(PREF_WEEKLY_SUMMARY, prefs.getBoolean(PREF_START + PREF_WEEKLY_SUMMARY, true));
        services.put(PREF_UPDATE_DATETOOL, prefs.getBoolean(PREF_START + PREF_UPDATE_DATETOOL, true));
        services.put(PREF_WATCHED_DISCUSSIONS_CLEANER, prefs.getBoolean(PREF_START + PREF_WATCHED_DISCUSSIONS_CLEANER, true));
        services.put(PREF_RSS_OKSYSTEM, prefs.getBoolean(PREF_START + PREF_RSS_OKSYSTEM, true));
        services.put(PREF_RSS_JOBPILOT, prefs.getBoolean(PREF_START + PREF_RSS_JOBPILOT, true));
        services.put(PREF_RSS_JOBSCZ, prefs.getBoolean(PREF_START + PREF_RSS_JOBSCZ, true));
        services.put(PREF_RSS_64BIT, prefs.getBoolean(PREF_START + PREF_RSS_64BIT, true));
        services.put(PREF_UPDATE_STATISTICS, prefs.getBoolean(PREF_START + PREF_UPDATE_STATISTICS, true));
        services.put(PREF_JOB_OFFER_MANAGER, prefs.getBoolean(PREF_START + PREF_JOB_OFFER_MANAGER, false));
        services.put(PREF_USER_SCORE_SETTER, prefs.getBoolean(PREF_START + PREF_USER_SCORE_SETTER, false));
        services.put(PREF_WEB_SERVICES, prefs.getBoolean(PREF_START + PREF_WEB_SERVICES, false));

        delays.put(PREF_POOL_MONITOR, prefs.getInt(PREF_POOL_MONITOR + PREF_DELAY, 60));
        delays.put(PREF_RSS_GENERATOR, prefs.getInt(PREF_RSS_GENERATOR + PREF_DELAY, 60));
        delays.put(PREF_RSS_MONITOR, prefs.getInt(PREF_RSS_MONITOR + PREF_DELAY, 60));
        delays.put(PREF_VARIABLE_FETCHER, prefs.getInt(PREF_VARIABLE_FETCHER + PREF_DELAY, 60));
        delays.put(PREF_WATCHED_DISCUSSIONS_CLEANER, prefs.getInt(PREF_WATCHED_DISCUSSIONS_CLEANER + PREF_DELAY, 60));
        delays.put(PREF_RSS_OKSYSTEM, prefs.getInt(PREF_RSS_OKSYSTEM + PREF_DELAY, 60));
        delays.put(PREF_RSS_JOBPILOT, prefs.getInt(PREF_RSS_JOBPILOT + PREF_DELAY, 60));
        delays.put(PREF_RSS_JOBSCZ, prefs.getInt(PREF_RSS_JOBSCZ + PREF_DELAY, 60));
        delays.put(PREF_RSS_64BIT, prefs.getInt(PREF_RSS_64BIT + PREF_DELAY, 60));
        delays.put(PREF_UPDATE_STATISTICS, prefs.getInt(PREF_UPDATE_STATISTICS + PREF_DELAY, 60));
        delays.put(PREF_JOB_OFFER_MANAGER, prefs.getInt(PREF_JOB_OFFER_MANAGER + PREF_DELAY, 60));

        periods.put(PREF_POOL_MONITOR, prefs.getInt(PREF_POOL_MONITOR + PREF_PERIOD, 60));
        periods.put(PREF_RSS_GENERATOR, prefs.getInt(PREF_RSS_GENERATOR + PREF_PERIOD, 60));
        periods.put(PREF_RSS_MONITOR, prefs.getInt(PREF_RSS_MONITOR + PREF_PERIOD, 60));
        periods.put(PREF_VARIABLE_FETCHER, prefs.getInt(PREF_VARIABLE_FETCHER + PREF_PERIOD, 60));
        periods.put(PREF_WATCHED_DISCUSSIONS_CLEANER, prefs.getInt(PREF_WATCHED_DISCUSSIONS_CLEANER + PREF_PERIOD, 60));
        periods.put(PREF_RSS_OKSYSTEM, prefs.getInt(PREF_RSS_OKSYSTEM + PREF_PERIOD, 60));
        periods.put(PREF_RSS_JOBPILOT, prefs.getInt(PREF_RSS_JOBPILOT + PREF_PERIOD, 60));
        periods.put(PREF_RSS_JOBSCZ, prefs.getInt(PREF_RSS_JOBSCZ + PREF_PERIOD, 60));
        periods.put(PREF_RSS_64BIT, prefs.getInt(PREF_RSS_64BIT + PREF_PERIOD, 60));
        periods.put(PREF_UPDATE_STATISTICS, prefs.getInt(PREF_UPDATE_STATISTICS + PREF_PERIOD, 60));
        periods.put(PREF_JOB_OFFER_MANAGER, prefs.getInt(PREF_JOB_OFFER_MANAGER + PREF_PERIOD, 60));

        endpointUrlServices = prefs.get(PREF_USERS_DEPLOY_PATH, "/users");
    }

    /**
     * Test, whether given service is set to true.
     */
    protected boolean isSet(String name) {
        Boolean aBoolean = services.get(name);
        return aBoolean != null && aBoolean.booleanValue();
    }

    /**
     * @return configuration for delay of given service
     */
    protected int getDelay(String name) {
        Integer integer = delays.get(name);
        if (integer != null)
            return integer * 1000;
        return 60000;
    }

    /**
     * @return configuration for repeat period of given service
     */
    protected int getPeriod(String name) {
        Integer integer = periods.get(name);
        if (integer != null)
            return integer * 1000;
        return 600000;
    }
}
