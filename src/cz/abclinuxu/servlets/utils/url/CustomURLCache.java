/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.servlets.utils.url;

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.LRUMap;
import cz.abclinuxu.data.Relation;

import java.util.prefs.Preferences;
import java.util.Map;
import java.util.Collections;

/**
 * Cache of mapping custom URLs to relations.
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
        cache = Collections.synchronizedMap(new LRUMap(size));
    }

    /**
     * @return singleton of this class
     */
    public static CustomURLCache getInstance() {
        return instance;
    }

    /**
     * Stores relation with custom url into cache.
     * @param relation
     */
    public void put(Relation relation) {
        if (! relation.isInitialized())
            return;
        if (relation.getUrl() == null)
            return;
        cache.put(relation.getUrl(), relation.makeLightClone());
    }

    /**
     * Finds relation by URL. If this method returns null, it does not mean that
     * this URL is not mapped to any relation. It might be not cached yet.
     * @param url URl starting with /
     * @return unitialized relation or null
     */
    public Relation get(String url) {
        Relation relation = (Relation) cache.get(url);
        if (relation != null)
            relation = (Relation) relation.makeLightClone();
        return relation;
    }

    /**
     * Removes relation with given custom url.
     * @param url
     * @return originally stored (uninitialized) relation or null
     */
    public Relation remove(String url) {
        return (Relation) cache.remove(url);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        size = prefs.getInt(PREF_CACHE_SIZE, 100);
    }
}
