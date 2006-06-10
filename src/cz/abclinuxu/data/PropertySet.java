/*
 *  Copyright (C) 2006 Leos Literak
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
package cz.abclinuxu.data;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.servlets.Constants;

import java.util.prefs.Preferences;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Container for various properties.
 * @author literakl
 * @since 7.6.2006
 */
public class PropertySet implements Configurable {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PropertySet.class);

    static final String PREF_UI_VALUES_FILE = "ui.property.values.file";
    static final String PREF_LICENSE_VALUES_FILE = "license.property.values.file";

    static PropertySet instance;
    static {
        instance = new PropertySet();
    }

    private Map properties;

    /**
     * Gets value set for specified property. If property does not have fixed
     * set of values, null is returned.
     * @param id identifier of property as defined in Constants
     * @return map where key is property value identifier and value is its caption
     */
    public static Map getPropertyValues(String id) {
        return (Map) instance.properties.get(id);
    }

    /**
     * Loads value sets for properties.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        String file = prefs.get(PREF_UI_VALUES_FILE, null);
        Map map = loadValues(file);
        properties.put(Constants.PROPERTY_USER_INTERFACE, map);

        file = prefs.get(PREF_LICENSE_VALUES_FILE, null);
        map = loadValues(file);
        properties.put(Constants.PROPERTY_LICENSE, map);
    }

    private PropertySet() {
        properties = new HashMap(4);
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
    }

    private Map loadValues(String file) {
        LinkedHashMap result = new LinkedHashMap();
        log.info("Loading list of property values from file " + file);
        try {
            Document document = new SAXReader().read(file);
            String key, name;
            List categoryTags = document.getRootElement().elements("value");
            for (Iterator iter = categoryTags.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                key = element.elementTextTrim("id");
                name = element.elementTextTrim("caption");
                result.put(key, name);
            }
            log.info("Loaded " + result.size() + " property values.");
            return result;
        } catch (Exception e) {
            log.error("Cannot load list of news categories.", e);
            return null;
        }
    }
}
