/*
 * User: Leos Literak
 * Date: Jun 21, 2003
 * Time: 11:42:59 AM
 */
package cz.abclinuxu.persistance;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.User;
import cz.abclinuxu.AbcException;

import java.util.prefs.Preferences;
import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.sql.*;

/**
 * Thread-safe singleton, that encapsulates SQL commands
 * used outside of Persistance implementations.
 */
public final class SQLTool implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQLTool.class);

    public static final String PREF_MAX_RECORD_CREATED_OF_ITEM = "max.record.created.of.item";
    private static final String DEFAULT_MAX_RECORD_CREATED_OF_ITEM = "select max(vytvoreno) from relace R left join zaznam Z on Z.cislo=R.potomek where R.typ_predka='P' and R.predek=";
    public static final String PREF_RELATION_HARDWARE_BY_UPDATED = "hardware.relations.by.updated";
    private static final String DEFAULT_RELATION_HARDWARE_BY_UPDATED = "select R.cislo from zaznam Z, relace R where typ=1 and Z.cislo=R.potomek and typ_potomka='Z' order by zmeneno desc";
    public static final String PREF_RELATION_SOFTWARE_BY_UPDATED = "software.relations.by.updated";
    private static final String DEFAULT_RELATION_SOFTWARE_BY_UPDATED = "select R.cislo from zaznam Z, relace R where typ=2 and Z.cislo=R.potomek and typ_potomka='Z' order by zmeneno desc";
    public static final String PREF_RELATION_DRIVERS_BY_UPDATED = "driver.relations.by.updated";
    private static final String DEFAULT_RELATION_DRIVERS_BY_UPDATED = "select R.cislo from polozka P, relace R where typ=5 and P.cislo=R.potomek and typ_potomka='P' order by zmeneno desc";
    public static final String PREF_RELATION_ARTICLES_BY_CREATED = "article.relations.by.created";
    private static final String DEFAULT_RELATION_ARTICLES_BY_CREATED = "select R.cislo from polozka P, relace R where R.predchozi in (2,3,4,5,6,251,5324,8546,12448) and R.typ_potomka='P' and P.typ=2 and P.cislo=R.potomek and P.vytvoreno<now() order by vytvoreno desc";
    public static final String PREF_RELATION_DISCUSSIONS_BY_CREATED = "discussion.relations.by.created";
    private static final String DEFAULT_RELATION_DISCUSSIONS_BY_CREATED = "select R.cislo from relace R left join polozka P on P.cislo=R.potomek where R.typ_potomka='P' and R.typ_predka='K' and P.typ=3 order by P.vytvoreno desc";
    public static final String PREF_COUNT_HARDWARE = "count.hardware.records";
    private static final String DEFAULT_COUNT_HARDWARE = "select count(cislo) from zaznam where typ=1";
    public static final String PREF_COUNT_SOFTWARE = "count.software.records";
    private static final String DEFAULT_COUNT_SOFTWARE = "select count(cislo) from zaznam where typ=2";
    public static final String PREF_COUNT_DRIVERS = "count.driver.items";
    private static final String DEFAULT_COUNT_DRIVERS = "select count(cislo) from polozka where typ=5";
    public static final String PREF_MAX_POLL = "max.poll";
    private static final String DEFAULT_MAX_POLL = "select max(cislo) from anketa";
    private static final String PREF_RELATION_ARTICLES_WITHIN_PERIOD = "article.relations.within.period";
    private static final String DEFAULT_RELATION_ARTICLES_WITHIN_PERIOD = "select R.cislo from relace R left join polozka P on R.potomek=P.cislo where R.typ_potomka='P' and R.predchozi in (2,3,4,5,6,251,5324,8546,12448) and P.typ=2 and P.vytvoreno>? and P.vytvoreno<? order by vytvoreno asc";
    private static final String PREF_USERS_WITH_WEEKLY_MAIL = "users.email.weekly";
    private static final String DEFAULT_USERS_WITH_WEEKLY_MAIL = "select cislo from uzivatel where data like '%<communication><email valid=\"yes\">%<weekly_summary>yes%'";

    private static SQLTool singleton;

    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureMe(singleton);
    }

    private String maxRecordCreatedOfItem, maxPoll;
    private String relationsHardwareByUpdated, relationsSoftwareByUpdated, relationsDriverByUpdated;
    private String relationsArticleByCreated, relationsArticleWithinPeriod, relationsDiscussionByCreated;
    private String countHardware, countSoftware, countDrivers;
    private String usersWithWeeklyMail;


    /**
     * Returns singleton of SQLTool.
     */
    public static SQLTool getInstance() {
        return singleton;
    }

    /**
     * Finds maximum value of created property of records belonging to given item.
     * If the item doesn't have any associated records, its created property is
     * returned. Argument shall be initialized.
     * @throws PersistanceException - sql errors ..
     */
    public Date getMaxCreatedDateOfRecordForItem(Item item) {
        if ( ! item.isInitialized() )
            throw new IllegalStateException("Item is not initialized!");
        Persistance persistance = PersistanceFactory.getPersistance();
        List objects = persistance.findByCommand(maxRecordCreatedOfItem+item.getId());
        java.sql.Timestamp max = (java.sql.Timestamp) ((Object[])objects.get(0))[0];
        if ( max==null )
            return item.getCreated();
        else
            return new Date(max.getTime());
    }

    /**
     * Finds relations, where child is hardware record ordered by updated property.
     * The order is descendant - the freshest records first. Use offset to skip
     * some record and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findHardwareRelationsByUpdated(int offset, int count) {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(relationsHardwareByUpdated+" limit "+offset+","+count);
        List result = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            int id = ((Integer)((Object[]) iter.next())[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }
        return result;
    }

    /**
     * Finds relations, where child is software record ordered by updated property.
     * The order is descendant - the freshest records first. Use offset to skip
     * some record and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findSoftwareRelationsByUpdated(int offset, int count) {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(relationsSoftwareByUpdated+" limit "+offset+","+count);
        List result = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            int id = ((Integer)((Object[]) iter.next())[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }
        return result;
    }

    /**
     * Finds relations, where child is driver item ordered by created property.
     * The order is descendant - the freshest items first. Use offset to skip
     * some items and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findDriverRelationsByUpdated(int offset, int count) {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(relationsDriverByUpdated+" limit "+offset+","+count);
        List result = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            int id = ((Integer)((Object[]) iter.next())[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }
        return result;
    }

    /**
     * Finds relations, where child is discussion item ordered by created property.
     * The order is descendant - the freshest items first. Use offset to skip
     * some items and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findDiscussionRelationsByUpdated(int offset, int count) {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(relationsDiscussionByCreated+" limit "+offset+","+count);
        List result = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            int id = ((Integer)((Object[]) iter.next())[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }
        return result;
    }

    /**
     * Finds relations, where child is article item ordered by created property.
     * The order is descendant - the freshest items first. Use offset to skip
     * some items and count to manage count of returned relations. Items with
     * created property in future and outside of typical columns are skipped.
     * @return List of initialized relations
     * @throws PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findArticleRelationsByUpdated(int offset, int count) {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(relationsArticleByCreated+" limit "+offset+","+count);
        List result = new ArrayList(found.size());
        for (Iterator iter = found.iterator(); iter.hasNext();) {
            int id = ((Integer)((Object[]) iter.next())[0]).intValue();
            Relation relation = (Relation) persistance.findById(new Relation(id));
            result.add(relation);
        }
        return result;
    }

    /**
     * Finds relations, where child is an article item with created property, that is inside
     * given time period. Items are sorted by created property in ascendant order.
     * @param from starting point (exclusive) of time period
     * @param until end point (exclusive) of time period
     * @return List of initialized relations
     */
    public List findArticleRelationsWithinPeriod(Date from, Date until) throws PersistanceException {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        List result;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(relationsArticleWithinPeriod);
            statement.setDate(1,new java.sql.Date(from.getTime()));
            statement.setDate(2,new java.sql.Date(until.getTime()));

            resultSet = statement.executeQuery();
            result = new ArrayList();
            while ( resultSet.next() ) {
                Relation relation = new Relation(resultSet.getInt(1));
                result.add(persistance.findById(relation));
            }
        } catch (SQLException e) {
            log.error("Chyba pri hledani podle "+relationsArticleWithinPeriod,e);
            throw new PersistanceException("Chyba pri hledani!",AbcException.DB_FIND);
        } finally {
            persistance.releaseSQLResources(con,statement,resultSet);
        }
        return result;
    }

    /**
     * Finds users, that have active email and have subscribed weekly email.
     * @return list of Integers of user ids.
     */
    public List findUsersWithWeeklyMail() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        List result;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(usersWithWeeklyMail);
            result = new ArrayList();
            while ( resultSet.next() ) {
                Integer id = new Integer(resultSet.getInt(1));
                result.add(id);
            }
        } catch (SQLException e) {
            log.error("Chyba pri hledani podle "+relationsArticleWithinPeriod,e);
            throw new PersistanceException("Chyba pri hledani!",AbcException.DB_FIND);
        } finally {
            persistance.releaseSQLResources(con,statement,resultSet);
        }
        return result;
    }

    /**
     * Finds count of hardware records.
     * @return number of hardware records in persistant storage
     * @throws PersistanceException if there is an error with the underlying persistant storage.
     */
    public Integer getHardwareCount() {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(countHardware);
        return ((Integer)((Object[]) found.get(0))[0]);
    }

    /**
     * Finds count of software records.
     * @return number of software records in persistant storage
     * @throws PersistanceException if there is an error with the underlying persistant storage.
     */
    public Integer getSoftwareCount() {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(countSoftware);
        return ((Integer)((Object[]) found.get(0))[0]);
    }

    /**
     * Finds count of drivers records.
     * @return number of hardware records in persistant storage
     * @throws PersistanceException if there is an error with the underlying persistant storage.
     */
    public Integer getDriversCount() {
        Persistance persistance = PersistanceFactory.getPersistance();
        List found = persistance.findByCommand(countDrivers);
        return ((Integer)((Object[]) found.get(0))[0]);
    }

    /**
     * Finds the last poll. If it is not active, null is returned.
     * @return last initialized poll, if it is active, null otherwise.
     * @throws PersistanceException if there is an error with the underlying persistant storage.
     */
    public Poll findActivePoll() {
        Persistance persistance = PersistanceFactory.getPersistance();
        List list = persistance.findByCommand("select max(cislo) from anketa");
        Object[] objects = (Object[]) list.get(0);
        int id = ((Integer)objects[0]).intValue();
        Poll poll = (Poll) persistance.findById(new Poll(id));
        if ( poll.isClosed() )
            return null;
        else
            return poll;
    }

    /**
     * Private constructor
     */
    private SQLTool() {
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        maxPoll = prefs.get(PREF_MAX_POLL,DEFAULT_MAX_POLL);
        maxRecordCreatedOfItem = prefs.get(PREF_MAX_RECORD_CREATED_OF_ITEM,DEFAULT_MAX_RECORD_CREATED_OF_ITEM);
        relationsArticleByCreated = prefs.get(PREF_RELATION_ARTICLES_BY_CREATED,DEFAULT_RELATION_ARTICLES_BY_CREATED);
        relationsArticleWithinPeriod = prefs.get(PREF_RELATION_ARTICLES_WITHIN_PERIOD,DEFAULT_RELATION_ARTICLES_WITHIN_PERIOD);
        relationsDiscussionByCreated = prefs.get(PREF_RELATION_DISCUSSIONS_BY_CREATED,DEFAULT_RELATION_DISCUSSIONS_BY_CREATED);
        relationsDriverByUpdated = prefs.get(PREF_RELATION_DRIVERS_BY_UPDATED,DEFAULT_RELATION_DRIVERS_BY_UPDATED);
        relationsHardwareByUpdated = prefs.get(PREF_RELATION_HARDWARE_BY_UPDATED,DEFAULT_RELATION_HARDWARE_BY_UPDATED);
        relationsSoftwareByUpdated = prefs.get(PREF_RELATION_SOFTWARE_BY_UPDATED,DEFAULT_RELATION_SOFTWARE_BY_UPDATED);
        countDrivers = prefs.get(PREF_COUNT_DRIVERS,DEFAULT_COUNT_DRIVERS);
        countHardware = prefs.get(PREF_COUNT_HARDWARE,DEFAULT_COUNT_HARDWARE);
        countSoftware = prefs.get(PREF_COUNT_SOFTWARE,DEFAULT_COUNT_SOFTWARE);
        usersWithWeeklyMail = prefs.get(PREF_USERS_WITH_WEEKLY_MAIL,DEFAULT_USERS_WITH_WEEKLY_MAIL);
    }
}
