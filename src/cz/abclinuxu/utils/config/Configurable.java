/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 3:36:32 PM
 */
package cz.abclinuxu.utils.config;

import java.util.prefs.Preferences;

/**
 * Callback for configuring classes. See ConfigurationManager
 * and Configurator for details.
 */
public interface Configurable {
    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException ;
}
