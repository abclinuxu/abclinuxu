/*
 * User: literakl
 * Date: Dec 19, 2001
 * Time: 5:55:35 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.servlets.init;

import java.io.IOException;
import java.io.File;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.velocity.app.Velocity;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.scheduler.*;
import cz.abclinuxu.servlets.utils.template.VelocityTemplateSelector;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.utils.Tools;
import cz.abclinuxu.servlets.view.Search;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.Sorters2;
import cz.abclinuxu.utils.DateTool;
import cz.abclinuxu.data.Category;
import freemarker.template.*;
import freemarker.ext.beans.BeansWrapper;

/**
 * This servlet initializes Log4J
 */
public class AbcInit extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcInit.class);

    /** scheduler used by all objects in project */
    static Timer scheduler;
    static VariableFetcher fetcher;

    static {
        scheduler = new Timer(true);
    }

    public void init() throws ServletException {
        String path = getServletContext().getRealPath("/")+"/";

        String tmp = getInitParameter("LOG4J");
        if ( ! Misc.empty(tmp) ) {
            DOMConfigurator.configure(path+tmp);
        } else {
            BasicConfigurator.configure();
        }

        tmp = getInitParameter("JDBC");
        if ( ! Misc.empty(tmp) ) {
            log.info("Inicializuji vrstvu persistence pomoci URL "+tmp);
            PersistanceFactory.setDefaultUrl(tmp);
        }

        tmp = getInitParameter("VELOCITY");
        if ( ! Misc.empty(tmp) ) {
            try {
                log.info("Inicializuji Velocity");
                Velocity.init(path+tmp);
            } catch (Exception e) {
                log.error("Nemohu inicializovat Velocity!",e);
            }
        }

        fetcher = new VariableFetcher();
        configureFreeMarker(path);

        tmp = getInitParameter("KERNEL");
        if ( ! Misc.empty(tmp) )
            UpdateKernel.setFileName(path+tmp);

        tmp = getInitParameter("LINKS_TRAFIKA");
        if ( ! Misc.empty(tmp) )
            GenerateLinks.setFileNameTrafika(path+tmp);

        tmp = getInitParameter("LINKS_ANNECA");
        if ( ! Misc.empty(tmp) )
            GenerateLinks.setFileNameAnneca(path+tmp);

        tmp = getInitParameter("LINKS_RSS");
        if ( ! Misc.empty(tmp) )
            GenerateLinks.setFileNameRSS(path+tmp);

        tmp = getInitParameter("TEMPLATES");
        try {
            VelocityTemplateSelector.initialize(path+tmp);
        } catch (Exception e) {
            log.fatal("Nemohu inicializovat systém ¹ablon!", e);
        }

        tmp = getInitParameter("INDEX_PATH");
        if ( tmp!=null )
            Search.setIndexPath(path+tmp);

        // start scheduler tasks
        startFetchingVariables();
        startKernelUpdate();
        startLinksUpdate();
        startGenerateLinks();
        startArticlePoolMonitor();

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
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR, 7);
        cal.set(Calendar.MINUTE, 30);

        Date next = cal.getTime();
        Date now = new Date();

        while ( next.before(now) ) {
            cal.add(Calendar.HOUR_OF_DAY,6);
            next = cal.getTime();
        }

        AbcInit.getScheduler().scheduleAtFixedRate(new UpdateLinks(),next,6*60*60*1000);
    }

    /**
     * Update kernel versions each hour, starting after one minute
     */
    protected void startKernelUpdate() {
        scheduler.schedule(new UpdateKernel(),60*1000,1*60*60*1000);
    }

    /**
     * Generate file with newest links each hour, starting 30 seconds later
     */
    protected void startGenerateLinks() {
        scheduler.schedule(new GenerateLinks(),30*1000,1*60*60*1000);
    }

    /**
     * Fetches some context variables each 30 seconds, starting now.
     * It must be called before Freemarker's init.
     */
    protected void startFetchingVariables() {
        scheduler.schedule(fetcher,0,30*1000);
    }

    /**
     * Monitors article pool and moves articles to new articles, when they are ready.
     * Start two minutes later with 3 minute period.
     */
    protected void startArticlePoolMonitor() {
        scheduler.schedule(new ArticlePoolMonitor(),1*60*1000,3*60*1000);
    }

    /**
     * set ups freemarker
     */
    void configureFreeMarker(String path) {
        log.info("Inicializuji FreeMarker");

        VelocityHelper helper = new VelocityHelper();
        Persistance persistance = PersistanceFactory.getPersistance();
        Configuration cfg = Configuration.getDefaultConfiguration();
        BeansWrapper wrapper = BeansWrapper.getDefaultInstance();

        cfg.setDefaultEncoding("ISO-8859-2");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setObjectWrapper(wrapper);
        cfg.setTemplateUpdateDelay(1);
        cfg.setStrictSyntaxMode(true);

        try {
            Category rubriky = (Category) persistance.findById(new Category(Constants.CAT_ARTICLES));
            helper.sync(rubriky.getContent());
            Category abc = (Category) persistance.findById(new Category(Constants.CAT_ABC));
            helper.sync(abc.getContent());
            Category reklama = (Category) persistance.findById(new Category(Constants.CAT_REKLAMA));
            helper.sync(reklama.getContent());
            Category linksCategory = (Category) persistance.findById(new Category(Constants.CAT_LINKS));
            Map links = UpdateLinks.groupLinks(linksCategory,persistance);

            cfg.setSharedVariable(Constants.VAR_RUBRIKY,rubriky.getContent());
            cfg.setSharedVariable(Constants.VAR_ABCLINUXU,abc.getContent());
//            cfg.setSharedVariable(Constants.VAR_REKLAMA,reklama.getContent());
            cfg.setSharedVariable(Constants.VAR_LINKS,links);
            cfg.setSharedVariable(Constants.VAR_TOOL,new Tools());
            cfg.setSharedVariable(Constants.VAR_DATE_TOOL,new DateTool());
            cfg.setSharedVariable(Constants.VAR_SORTER,new Sorters2());
            cfg.setSharedVariable(Constants.VAR_FETCHER,fetcher);

            log.info("Inicializace FreeMarkeru je hotova");
        } catch (TemplateModelException e) {
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
}
