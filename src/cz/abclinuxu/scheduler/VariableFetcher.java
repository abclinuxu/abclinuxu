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
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.*;
import java.util.prefs.Preferences;

import org.dom4j.Node;

/**
 * This class is responsible for periodic fetching
 * of template and index variables from database.
 */
public class VariableFetcher extends TimerTask implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VariableFetcher.class);

    static VariableFetcher instance;
    static {
        instance = new VariableFetcher();
        ConfigurationManager.getConfigurator().configureAndRememberMe(instance);
    }

    static final String PREF_HARDWARE_SIZE = "hardware";
    static final String PREF_DRIVERS_SIZE = "drivers";
    static final String PREF_STORIES_SIZE = "stories";

    int hwSize, driversSize,storiesSize;

    List newHardware, newDrivers, newStories;
    Map counter;
    Relation currentPoll;

    long linksLastRun;
    SQLTool sqlTool;

    /**
     * Private constructor
     */
    private VariableFetcher() {
        sqlTool = SQLTool.getInstance();
        linksLastRun = System.currentTimeMillis();
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        hwSize = prefs.getInt(PREF_HARDWARE_SIZE, 3);
        driversSize = prefs.getInt(PREF_DRIVERS_SIZE, 3);
        storiesSize = prefs.getInt(PREF_STORIES_SIZE, 3);
    }

    /**
     * @return singleton of this object
     */
    public static VariableFetcher getInstance() {
        return instance;
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
     * List of few freshest drivers (relation).
     */
    public List getNewDrivers() {
        return newDrivers;
    }

    /**
     * @return List of relations with new stories.
     */
    public List getNewStories() {
        return newStories;
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
     * Actual poll relation.
     */
    public Relation getCurrentPoll() {
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
            Category requests = (Category) persistance.findById(new Category(Constants.CAT_REQUESTS));
            counter.put("REQUESTS",new Integer(requests.getChildren().size()));
            Category news = (Category) persistance.findById(new Category(Constants.CAT_NEWS_POOL));
            counter.put("WAITING_NEWS",new Integer(news.getChildren().size()));
            Item todo = (Item) persistance.findById(new Item(Constants.ITEM_DIZ_TODO));
            synchronized (todo.getData().getRootElement()) {
                Node node = todo.getData().selectSingleNode("//comments");
                if ( node!=null )
                    counter.put("TODO", node.getText());
            }

            currentPoll = sqlTool.findActivePoll();
            Tools.sync(currentPoll);

            Qualifier[] qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, hwSize)};
            List list = sqlTool.findRecordParentRelationsWithType(Record.HARDWARE, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Tools.sync(relation.getChild());
            }
            newHardware = list;

            qualifiers = new Qualifier[]{Qualifier.SORT_BY_UPDATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, driversSize)};
            list = sqlTool.findItemRelationsWithType(Item.DRIVER, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Tools.sync(relation.getChild());
            }
            newDrivers = list;

            qualifiers = new Qualifier[]{Qualifier.SORT_BY_CREATED, Qualifier.ORDER_DESCENDING, new LimitQualifier(0, storiesSize)};
            list = sqlTool.findItemRelationsWithType(Item.BLOG, qualifiers);
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                Relation relation = (Relation) iter.next();
                Tools.sync(relation.getParent());
                Tools.sync(relation.getChild());
            }
            newStories = list;

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
