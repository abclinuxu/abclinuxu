/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 4:29:14 PM
 */
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.Misc;

import java.util.prefs.Preferences;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.StringTokenizer;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Default implementation of Configurator without reload functionality.
 */
public class SimpleConfigurator implements Configurator {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleConfigurator.class);

    /**
     * A file, from which preferences were imported.
     */
    String configurationFile;

    /**
     * Initializes system preferences from file configurationFile.
     * If file doesn't exist or it cannot be read, this event will
     * be logged and application will be halted.
     */
    public SimpleConfigurator(String file) {
        this.configurationFile = file;
        if ( file==null || file.length()==0 ) {
            log.fatal("You must set property "+ConfigurationManager.PROPERTY_CONFIG_FILE+"!");
            System.exit(1);
        }

        log.info("Loading preferences from file '"+file+"'");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Preferences.importPreferences(fis);
            log.info("Preferences successfuly loaded!");
            log.debug("Autoconfig starts ..");
            autoConfigure();
            log.debug("Finished autoconfig.");
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger("java.util.prefs");
            logger.setLevel(java.util.logging.Level.OFF);
        } catch (IOException e) {
            log.fatal("Cannot read file '"+file+"'!",e);
            System.exit(1);
        } catch (InvalidPreferencesFormatException e) {
            log.fatal("Preferences file '"+file+"' is corrupted!",e);
            System.exit(1);
        } finally {
            try { if ( fis!=null) fis.close(); } catch (IOException e) { log.warn(e); }
        }
    }

    /**
     * Configurable, that wishes to be configured, shall
     * get Configurator implementation via ConfigurationManager
     * and then call this method with this as argument.
     * It will call Configurable.configure in response.
     */
    public void configureMe(Configurable configurable) throws ConfigurationException {
        if ( log.isDebugEnabled() )
            log.debug("Configuring class "+configurable.getClass());
        String nodePath = configurable.getClass().getName().replace('.', '/');
        Preferences prefs = Preferences.systemRoot().node(nodePath);
        configurable.configure(prefs);
    }

    /**
     * Finds nodes, that shall be configured automatically at start up.
     */
    void autoConfigure() {
        Preferences root = Preferences.systemRoot();
        Preferences auto = root.node(KEY_AUTOCONFIG);
        String nodes = auto.get(KEY_NODES,"");
        StringTokenizer stk = new StringTokenizer(nodes," ,\t\n\r\f");
        while (stk.hasMoreTokens()) {
            autoConfigureNode(root.node(stk.nextToken()));
        }
    }

    /**
     * Recursively goes through all descendants and configures
     * nodes containing KEY_AUTOCONFIG.
     */
    void autoConfigureNode(Preferences node) {
        String autoClass = node.get(KEY_AUTOCONFIG,null);
        if ( !Misc.empty(autoClass) ) {
            try {
                Class aClass = Class.forName(autoClass);
                Object o = aClass.newInstance();
                if ( o instanceof Configurable )
                    ((Configurable)o).configure(node);
                log.debug("Node "+node.absolutePath()+" configured with class "+autoClass);
            } catch (Exception e) {
                log.error("Configuration problem with class '"+autoClass+"'!", e);
            }
        }
    }

    public String toString() {
        return "SimpleConfigurator("+configurationFile+")";
    }
}
