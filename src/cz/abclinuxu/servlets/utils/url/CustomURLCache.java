package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.prefs.Preferences;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

/**
 * Cache of custom URLs, that are known.
 * It maps URL to relation id.
 * User: literakl
 * Date: 17.4.2005
 */
public class CustomURLCache implements Configurable {
    private static final String PREF_CACHE_SIZE = "size";

    private static CustomURLCache instance;
    static {
        instance = new CustomURLCache();
    }
    int size;
    Map cache;


    private CustomURLCache() {
        ConfigurationManager.getConfigurator().configureMe(this);
        cache = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        size = prefs.getInt(PREF_CACHE_SIZE, 100);
    }
}
