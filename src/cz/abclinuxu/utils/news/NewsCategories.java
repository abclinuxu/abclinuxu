/*
 * User: literakl
 * Date: 28.9.2003
 * Time: 19:06:23
 */
package cz.abclinuxu.utils.news;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.*;
import java.util.prefs.Preferences;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Map of available categories.
 */
public final class NewsCategories implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NewsCategories.class);

    public static final String PREF_NEWS_CATEGORIES_FILE = "categories.file";

    static NewsCategories singleton;

    static {
        singleton = new NewsCategories();
    }

    private Map map;
    private String filename;

    /**
     * private constructor, inaccessible outside of this class.
     */
    private NewsCategories() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    /**
     * @return singleton object of this class.
     */
    public static NewsCategories getInstance() {
        return singleton;
    }

    /**
     * Finds NewsCategory for given key. Case-insensitive match is performed.
     * @param key
     * @return
     */
    public static final NewsCategory get(String key) {
        return (NewsCategory) singleton.map.get(key.toUpperCase());
    }

    /**
     * @return All existing categories.
     */
    public static final Collection getAllCategories() {
        Collection collection = singleton.map.values();
        List list = new ArrayList(collection);
        return list;
    }

    /**
     * Gets list of keys of all available categories.
     * @return list of keys
     */
    public static final List listKeys() {
        Set set = singleton.map.keySet();
        return new ArrayList(set);
    }

    /**
     * Configures this instance.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        filename = prefs.get(PREF_NEWS_CATEGORIES_FILE,null);
        loadCategories();
    }

    /**
     * Loads all categories from external file.
     */
    private void loadCategories() {
        log.info("Loading list of news categories from file "+filename);
        try {
            Document document = new SAXReader().read(filename);
            String key, name, desc;
            Map aMap = new LinkedHashMap(11, 1.0f);
            List categoryTags = document.getRootElement().elements("category");
            for ( Iterator iter = categoryTags.iterator(); iter.hasNext(); ) {
                Element tagCategory = (Element) iter.next();
                key = tagCategory.elementTextTrim("key");
                name = tagCategory.elementTextTrim("name");
                desc = tagCategory.elementTextTrim("desc");
                NewsCategory category = new NewsCategory(key, name, desc);
                aMap.put(key,category);
            }
            map = aMap;
            log.info("Loaded "+map.size()+" news categories.");
        } catch (Exception e) {
            log.error("Cannot load list of news categories.", e);
        }
    }
}
