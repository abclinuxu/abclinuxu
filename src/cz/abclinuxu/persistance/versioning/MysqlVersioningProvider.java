package cz.abclinuxu.persistance.versioning;

import cz.abclinuxu.exceptions.PersistanceException;
import cz.abclinuxu.persistance.PersistanceFactory;
import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;


/**
 * This implementation of Versioning will store all versions
 * in Mysql table. Each version will be stored as separate
 * row. The version will be number, starting with 1
 * incremented by 1.
 * User: literakl
 * Date: 27.3.2005
 */
public class MysqlVersioningProvider implements Versioning, Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MysqlVersioningProvider.class);

    private static final String PREF_FIND_LATEST_VERSION = "sql.find.latest.version";
    private static final String PREF_FIND_ALL_VERSIONS = "sql.find.all.versions";
    private static final String PREF_INSERT_VERSION = "sql.insert.version";
    private static final String PREF_FETCH_DOCUMENT = "sql.fetch.document";

    private String latestVersion, allVersions, insertVersion, fetchDocument;

    public MysqlVersioningProvider() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    /**
     * Stores latest version of document into versioning repository.
     *
     * @param document document to be stored
     * @param path     path that uniquely identifies the document
     * @param user     identifier of the user who commited this version
     * @return information about this version
     */
    public VersionInfo commit(String document, String path, String user) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int lastVersion = 0;
        Date commited = new Date(System.currentTimeMillis());
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(latestVersion);
            statement.setString(1, path);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String s = resultSet.getString(1);
                try {
                    lastVersion = Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    log.warn("Document "+path+" contains wrong version: '"+s+"'.");
                }
            }

            statement.close();
            statement = con.prepareStatement(insertVersion);
            statement.setString(1, path);
            String version = Integer.toString(++lastVersion);
            statement.setString(2, version);
            statement.setString(3, user);
            statement.setTimestamp(4, new Timestamp(commited.getTime()));
            statement.setString(5, document);
            statement.executeUpdate();

            VersionInfo info = new VersionInfo();
            info.setVersion(version);
            info.setUser(user);
            info.setCommited(commited);
            return info;
        } catch (SQLException e) {
            throw new PersistanceException("SQL error", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads document identified by path in selected version.
     *
     * @param path    unique identifier of the document
     * @param version version to be fetched
     * @return document with versioning metadata
     * @throws VersionNotFoundException Thrown when either document or specified version doesn't exist.
     */
    public VersionedDocument load(String path, String version) throws VersionNotFoundException {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(fetchDocument);
            statement.setString(1, path);
            statement.setString(2, version);
            resultSet = statement.executeQuery();
            if (!resultSet.next())
                throw new VersionNotFoundException("Cannot find document '"+path+"' in version '"+version+"'!");

            VersionedDocument doc = new VersionedDocument();
            doc.setVersion(resultSet.getString(1));
            doc.setUser(resultSet.getString(2));
            doc.setCommited(new Date(resultSet.getTimestamp(3).getTime()));
            doc.setDocument(resultSet.getString(4));
            return doc;
        } catch (SQLException e) {
            throw new PersistanceException("SQL error", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Loads versioning history for selected document.
     *
     * @param path unique identifier of the document
     * @return list of VersionInfo
     */
    public List getHistory(String path) {
        MySqlPersistance persistance = (MySqlPersistance) PersistanceFactory.getPersistance();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            con = persistance.getSQLConnection();
            statement = con.prepareStatement(allVersions);
            statement.setString(1, path);
            resultSet = statement.executeQuery();

            List history = new ArrayList();
            while (resultSet.next()) {
                VersionInfo info = new VersionInfo();
                info.setVersion(resultSet.getString(1));
                info.setUser(resultSet.getString(2));
                info.setCommited(new Date(resultSet.getTimestamp(3).getTime()));
                history.add(info);
            }

            return history;
        } catch (SQLException e) {
            throw new PersistanceException("SQL error", e);
        } finally {
            persistance.releaseSQLResources(con, statement, resultSet);
        }
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        latestVersion = getValue(PREF_FIND_LATEST_VERSION, prefs);
        allVersions = getValue(PREF_FIND_ALL_VERSIONS, prefs);
        fetchDocument = getValue(PREF_FETCH_DOCUMENT, prefs);
        insertVersion = getValue(PREF_INSERT_VERSION, prefs);
    }

    /**
     * Gets value from preferences. If value is not defined, it dumps info
     * into logs.
     */
    private String getValue(String name, Preferences prefs) {
        String sql = prefs.get(name, null);
        if (sql != null)
            return sql;

        log.fatal("Hodnota SQL prikazu " + name + " nebyla nastavena!");
        return null;
    }
}
