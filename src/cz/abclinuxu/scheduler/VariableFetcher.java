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
import cz.abclinuxu.exceptions.PersistanceException;

import java.util.*;

/**
 * This class is responsible for periodic fetching
 * of template and index variables from database.
 */
public class VariableFetcher extends TimerTask {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VariableFetcher.class);

    final static int SIZE = 3;
    final static int ARTICLE_SIZE = 15;

    List newHardware, newSoftware, newDrivers, newArticles, selectedProfiles;
    Map counter;
    Poll currentPoll;

    Calendar profileLastRun;
    SQLTool sqlTool;
    Persistance persistance;

    /**
     * Public constructor
     */
    public VariableFetcher() {
        persistance = PersistanceFactory.getPersistance();
        sqlTool = SQLTool.getInstance();
        newHardware = new ArrayList(SIZE);
        newSoftware = new ArrayList(SIZE);
        newDrivers = new ArrayList(SIZE);
        selectedProfiles = new ArrayList(SIZE);
        newArticles = new ArrayList(ARTICLE_SIZE);
        counter = new HashMap(4);
        profileLastRun = Calendar.getInstance();
        profileLastRun.add(Calendar.DAY_OF_MONTH,-1);
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
     * List of few freshest users.
     */
    public List getSelectedProfiles() {
        return selectedProfiles;
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
            // put counts into map
            Integer i = new Integer(sqlTool.getRecordCount(Record.HARDWARE));
            counter.put("HARDWARE",i);
            i = new Integer(sqlTool.getRecordCount(Record.SOFTWARE));
            counter.put("SOFTWARE",i);
            i = new Integer(sqlTool.getItemCount(Item.DRIVER));
            counter.put("DRIVERS",i);
            Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
            counter.put("FORUM",new Integer(forum.getContent().size()));
            Category requests = (Category) persistance.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS",new Integer(requests.getContent().size()));

            currentPoll = sqlTool.findActivePoll();
            newHardware = sqlTool.findRecordRelationsByUpdated(Record.HARDWARE, 0,SIZE);
            newSoftware = sqlTool.findRecordRelationsByUpdated(Record.SOFTWARE, 0,SIZE);
            newDrivers = sqlTool.findItemRelationsByUpdated(Item.DRIVER, 0,SIZE);
            newArticles = sqlTool.findArticleRelationsByCreated(0,ARTICLE_SIZE);
            updateProfiles();

            log.debug("finished fetching variables");
        } catch (Exception e) {
            log.error("Cannot fetch variables!", e);
        }
    }

    /**
     * Each day randomly selects SIZE profiles.
     */
    private void updateProfiles() {
        Calendar calendar = Calendar.getInstance();
        if ( calendar.get(Calendar.DAY_OF_MONTH)==profileLastRun.get(Calendar.DAY_OF_MONTH) )
            return;
        profileLastRun = calendar;

        int limit = 10;
        int max = sqlTool.getMaximumUserId();
        Random random = new Random();
        selectedProfiles.clear();

        for (int i=0; i<limit && selectedProfiles.size()<3; i++) {
            int id = random.nextInt(max);
            try {
                User user = (User) persistance.findById(new User(id));
                selectedProfiles.add(user);
            } catch (PersistanceException e) {
                // user doesn't exist, lets skip it.
            }
        }
    }
}
