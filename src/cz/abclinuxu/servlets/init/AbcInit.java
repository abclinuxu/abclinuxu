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
import javax.servlet.http.*;
import javax.servlet.ServletException;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.velocity.app.Velocity;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.scheduler.jobs.*;
import cz.abclinuxu.scheduler.Scheduler;

/**
 * This servlet initializes Log4J
 */
public class AbcInit extends HttpServlet {
    static Category log = Category.getInstance(AbcInit.class);

    public void init() throws ServletException {
        String path = getServletContext().getRealPath("/");

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
        if ( kernel!=null ) UpdateKernel.setFile(path+kernel);

        String links = getInitParameter("LINKS");
        if ( kernel!=null ) GenerateLinks.setFile(path+links);

        // start scheduler tasks
        startKernelUpdate();
        startLinksUpdate();
        startGenerateLinks();

        log.info("Inicializace je hotova.");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // this servlet shall be never called directly
    }

    /**
     * Update links each six hours, starting at 7:30 AM
     */
    protected void startLinksUpdate() {
        Date now = new Date();
        Calendar next = Calendar.getInstance();
        next.setTime(now);
        next.set(Calendar.HOUR,7);
        next.set(Calendar.MINUTE,30);

        Scheduler.getScheduler().addTask(new UpdateLinks2(),6*60*60*1000,next.getTime().getTime());
    }

    /**
     * Update kernel versions each hour, starting now
     */
    protected void startKernelUpdate() {
        Scheduler.getScheduler().addTask(new UpdateKernel(),1*60*60*1000,0);
    }

    /**
     * Generate file with newest links each hour, starting now
     */
    protected void startGenerateLinks() {
        Scheduler.getScheduler().addTask(new GenerateLinks(),1*60*60*1000,0);
    }
}
