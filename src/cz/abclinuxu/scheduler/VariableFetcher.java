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

    static List newHW, newSW, newDrv;
    static Map counts;
    static Poll poll;

    Persistance persistance;
    VelocityHelper helper;

    static {
        newHW = new ArrayList(3);
        newSW = new ArrayList(3);
        newDrv = new ArrayList(3);
        counts = new HashMap(4);
    }


    /**
     * Public constructor
     */
    public VariableFetcher() {
        persistance = PersistanceFactory.getPersistance();
        helper = new VelocityHelper();
    }

    /**
     * Map, where each service name is mapped to
     * count of objects of that service.
     */
    public static Map getItemCounts() {
        return counts;
    }

    /**
     * List of few freshest hardware records (relation).
     */
    public static List getNewHardwareList() {
        return newHW;
    }

    /**
     * List of few freshest software records (relation).
     */
    public static List getNewSoftwareList() {
        return newSW;
    }

    /**
     * List of few freshest drivers (relation).
     */
    public static List getNewDriversList() {
        return newDrv;
    }

    /**
     * Actual poll.
     */
    public static Poll getCurrentPoll() {
        return poll;
    }

    /**
     * performs lookup of fresh values.
     */
    public void run() {
        log.debug("fetching variables");
        try {
            // put counts into map
            List list = persistance.findByCommand("select count(cislo) from zaznam where typ=1");
            Object[] objects = (Object[]) list.get(0);
            counts.put("HARDWARE",objects[0]);

            list = persistance.findByCommand("select count(cislo) from zaznam where typ=2");
            objects = (Object[]) list.get(0);
            counts.put("SOFTWARE",objects[0]);

            list = persistance.findByCommand("select count(cislo) from polozka where typ=5");
            objects = (Object[]) list.get(0);
            counts.put("DRIVERS",objects[0]);

            Category forum = (Category) persistance.findById(new Category(Constants.CAT_FORUM));
            counts.put("FORUM",new Integer(forum.getContent().size()));

            Category requests = (Category) persistance.findById(new Category(Constants.CAT_REQUESTS));
            counts.put("REQUESTS",new Integer(requests.getContent().size()));

            // find current poll
            list = persistance.findByCommand("select max(cislo) from anketa");
            objects = (Object[]) list.get(0);
            int id = ((Integer)objects[0]).intValue();
            poll = (Poll) persistance.findById(new Poll(id));

            // find new hardware records
            newHW = new ArrayList(SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_HARDWARE+" limit "+SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                newHW.add(found);
            }

            // find new software
            newSW = new ArrayList(SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_SOFTWARE+" limit "+SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                newSW.add(found);
            }

            // find new drivers
            newDrv = new ArrayList(SIZE);
            list = persistance.findByCommand(ShowOlder.SQL_DRIVERS+" limit "+SIZE);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                objects = (Object[]) iter.next();
                id = ((Integer)objects[0]).intValue();
                Relation found = (Relation) persistance.findById(new Relation(id));
                newDrv.add(found);
            }
            log.debug("stopped fetching variables");
        } catch (Exception e) {
            log.error("Cannot fetch variables!", e);
        }
    }
}
