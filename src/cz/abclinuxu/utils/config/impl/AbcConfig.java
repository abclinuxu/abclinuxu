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
    public static final String PREF_VIEWUSER_PAGINGSIZE = "viewuser.page.size";
    public static final String PREF_VIEWINDEX_DISCUSSIONS_COUNT = "viewindex.discussions.count";
    public static final String PREF_VIEWINDEX_NEWS_COUNT = "viewindex.news.count";

    static String deployPath;
    static int viewUserPageSize, viewIndexDiscussions, viewIndexNewsCount;

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        deployPath = prefs.get(PREF_DEPLOY_PATH, null);
        viewUserPageSize = prefs.getInt(PREF_VIEWUSER_PAGINGSIZE,20);
        viewIndexDiscussions = prefs.getInt(PREF_VIEWINDEX_DISCUSSIONS_COUNT,20);
        viewIndexNewsCount = prefs.getInt(PREF_VIEWINDEX_NEWS_COUNT,5);
    }

    /**
     * @return directory, where application was deployed
     */
    public static String getDeployPath() {
        return deployPath;
    }

    /**
     * If given path is not absolute, we expect, that it is relative
     * to deploy path and concatenates them.
     * @return absolute path
     */
    public static String calculateDeployedPath(String path) {
        if ( path==null ) throw new NullPointerException("path cannot be null!");
        if ( path.startsWith(File.separator) )
            return path;
        if ( deployPath.endsWith(File.separator) )
            return deployPath.concat(path);
        else
            return deployPath.concat(File.separator).concat(path);
    }

    /**
     * @return ViewUser's pageSize attribute.
     * todo use map to configure page size by type. add String parameter to method signature to choose type
     */
    public static int getViewUserPageSize() {
        return viewUserPageSize;
    }

    /**
     * @return default maximum for discussions count on Index page
     */
    public static int getViewIndexDiscussionsCount() {
        return viewIndexDiscussions;
    }

    /**
     * @return default maximum for news count on Index page
     */
    public static int getViewIndexNewsCount() {
        return viewIndexNewsCount;
    }
}
