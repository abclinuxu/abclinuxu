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
package cz.abclinuxu.persistence;

import java.util.prefs.Preferences;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.ProxoolException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistence.impl.MySqlPersistence;
import cz.abclinuxu.persistence.cache.TransparentCache;
import cz.abclinuxu.exceptions.PersistenceException;

/**
 * Factory, which select Persistance class
 */
public class PersistenceFactory implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersistenceFactory.class);

    public static final String PREF_DEFAULT_URL = "url.live";
    public static final String PREF_DIRECT_URL = "url.direct";
    public static final String PREF_DEFAULT_TEST_URL = "url.test";
    public static final String PREF_DEFAULT_DEVEL_URL = "url.devel";
    public static final String PREF_PROXOOL = "proxool";
    public static final String PREF_DEFAULT_CACHE = "cache.class";

    public static String defaultUrl = null;
    public static String directUrl = null;
    public static String defaultTestUrl = null;
    public static String defaultDevelUrl = null;
    static Class defaultCache = null;

    static Persistence persistence;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new PersistenceFactory());
    }

    /**
     * Get default persistence.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistence getPersistence() {
        if (persistence != null)
            return persistence;
        return getPersistence(defaultUrl, defaultCache);
    }

    /**
     * Get persistence with direct connection to database using default cache..
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistence getUncachedPersistence() {
        Persistence persistence = new MySqlPersistence(directUrl);
        try {
            persistence.setCache((TransparentCache) defaultCache.newInstance());
        } catch (Exception e) {
            throw new PersistenceException("Cannot use Cache " + defaultCache.toString(), e);
        }
        return persistence;
    }

    /**
     * Get default persistence connected to specific url. If <code>url</code> is null,
     * <code>defaultUrl</code> is used. If persistence has been already initialized,
     * it will be returned regardless on equality with parameters.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static Persistence getPersistence(String url) {
        return getPersistence(url, defaultCache);
    }

    /**
     * Get persistence connected to specific url. If <code>url</code> is null,
     * <code>defaultUrl</code> is used. New instance is always returned, default
     * persistence is not set by this method.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static Persistence getSpecificPersistence(String url) {
        Persistence aPersistence = new MySqlPersistence(url);
        try {
            aPersistence.setCache((TransparentCache) defaultCache.newInstance());
        } catch (Exception e) {
            throw new PersistenceException("Cannot use Cache " + defaultCache.toString(), e);
        }
        return aPersistence;
    }

    /**
     * Get default persistence with custom cache. If persistence has been already initialized,
     * it will be returned regardless on equality with parameters.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistence getPersistance(Class cache) {
        return getPersistence(defaultUrl, cache);
    }

    /**
     * Get default persistence connected to specific url and using specified Cache.
     * If <code>url</code> is null, <code>defaultUrl</code> is used.
     * If persistence has been already initialized, it will be returned
     * regardless on equality with parameters.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static synchronized Persistence getPersistence(String url, Class cache) {
        if (persistence != null)
            return persistence;
        if (url == null)
            url = defaultUrl;

        persistence = new MySqlPersistence(url);
        try {
            persistence.setCache((TransparentCache) cache.newInstance());
        } catch (Exception e) {
            throw new PersistenceException("Cannot use Cache " + defaultCache.toString(), e);
        }
        return persistence;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        defaultUrl = prefs.get(PREF_DEFAULT_URL,null);
        directUrl = prefs.get(PREF_DIRECT_URL, null);
        defaultTestUrl = prefs.get(PREF_DEFAULT_TEST_URL,null);
        defaultDevelUrl = prefs.get(PREF_DEFAULT_DEVEL_URL, null);
        String defaultCacheClassName = prefs.get(PREF_DEFAULT_CACHE, null);

        if ( defaultUrl==null )
            throw new ConfigurationException("You must provide valid JDBC URL!");
        if ( defaultCacheClassName==null )
            throw new ConfigurationException("You must provide valid cache class name!");

        try {
            defaultCache = Class.forName(defaultCacheClassName);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("You must provide valid cache class name!", e);
        }

        String tmp = prefs.get(PREF_PROXOOL,null);
        if ( ! Misc.empty(tmp) ) {
            String path = AbcConfig.calculateDeployedPath(tmp);
            try {
                JAXPConfigurator.configure(path,false);
                Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
            } catch (ProxoolException e) {
                log.error("Cannot configure proxool with '"+path+"'!", e);
            } catch (ClassNotFoundException e) {
                log.error("Add proxool jar to your classpath!", e);
            }
        }
    }

    /**
     * Closes database connection and logs any errors
     */
    public static void releaseSQLResources(Connection con, Statement statement, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (Exception e) {
            log.warn("Problems while closing ResultSet!", e);
        }
        try {
            if (statement != null)
                statement.close();
        } catch (Exception e) {
            log.warn("Problems while closing statement!", e);
        }
        try {
            if (con != null)
                con.close();
        } catch (Exception e) {
            log.warn("Problems while closing connection to database!", e);
        }
    }

    /**
     * Closes database connection and logs any errors
     */
    public static void releaseSQLResources(Connection con, Statement[] statements, ResultSet[] rs) {
        if (rs != null)
            for (ResultSet resultSet : rs) {
                try {
                    if (resultSet != null)
                        resultSet.close();
                } catch (Exception e) {
                    log.warn("Problems while closing ResultSet!", e);
                }
            }

        if (statements != null)
            for (Statement statement : statements) {
                try {
                    if (statement != null)
                        statement.close();
                } catch (Exception e) {
                    log.warn("Problems while closing statement!", e);
                }
            }

        try {
            if (con != null)
                con.close();
        } catch (Exception e) {
            log.warn("Problems while closing connection to database!", e);
        }
    }
}
