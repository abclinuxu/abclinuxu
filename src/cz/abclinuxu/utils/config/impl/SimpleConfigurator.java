/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 4:29:14 PM
 */
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.prefs.Preferences;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Default implementation of Configurator without reload functionality.
 */
public class SimpleConfigurator implements Configurator {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SimpleConfigurator.class);
    public static final String PREF_LOCATION = "location";

    /**
     * A file, from which preferences were imported.
     */
    String configurationFile;

    List watched = new ArrayList();

    /**
     * Initializes system preferences from file configurationFile.
     * If file doesn't exist or it cannot be read, this event will
     * be logged and application will be halted.
     */
    public SimpleConfigurator(String file) {
        this.configurationFile = file;
        loadPreferences();
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger("java.util.prefs");
        logger.setLevel(java.util.logging.Level.OFF);
    }

    /**
     * Imports Preferences from given file.
     */
    private void loadPreferences() {
        log.info("Loading preferences from file '"+configurationFile+"'");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(configurationFile);
            Preferences.importPreferences(fis);
            log.info("Preferences successfuly loaded!");
        } catch (IOException e) {
            log.fatal("Cannot read file '"+configurationFile+"'!", e);
            System.exit(1);
        } catch (InvalidPreferencesFormatException e) {
            log.fatal("Preferences file '"+configurationFile+"' is corrupted!", e);
            System.exit(1);
        } finally {
            try {
                if ( fis!=null ) fis.close();
            } catch (IOException e) {
                log.warn(e);
            }
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
        Preferences preferences = Preferences.systemRoot();
        String location = preferences.get(PREF_LOCATION, configurationFile);
        if (!configurationFile.equals(location)) {
            log.info("Oops, configuration file does not match Preferences, reloading");
            loadPreferences();
        }

        String nodePath = configurable.getClass().getName().replace('.', '/');
        Preferences prefs = preferences.node(nodePath);
        configurable.configure(prefs);
    }

    /**
     * Loads preferences for argument and calls configurable.configure(). It also
     * remembers the instance, so reconfiguration is possible.
     */
    public void configureAndRememberMe(Configurable configurable) throws ConfigurationException {
        watched.add(configurable);
        configureMe(configurable);
    }

    /**
     * Reloads preferences from external file and reconfigures instances, that wished it.
     */
    public void reconfigureAll() throws ConfigurationException {
        loadPreferences();
        for ( Iterator iter = watched.iterator(); iter.hasNext(); ) {
            Configurable configurable = (Configurable) iter.next();
            configureMe(configurable);
        }
    }

    public String toString() {
        return "SimpleConfigurator("+configurationFile+")";
    }
}
