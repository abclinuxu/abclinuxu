/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 3:36:20 PM
 */
package cz.abclinuxu.utils.config;

import cz.abclinuxu.utils.config.impl.SimpleConfigurator;

/**
 * Manager for configuration of system.
 */
public class ConfigurationManager {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConfigurationManager.class);

    /**
     * ConfigurationManager will try to automatically loads preferences
     * from file specified by this system property.
     */
    public static final String PROPERTY_CONFIG_FILE = "abc.config";

    /** singleton */
    static Configurator configurator;

    /**
     * Gets instance of available configurator.
     */
    public static Configurator getConfigurator() {
        if ( configurator==null )
            init();
        return configurator;
    }

    /**
     * Sets default configurator.
     */
    public static void setConfigurator(Configurator configurator) {
        if ( log.isDebugEnabled() )
            log.debug("Setting "+configurator+" as default configurator.");
        ConfigurationManager.configurator = configurator;
    }

    /**
     * Sets default configurator from system properties.
     */
    protected static synchronized void init() {
        if ( configurator!=null ) return;
        String file = System.getProperty(PROPERTY_CONFIG_FILE, null);
        Configurator aConfigurator = new SimpleConfigurator(file);
        setConfigurator(aConfigurator);
    }
}
