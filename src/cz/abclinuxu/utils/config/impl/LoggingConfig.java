/*
 * User: Leos Literak
 * Date: Jun 3, 2003
 * Time: 7:09:39 PM
 */
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.Misc;

import java.util.prefs.Preferences;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Configures logging. It shall be one of the first
 * things to be called.
 * @todo: use -Dlog4j.configuration=log4j.xml everywhere
 * or look at jakarta logging for better solution.
 */
public class LoggingConfig implements Configurable {
    static boolean hasBeenConfigured = false;

    /** location of Log4J configuration file */
    public static final String PREF_FILE = "file";

    /**
     * If logging has not been configured yet, uses
     * fail-safe default to console.
     */
    public LoggingConfig() {
        if ( !hasBeenConfigured )
            BasicConfigurator.configure();
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        if ( hasBeenConfigured )
            return;
        String file = prefs.get(PREF_FILE,null);
        if ( !Misc.empty(file) ) {
            DOMConfigurator.configure(file);
            hasBeenConfigured = true;
        }
    }
}
