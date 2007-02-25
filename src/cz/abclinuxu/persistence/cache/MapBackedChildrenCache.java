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
package cz.abclinuxu.persistence.cache;

import cz.abclinuxu.data.GenericObject;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

/**
 * LRU Cache. Removes oldest entries.
 * @author literakl
 * @since 25.2.2007
 */
public class MapBackedChildrenCache implements ChildrenCache, Configurable {
    static Logger log = Logger.getLogger(MapBackedChildrenCache.class);

    public static final String PREF_CACHE_SIZE = "size";

    Map<GenericObject, List<Integer>> map;
    int size;

    public MapBackedChildrenCache() {
        ConfigurationManager.getConfigurator().configureAndRememberMe(this);
        map = new LinkedHashMap<GenericObject, List<Integer>>(size, 1.0f, true) {
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > size;
            }
        };
    }

    public List<Integer> get(GenericObject parent) {
        return map.get(parent);
    }

    public List<Integer> remove(GenericObject parent) {
        return map.remove(parent);
    }

    public void put(GenericObject parent, List<Integer> childRelationIds) {
        map.put(parent, childRelationIds);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        int size = prefs.getInt(PREF_CACHE_SIZE, 500);
        log.info("Initializing with cache size " + size);
    }
}
