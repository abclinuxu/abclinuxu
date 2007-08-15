/*
 *  Copyright (C) 2007 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.data.view;

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
public final class DriverCategories implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DriverCategories.class);

    public static final String PREF_DRIVER_CATEGORIES_FILE = "categories.file";

    static DriverCategories singleton;

    static {
        singleton = new DriverCategories();
    }

    private Map map;
    private String filename;

    /**
     * private constructor, inaccessible outside of this class.
     */
    private DriverCategories() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    /**
     * @return singleton object of this class.
     */
    public static DriverCategories getInstance() {
        return singleton;
    }

    /**
     * Finds DriverCategory for given key. Case-insensitive match is performed.
     * @param key
     * @return
     */
    public static final DriverCategory get(String key) {
        return (DriverCategory) singleton.map.get(key.toUpperCase());
    }

    /**
     * @return All existing categories.
     */
    public static final Collection getAllCategories() {
        Collection collection = singleton.map.values();
        List list = new ArrayList(collection);
        Collections.sort(list);
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
        filename = prefs.get(PREF_DRIVER_CATEGORIES_FILE,null);
        loadCategories();
    }

    /**
     * Loads all categories from external file.
     */
    private void loadCategories() {
        log.info("Loading list of driver categories from file "+filename);
        try {
            Document document = new SAXReader().read(filename);
            String key, name;
            Map aMap = new LinkedHashMap(6, 1.0f);
            List categoryTags = document.getRootElement().elements("category");
            for ( Iterator iter = categoryTags.iterator(); iter.hasNext(); ) {
                Element tagCategory = (Element) iter.next();
                key = tagCategory.elementTextTrim("key");
                name = tagCategory.elementTextTrim("name");
                DriverCategory category = new DriverCategory(key, name);
                aMap.put(key,category);
            }
            map = aMap;
            log.info("Loaded "+map.size()+" driver categories.");
        } catch (Exception e) {
            log.error("Cannot load list of driver categories.", e);
        }
    }
}
