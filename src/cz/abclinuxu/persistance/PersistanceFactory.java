/*
 * User: literakl
 * Date: Nov 17, 2001
 * Time: 8:21:33 AM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.Map;
import java.util.HashMap;
import java.util.prefs.Preferences;

import org.apache.log4j.xml.DOMConfigurator;
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

    public static final String PREF_DEFAULT_URL = "url";
    public static final String PREF_PROXOOL = "proxool";

    public static String defaultUrl = "jdbc:mysql://localhost/abc?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2";
    public static String defaultTestUrl = "jdbc:mysql://localhost/unit?user=literakl&password=lkaretil&useUnicode=true&characterEncoding=ISO-8859-2";

    static Map instances;
    static {
        instances = new HashMap(3);
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new PersistanceFactory());
    }

    /**
     * Get default persistance.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistance getPersistance() {
        return getPersistance(defaultUrl, LRUCache.class);
    }

    /**
     * Get default persistance connected to specific url. If <code>url</code> is null,
     * <code>defaultUrl</code> is used.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static Persistance getPersistance(String url) {
        return getPersistance(url, LRUCache.class);
    }

    /**
     * Get default persistance with custom cache.
     * @return instance of object, which implements <code>Persistance</code>.
     */
    public static Persistance getPersistance(Class cache) {
        return getPersistance(defaultUrl, cache);
    }

    /**
     * Get default persistance connected to specific url and using specified Cache.
     * If <code>url</code> is null, <code>defaultUrl</code> is used.
     * @return instance of object, which implements <code>Persistance</code> interface
     */
    public static synchronized Persistance getPersistance(String url, Class cache) {
        if ( url==null ) url = defaultUrl;
        Persistance persistance = (Persistance) instances.get(url);
        if ( persistance==null ) {
            persistance = new MySqlPersistance(url);
            try {
                persistance.setCache((Cache)cache.newInstance());
            } catch (Exception e) {
                log.error("Cannot use Cache "+cache.toString(), e);
                persistance.setCache(new DefaultCache());
            }
            instances.put(url,persistance);
        }
        return persistance;
    }

    /**
     * Sets default persistance URL.
     */
    public static void setDefaultUrl(String defaultUrl) {
        PersistanceFactory.defaultUrl = defaultUrl;
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        PersistanceFactory.defaultUrl = prefs.get(PREF_DEFAULT_URL,defaultUrl);

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
