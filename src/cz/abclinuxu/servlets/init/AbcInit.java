/*
 * User: literakl
 * Date: Dec 19, 2001
 * Time: 5:55:35 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.servlets.init;

import java.io.IOException;
import java.util.Date;
import java.util.Calendar;
import java.util.Timer;
import java.util.GregorianCalendar;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.velocity.app.Velocity;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.scheduler.*;
import cz.abclinuxu.servlets.utils.VelocityTemplateSelector;
import cz.abclinuxu.servlets.view.Search;

/**
 * This servlet initializes Log4J
 */
public class AbcInit extends HttpServlet {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcInit.class);

    /** scheduler used by all objects in project */
    static Timer scheduler;

    static {
        scheduler = new Timer(true);
    }

    public void init() throws ServletException {
        String path = getServletContext().getRealPath("/")+"/";

        String file = getInitParameter("CONFIG");
        if ( file!=null ) {
            DOMConfigurator.configure(path+file);
        } else {
            BasicConfigurator.configure();
        }

        file = getInitParameter("VELOCITY");
        if ( file!=null ) {
            try {
                log.info("Inicializuji Velocity");
                Velocity.init(path+file);
            } catch (Exception e) {
                log.error("Nemohu inicializovat Velocity!",e);
            }
        }

        String url = getInitParameter("URL");
        if ( url!=null ) {
            log.info("Inicializuji vrstvu persistence pomoci URL "+url);
            PersistanceFactory.setDefaultUrl(url);
        }

        String kernel = getInitParameter("KERNEL");
        if ( kernel!=null ) UpdateKernel.setFileName(path+kernel);

        String links = getInitParameter("LINKS_TRAFIKA");
        if ( links!=null ) GenerateLinks.setFileNameTrafika(path+links);

        links = getInitParameter("LINKS_ANNECA");
        if ( links!=null ) GenerateLinks.setFileNameAnneca(path+links);

        links = getInitParameter("LINKS_RSS");
        if ( links!=null ) GenerateLinks.setFileNameRSS(path+links);

        String tmp = getInitParameter("TEMPLATES");
        try {
            VelocityTemplateSelector.initialize(path+tmp);
        } catch (Exception e) {
            log.fatal("Cannot initialize template system!", e);
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
     * Fetches some context variables each 30 seconds, starting now
     */
    protected void startFetchingVariables() {
        scheduler.schedule(new VariableFetcher(),0,30*1000);
    }

    /**
     * Monitors article pool and moves articles to new articles, when they are ready.
     * Start two minutes later with 3 minute period.
     */
    protected void startArticlePoolMonitor() {
        scheduler.schedule(new ArticlePoolMonitor(),1*60*1000,3*60*1000);
    }
}
