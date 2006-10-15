/*
 *  Copyright (C) 2005 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.persistence.versioning;

import cz.abclinuxu.exceptions.PersistenceException;
import cz.abclinuxu.persistence.PersistenceFactory;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            throw new PersistenceException("SQL error", e);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            throw new PersistenceException("SQL error", e);
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
        MySqlPersistence persistance = (MySqlPersistence) PersistenceFactory.getPersistance();
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
            throw new PersistenceException("SQL error", e);
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