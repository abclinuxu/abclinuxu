/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:21:33 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
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

/**
 * Factory, which select Persistance class
 */
public class PersistanceFactory implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersistanceFactory.class);

    public static final String PREF_DEFAULT_URL = "url.live";
    public static final String PREF_DEFAULT_TEST_URL = "url.test";
    public static final String PREF_PROXOOL = "proxool";

    public static String defaultUrl = null;
    public static String defaultTestUrl = null;

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
        return getPersistance(defaultUrl, LRUCache.class);
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
        return getPersistance(url, LRUCache.class);
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
            log.error("Cannot use Cache "+cache.toString(), e);
            persistance.setCache(new LRUCache());
        }
        return persistance;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        defaultUrl = prefs.get(PREF_DEFAULT_URL,null);
        defaultTestUrl = prefs.get(PREF_DEFAULT_TEST_URL,null);

        if ( defaultUrl==null ) {
            log.fatal("You must provide valid JDBC URL!");
            System.exit(1);
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
