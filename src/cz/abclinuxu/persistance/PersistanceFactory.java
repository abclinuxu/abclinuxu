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
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.ProxoolException;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.data.Category;
import cz.abclinuxu.data.GenericObject;

/**
 * Factory, which select Persistance class
 */
public class PersistanceFactory implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersistanceFactory.class);

    public static final String PREF_NO_CHILDREN_FOR_SECTION = "no.children.for.section";
    public static final String PREF_DEFAULT_URL = "url.live";
    public static final String PREF_DEFAULT_TEST_URL = "url.test";
    public static final String PREF_PROXOOL = "proxool";

    public static String defaultUrl = null;
    public static String defaultTestUrl = null;

    static Map instances;
    private static Map noChildren = null;

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
     * Finds out whether it is prohibited to load children for this object.
     * @return true, if Peristance must not load children for genericObject.
     */
    public static boolean isLoadingChildrenForbidden(GenericObject genericObject) {
        return noChildren.get(genericObject)!=null;
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
                persistance.setCache(new LRUCache());
            }
            instances.put(url,persistance);
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

        // content of these sections shall not be loaded!
        PersistanceFactory.noChildren = new HashMap(100, 0.95f);
        Category category = null;
        tmp = prefs.get(PREF_NO_CHILDREN_FOR_SECTION, "");
        StringTokenizer stk = new StringTokenizer(tmp, ",");
        while ( stk.hasMoreTokens() ) {
            String key = PREF_NO_CHILDREN_FOR_SECTION+"."+stk.nextToken();
            String values = prefs.get(key, "");

            StringTokenizer stk2 = new StringTokenizer(values, ",");
            while ( stk2.hasMoreTokens() ) {
                category = new Category(Misc.parseInt(stk2.nextToken(), 0));
                PersistanceFactory.noChildren.put(category, Boolean.TRUE);
            }
        }
    }
}
