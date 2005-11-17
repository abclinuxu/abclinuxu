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
package cz.abclinuxu.persistance;

import java.util.prefs.Preferences;

import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.ProxoolException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.persistance.impl.MySqlPersistance;
import cz.abclinuxu.exceptions.PersistanceException;

/**
 * Factory, which select Persistance class
 */
public class PersistanceFactory implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersistanceFactory.class);

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

    static Persistance persistance;

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new PersistanceFactory());
    }

    /**
     * Get default persistance.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistance getPersistance() {
        if ( persistance!=null )
            return persistance;
        return getPersistance(defaultUrl, defaultCache);
    }

    /**
     * Get persistance with direct connection to database using default cache..
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistance getUncachedPersistance() {
        Persistance persistance = new MySqlPersistance(directUrl);
        try {
            persistance.setCache((Cache) defaultCache.newInstance());
        } catch (Exception e) {
            throw new PersistanceException("Cannot use Cache " + defaultCache.toString(), e);
        }
        return persistance;
    }

    /**
     * Get default persistance connected to specific url. If <code>url</code> is null,
     * <code>defaultUrl</code> is used. If persistance has been already initialized,
     * it will be returned regardless on equality with parameters.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static Persistance getPersistance(String url) {
        if ( persistance!=null )
            return persistance;
        return getPersistance(url, defaultCache);
    }

    /**
     * Get default persistance with custom cache. If persistance has been already initialized,
     * it will be returned regardless on equality with parameters.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistance getPersistance(Class cache) {
        if ( persistance!=null )
            return persistance;
        return getPersistance(defaultUrl, cache);
    }

    /**
     * Get default persistance connected to specific url and using specified Cache.
     * If <code>url</code> is null, <code>defaultUrl</code> is used.
     * If persistance has been already initialized, it will be returned
     * regardless on equality with parameters.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static synchronized Persistance getPersistance(String url, Class cache) {
        if (persistance!=null)
            return persistance;
        if ( url==null )
            url = defaultUrl;

        persistance = new MySqlPersistance(url);
        try {
            persistance.setCache((Cache) cache.newInstance());
        } catch (Exception e) {
            throw new PersistanceException("Cannot use Cache " + defaultCache.toString(), e);
        }
        return persistance;
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
}
