/*
 * User: literakl
 * Date: 29.8.2002
 * Time: 11:21:29
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.persistance.*;
import cz.abclinuxu.persistance.extra.LimitQualifier;
import cz.abclinuxu.persistance.extra.Qualifier;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.Misc;

import java.util.*;

import org.dom4j.Node;

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

    long linksLastRun;
    SQLTool sqlTool;

    /**
     * Public constructor
     */
    public VariableFetcher() {
        sqlTool = SQLTool.getInstance();
        newHardware = new ArrayList(SIZE);
        newSoftware = new ArrayList(SIZE);
        newDrivers = new ArrayList(SIZE);
        newArticles = new ArrayList(ARTICLE_SIZE);
        counter = new HashMap(8,1.0f);
        linksLastRun = System.currentTimeMillis();
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
     * List of the freshest news (relation) limited by user settings.
     */
    public List getFreshNews(Object user) {
        int userLimit = getNumberOfNews(user);
        if ( userLimit>0 ) {
            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, userLimit)};
            List news = SQLTool.getInstance().findNewsRelations(qualifiers);
            return news;
        }
        return Collections.EMPTY_LIST;
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
        Persistance persistance = PersistanceFactory.getPersistance();
        try {
            // put counts into map
            Integer i = new Integer(sqlTool.countRecordRelationsWithType(Record.HARDWARE));
            counter.put("HARDWARE",i);
            i = new Integer(sqlTool.countRecordRelationsWithType(Record.SOFTWARE));
            counter.put("SOFTWARE",i);
            i = new Integer(sqlTool.countItemRelationsWithType(Item.DRIVER));
            counter.put("DRIVERS",i);
            i = new Integer(sqlTool.countItemRelationsWithType(Item.NEWS));
            counter.put("NEWS",i);
            i = new Integer(sqlTool.countDiscussionRelations());
            counter.put("FORUM",i);
            Category requests = (Category) persistance.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS",new Integer(requests.getChildren().size()));
            Category news = (Category) persistance.findById(new Category(Constants.CAT_NEWS_POOL));
            counter.put("WAITING_NEWS",new Integer(news.getChildren().size()));
            Category polls = (Category) persistance.findById(new Category(Constants.CAT_POLLS));
            counter.put("POLLS",new Integer(polls.getChildren().size()));

            currentPoll = sqlTool.findActivePoll();

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, SIZE)};
            newHardware = sqlTool.findRecordRelationsWithType(Record.HARDWARE, qualifiers);
            newSoftware = sqlTool.findRecordRelationsWithType(Record.SOFTWARE, qualifiers);
            newDrivers = sqlTool.findItemRelationsWithType(Item.DRIVER, qualifiers);

            log.debug("finished fetching variables");
        } catch (Exception e) {
            log.error("Cannot fetch variables!", e);
        }
    }

    /**
     * Gets limit of displayed news. If user is not authenticated or he didn't
     * configured this value, default value will be used.
     * @param user
     * @return number of news to be displayed
     */
    private int getNumberOfNews(Object user) {
        int defaultValue = AbcConfig.getNewsCount();
        if ( user==null || !(user instanceof User))
            return defaultValue;
        Node node = ((User)user).getData().selectSingleNode("/data/settings/index_news");
        if ( node==null )
            return defaultValue;
        int count = Misc.parseInt(node.getText(), defaultValue);
        return count;
    }
}
