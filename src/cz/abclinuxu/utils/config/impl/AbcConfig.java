/*
 * User: Leos Literak
 * Date: Jun 2, 2003
 * Time: 10:10:55 PM
 */
package cz.abclinuxu.utils.config.impl;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.prefs.Preferences;
import java.io.File;

/**
 * Replacement for configuration part of AbcInit servlet.
 */
public class AbcConfig implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbcConfig.class);

    static {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(new AbcConfig());
    }

    public static final String PREF_DEPLOY_PATH = "deploy.path";
    public static final String DEFAULT_DEPLOY_PATH = "/home/literakl/abc/deploy";

    static String deployPath;

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        deployPath = prefs.get(PREF_DEPLOY_PATH,DEFAULT_DEPLOY_PATH);
    }

    /**
     * @return directory, where application was deployed
     */
    public static String getDeployPath() {
        return deployPath;
    }


    /**
     * sets directory, where application was deployed
     */
    public static void setDeployPath(String aDeployPath) {
        deployPath = aDeployPath;
    }

    /**
     * If given path is not absolute, we expect, that it is relative
     * to deploy path and concatenates them.
     * @return absolute path
     */
    public static String calculateDeployedPath(String path) {
        if ( path.startsWith(File.separator) )
            return path;
        if ( deployPath.endsWith(File.separator) )
            return deployPath.concat(path);
        else
            return deployPath.concat(File.separator).concat(path);
    }
}
