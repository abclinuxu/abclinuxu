/*
 * User: literakl
 * Date: 29.8.2002
 * Time: 11:21:29
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.view.ShowOlder;
import cz.abclinuxu.servlets.utils.VelocityHelper;
import cz.abclinuxu.persistance.Persistance;
import cz.abclinuxu.persistance.PersistanceFactory;

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
    VelocityHelper helper;

    /**
     * Public constructor
     */
    public VariableFetcher() {
        persistance = PersistanceFactory.getPersistance();
        helper = new VelocityHelper();
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
            // put counts into map
            List list = persistance.findByCommand("select count(cislo) from zaznam where typ=1");
            List tmpList = null;

            Object[] objects = (Object[]) list.get(0);
            counter.put("HARDWARE",objects[0]);

            list = persistance.findByCommand("select count(cislo) from zaznam where typ=2");
            objects = (Object[]) list.get(0);
            counter.put("SOFTWARE",objects[0]);

            list = persistance.findByCommand("select count(cislo) from polozka where typ=5");
            objects = (Object[]) list.get(0);
            counter.put("DRIVERS",objects[0]);

            Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
            counter.put("FORUM",new Integer(forum.getContent().size()));

            Category requests = (Category) persistance.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS",new Integer(requests.getContent().size()));

            // find current poll
            list = persistance.findByCommand("select max(cislo) from anketa");
            objects = (Object[]) list.get(0);
            int id = ((Integer)objects[0]).intValue();
            currentPoll = (Poll) persistance.findById(new Poll(id));
            if ( currentPoll.isClosed() )
                currentPoll = null;

            // find new hardware records
            tmpList = new ArrayList(SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_HARDWARE+" limit "+SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                tmpList.add(found);
            }
            newHardware = tmpList;

            // find new software
            tmpList = new ArrayList(SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_SOFTWARE+" limit "+SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                tmpList.add(found);
            }
            newSoftware = tmpList;

            // find new drivers
            tmpList = new ArrayList(SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_DRIVERS+" limit "+SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                tmpList.add(found);
            }
            newDrivers = tmpList;

            // find new articles
            tmpList = new ArrayList(ARTICLE_SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_ARTICLES+" limit "+ARTICLE_SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                tmpList.add(found);
            }
            newArticles = tmpList;

            log.debug("finished fetching variables");
        } catch (Exception e) {
            log.error("Cannot fetch variables!", e);
        }
    }
}
