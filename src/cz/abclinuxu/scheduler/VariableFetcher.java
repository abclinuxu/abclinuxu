/*
 * User: literakl
 * Date: 29.8.2002
 * Time: 11:21:29
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.SQLTool;

import java.util.*;

/**
 * This class is responsible for periodic fetching
 * of template and index variables from database.
 */
public class VariableFetcher extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VariableFetcher.class);

    final static int SIZE = 3;
    final static int ARTICLE_SIZE = 15;

    List newHardware, newSoftware, newDrivers, newArticles;
    Map counter;
    Poll currentPoll;

    Persistance persistance;

    /**
     * Public constructor
     */
    public VariableFetcher() {
        persistance = PersistanceFactory.getPersistance();
        newHardware = new ArrayList(3);
        newSoftware = new ArrayList(3);
        newDrivers = new ArrayList(3);
        newArticles = new ArrayList(ARTICLE_SIZE);
        counter = new HashMap(4);
    }

    /**
     * Map, where each service name is mapped to
     * count of objects of that service.
     */
    public Map getCounter() {
        return counter;
    }

    /**
     * List of few freshest hardware records (relation).
     */
    public List getNewHardware() {
        return newHardware;
    }

    /**
     * List of few freshest software records (relation).
     */
    public List getNewSoftware() {
        return newSoftware;
    }

    /**
     * List of few freshest drivers (relation).
     */
    public List getNewDrivers() {
        return newDrivers;
    }

    /**
     * List of few freshest articles (relation).
     */
    public List getNewArticles() {
        return newArticles;
    }

    /**
     * Actual poll.
     */
    public Poll getCurrentPoll() {
        return currentPoll;
    }

    /**
     * performs lookup of fresh values.
     */
    public void run() {
        log.debug("fetching variables");
        try {
            SQLTool sqlTool = SQLTool.getInstance();
            // put counts into map
            Integer i = sqlTool.getHardwareCount();
            counter.put("HARDWARE",i);
            i = sqlTool.getSoftwareCount();
            counter.put("SOFTWARE",i);
            i = sqlTool.getDriversCount();
            counter.put("DRIVERS",i);
            Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
            counter.put("FORUM",new Integer(forum.getContent().size()));
            Category requests = (Category) persistance.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS",new Integer(requests.getContent().size()));

            currentPoll = sqlTool.findActivePoll();
            newHardware = sqlTool.findHardwareRelationsByUpdated(0,SIZE);
            newSoftware = sqlTool.findSoftwareRelationsByUpdated(0,SIZE);
            newDrivers = sqlTool.findDriverRelationsByUpdated(0,SIZE);
            newArticles = sqlTool.findArticleRelationsByUpdated(0,ARTICLE_SIZE);

            log.debug("finished fetching variables");
        } catch (Exception e) {
            log.error("Cannot fetch variables!", e);
        }
    }
}
