/*
 * User: literakl
 * Date: Dec 19, 2001
 * Time: 5:55:35 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.servlets.init;

import cz.abclinuxu.data.Category;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.scheduler.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.email.monitor.InstantSender;
import cz.abclinuxu.utils.email.forum.CommentSender;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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
    public static final String PREF_START_VARIABLE_FETCHER = "start.variable.fetcher";
    public static final String PREF_START_ARTICLE_POOL_MONITOR = "start.article.pool.monitor";
    public static final String PREF_START_ABC_MONITOR = "start.abc.monitor";
    public static final String PREF_START_FORUM_MAIL_GATEWAY = "start.forum.mail.gateway";
    public static final String PREF_START_WEEKLY_EMAILS = "start.weekly.emails";

    /** scheduler used by all objects in project */
    static Timer scheduler;
    static VariableFetcher fetcher;

    static {
        scheduler = new Timer(true);
    }

    private Map services = new HashMap(10, 0.9f);


    public void init() throws ServletException {
        ConfigurationManager.getConfigurator().configureMe(this);

        fetcher = new VariableFetcher();
        String path = getServletContext().getRealPath("/")+"/";
        configureFreeMarker(path);
        String tmp = getInitParameter("TEMPLATES");
        try {
            FMTemplateSelector.initialize(path+tmp);
        } catch (Exception e) {
            log.fatal("Nemohu inicializovat systém ¹ablon!", e);
        }

        // start scheduler tasks
        startFetchingVariables();
        startKernelUpdate();
        startLinksUpdate();
        startGenerateLinks();
        startArticlePoolMonitor();
        startSendingWeeklyEmails();
        startObjectMonitor();
        startForumSender();

        log.info("Inicializace je hotova.");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // this servlet shall be never called directly
    }

    /**
     * @return instance of scheduler
     */
    public static Timer getScheduler() {
        return scheduler;
    }

    /**
     * Update links, each six hours, starting at 6:30+k*6, where k is minimal non-negativ integer
     */
    protected void startLinksUpdate() {
        if (!isSet(PREF_START_RSS_MONITOR)) {
            log.info("RSS monitor configured not to run");
            return;
        }
        log.info("Scheduling RSS monitor");
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR, 7);
        cal.set(Calendar.MINUTE, 30);

        Date next = cal.getTime();
        Date now = new Date();

        while ( next.before(now) ) {
            cal.add(Calendar.HOUR_OF_DAY,6);
            next = cal.getTime();
        }

        scheduler.scheduleAtFixedRate(new UpdateLinks(),next,6*60*60*1000);
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
        scheduler.schedule(new UpdateKernel(),60*1000,1*60*60*1000);
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
        scheduler.schedule(new GenerateLinks(),30*1000,1*60*60*1000);
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
        scheduler.schedule(fetcher,0,30*1000);
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
        scheduler.schedule(new ArticlePoolMonitor(),1*60*1000,3*60*1000);
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
        calendar.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        calendar.set(Calendar.HOUR_OF_DAY,12);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        if ( new Date().after(calendar.getTime()) ) {
            calendar.add(Calendar.DAY_OF_WEEK,7);
        }

        scheduler.scheduleAtFixedRate(new WeeklyEmail(),calendar.getTime(),7*24*60*60*1000);
    }

    /**
     * Sets some commonly used variables in freemarker templates.
     */
    public static void setSharedVariables() {
        try {
            setSharedVariableLinks();
        } catch (Exception e) {
            log.error("cannot store shared variable!", e);
        }
    }

    /**
     * Sets LINKS variable in freemarker templates.
     */
    public static void setSharedVariableLinks() {
        Persistance persistance = PersistanceFactory.getPersistance();
        Configuration cfg = Configuration.getDefaultConfiguration();

        try {
            Category linksCategory = (Category) persistance.findById(new Category(Constants.CAT_LINKS));
            Map links = UpdateLinks.groupLinks(linksCategory, persistance);
            cfg.setSharedVariable(Constants.VAR_LINKS, links);
        } catch (Exception e) {
            log.error("cannot store shared variable!", e);
        }
    }

    /**
     * set ups freemarker
     */
    void configureFreeMarker(String path) {
        log.info("Inicializuji FreeMarker");

        Configuration cfg = Configuration.getDefaultConfiguration();
        cfg.setDefaultEncoding("ISO-8859-2");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        cfg.setObjectWrapper(BeansWrapper.getDefaultInstance());
        cfg.setTemplateUpdateDelay(1);
        cfg.setStrictSyntaxMode(true);

        setSharedVariables();
        try {
            cfg.setSharedVariable(Constants.VAR_TOOL,new Tools());
            cfg.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
            cfg.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
            cfg.setSharedVariable(Constants.VAR_FETCHER,fetcher);

            log.info("Inicializace FreeMarkeru je hotova");
        } catch (Exception e) {
            log.error("cannot store shared variable!", e);
        }

        String tmp = getInitParameter("FREEMARKER");
        if ( ! Misc.empty(tmp) ) {
            try {
                cfg.setDirectoryForTemplateLoading(new File(path,tmp));
            } catch (IOException e) {
                log.error("Nemohu inicializovat FreeMarker!",e);
            }
        }
    }

    /**
     * legacy method for compatibility with previous velocity based implementation
     */
    public static VariableFetcher getFetcher() {
        return fetcher;
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
        services.put(PREF_START_VARIABLE_FETCHER, new Boolean(prefs.getBoolean(PREF_START_VARIABLE_FETCHER, true)));
        services.put(PREF_START_WEEKLY_EMAILS, new Boolean(prefs.getBoolean(PREF_START_WEEKLY_EMAILS, true)));
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
