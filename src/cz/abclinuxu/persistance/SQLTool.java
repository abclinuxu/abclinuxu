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
import cz.abclinuxu.exceptions.PersistanceException;

import java.util.prefs.Preferences;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

/**
 * Thread-safe singleton, that encapsulates SQL commands
 * used outside of Persistance implementations.
 */
public final class SQLTool implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SQLTool.class);

    public static final String PREF_MAX_RECORD_CREATED_OF_ITEM = "max.record.created.of.item";
    public static final String PREF_RELATION_HARDWARE_BY_UPDATED = "hardware.relations.by.updated";
    public static final String PREF_RELATION_SOFTWARE_BY_UPDATED = "software.relations.by.updated";
    public static final String PREF_RELATION_DRIVERS_BY_UPDATED = "driver.relations.by.updated";
    public static final String PREF_RELATION_ARTICLES_BY_CREATED = "article.relations.by.created";
    public static final String PREF_RELATION_DISCUSSIONS_BY_CREATED = "discussion.relations.by.created";
    public static final String PREF_COUNT_HARDWARE = "count.hardware.records";
    public static final String PREF_COUNT_SOFTWARE = "count.software.records";
    public static final String PREF_COUNT_DRIVERS = "count.driver.items";
    public static final String PREF_MAX_POLL = "max.poll";
    public static final String PREF_MAX_USER = "max.user";
    private static final String PREF_RELATION_ARTICLES_WITHIN_PERIOD = "article.relations.within.period";
    private static final String PREF_USERS_WITH_WEEKLY_MAIL = "users.email.weekly";

    private static SQLTool singleton;

    static {
        singleton = new SQLTool();
        ConfigurationManager.getConfigurator().configureMe(singleton);
    }

    private String maxRecordCreatedOfItem, maxPoll, maxUser;
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
     * @throws cz.abclinuxu.exceptions.PersistanceException - sql errors ..
     */
    public Date getMaxCreatedDateOfRecordForItem(Item item) {
        if ( ! item.isInitialized() )
            throw new IllegalStateException("Item is not initialized!");

        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(maxRecordCreatedOfItem);
            statement.setInt(1, item.getId());

            resultSet = statement.executeQuery();
            if ( !resultSet.next() )
                return item.getCreated();
            java.sql.Timestamp max = resultSet.getTimestamp(1);
            return (max==null) ? item.getCreated() : new Date(max.getTime());
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds relations, where child is hardware record ordered by updated property.
     * The order is descendant - the freshest records first. Use offset to skip
     * some record and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findHardwareRelationsByUpdated(int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(relationsHardwareByUpdated);
            statement.setInt(1, offset);
            statement.setInt(2, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Relation relation = (Relation) persistance.findById(new Relation(id));
                result.add(relation);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds relations, where child is software record ordered by updated property.
     * The order is descendant - the freshest records first. Use offset to skip
     * some record and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findSoftwareRelationsByUpdated(int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(relationsSoftwareByUpdated);
            statement.setInt(1, offset);
            statement.setInt(2, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Relation relation = (Relation) persistance.findById(new Relation(id));
                result.add(relation);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds relations, where child is driver item ordered by created property.
     * The order is descendant - the freshest items first. Use offset to skip
     * some items and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findDriverRelationsByUpdated(int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(relationsDriverByUpdated);
            statement.setInt(1, offset);
            statement.setInt(2, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Relation relation = (Relation) persistance.findById(new Relation(id));
                result.add(relation);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds relations, where child is discussion item ordered by created property.
     * The order is descendant - the freshest items first. Use offset to skip
     * some items and count to manage count of returned relations.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findDiscussionRelationsByUpdated(int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(relationsDiscussionByCreated);
            statement.setInt(1, offset);
            statement.setInt(2, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Relation relation = (Relation) persistance.findById(new Relation(id));
                result.add(relation);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds relations, where child is article item ordered by created property.
     * The order is descendant - the freshest items first. Use offset to skip
     * some items and count to manage count of returned relations. Items with
     * created property in future and outside of typical columns are skipped.
     * @return List of initialized relations
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistent storage.
     */
    public List findArticleRelationsByUpdated(int offset, int count) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; PreparedStatement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(relationsArticleByCreated);
            statement.setInt(1, offset);
            statement.setInt(2, count);

            resultSet = statement.executeQuery();
            List result = new ArrayList(count);
            while ( resultSet.next() ) {
                int id = resultSet.getInt(1);
                Relation relation = (Relation) persistance.findById(new Relation(id));
                result.add(relation);
            }
            return result;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
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
            throw new PersistanceException("Chyba pri hledani!",e);
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
            throw new PersistanceException("Chyba pri hledani!",e);
        } finally {
            persistance.releaseSQLResources(con,statement,resultSet);
        }
        return result;
    }

    /**
     * Finds count of hardware records.
     * @return number of hardware records in persistant storage
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistant storage.
     */
    public Integer getHardwareCount() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(countHardware);
            resultSet.next();
            return new Integer(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds count of software records.
     * @return number of software records in persistant storage
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistant storage.
     */
    public Integer getSoftwareCount() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(countSoftware);
            resultSet.next();
            return new Integer(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds count of drivers records.
     * @return number of hardware records in persistant storage
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistant storage.
     */
    public Integer getDriversCount() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(countDrivers);
            resultSet.next();
            return new Integer(resultSet.getInt(1));
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds the last poll. If it is not active, null is returned.
     * @return last initialized poll, if it is active, null otherwise.
     * @throws cz.abclinuxu.exceptions.PersistanceException if there is an error with the underlying persistant storage.
     */
    public Poll findActivePoll() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(maxPoll);
            resultSet.next();

            int id = resultSet.getInt(1);
            Poll poll = (Poll) persistance.findById(new Poll(id));
            return (poll.isClosed()) ?  null : poll;
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Finds maximum id between users.
     */
    public int getMaximumUserId() {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null; Statement statement = null; ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.createStatement();
            resultSet = statement.executeQuery(maxUser);
            if ( ! resultSet.next() )
                return 0;
            Integer id = new Integer(resultSet.getInt(1));
            return id.intValue();
        } catch (SQLException e) {
            throw new PersistanceException("Chyba pri hledani!",e);
        } finally {
            persistance.releaseSQLResources(con,statement,resultSet);
        }
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
        maxPoll = prefs.get(PREF_MAX_POLL,null);
        maxUser = prefs.get(PREF_MAX_USER, null);
        maxRecordCreatedOfItem = prefs.get(PREF_MAX_RECORD_CREATED_OF_ITEM, null);
        relationsArticleByCreated = prefs.get(PREF_RELATION_ARTICLES_BY_CREATED, null);
        relationsArticleWithinPeriod = prefs.get(PREF_RELATION_ARTICLES_WITHIN_PERIOD, null);
        relationsDiscussionByCreated = prefs.get(PREF_RELATION_DISCUSSIONS_BY_CREATED, null);
        relationsDriverByUpdated = prefs.get(PREF_RELATION_DRIVERS_BY_UPDATED, null);
        relationsHardwareByUpdated = prefs.get(PREF_RELATION_HARDWARE_BY_UPDATED, null);
        relationsSoftwareByUpdated = prefs.get(PREF_RELATION_SOFTWARE_BY_UPDATED, null);
        countDrivers = prefs.get(PREF_COUNT_DRIVERS, null);
        countHardware = prefs.get(PREF_COUNT_HARDWARE, null);
        countSoftware = prefs.get(PREF_COUNT_SOFTWARE, null);
        usersWithWeeklyMail = prefs.get(PREF_USERS_WITH_WEEKLY_MAIL, null);
    }
}
