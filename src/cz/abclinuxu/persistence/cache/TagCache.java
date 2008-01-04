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

import cz.abclinuxu.data.Tag;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.ConfigurationException;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import com.whirlycott.cache.Cache;
import com.whirlycott.cache.CacheManager;
import com.whirlycott.cache.CacheException;

/**
 * @author literakl
 * @since 4.1.2008
 */
public class TagCache implements Configurable {
    static Logger log = Logger.getLogger(TagCache.class);

    private static final String PREF_SIZE = "size";
    private static final String PREF_CONCURRENCY_LEVEL = "concurrency.level";

    private static TagCache instance;
    static {
        instance = new TagCache();
    }

    private int cacheSize, concurrencyLevel;
    private ConcurrentHashMap tagCache;
    Cache mappingCache;

    /**
     * Finds tag with specified identifier or return null, if there is no such tag.
     * The search is case insensitive.
     * @param id tag identifier
     * @return the tag
     */
    public Tag get(String id) {
        return (Tag) tagCache.get(id.toLowerCase());
    }

    /**
     * Stores given tag into the cache.
     * @param tag tag to be stored
     */
    public void put(Tag tag) {
        tagCache.put(tag.getId().toLowerCase(), tag);
    }

    /**
     * Removes given tag from the cache.
     * @param id tag identifier
     */
    public void remove(String id) {
        tagCache.remove(id.toLowerCase());
    }

    /**
     * Clears all tags from the cache.
     */
    public void clear() {
        tagCache.clear();
        mappingCache.clear();
    }

    /**
     * Stores list of tags assigned to given object
     * @param obj some object
     * @param tags its tags, empty list of none tags are assigned
     */
    public void storeAssignedTags(GenericDataObject obj, List<String> tags) {
        mappingCache.store(obj.makeLightClone(), tags);
    }

    /**
     * Loads list of tags assigned to given object
     * @param obj some object
     * @return list of tags, null if this information has not been cached yet
     */
    public List<String> loadAssignedTags(GenericDataObject obj) {
        List<String> tags = (List<String>) mappingCache.retrieve(obj);
        if (tags == null)
            return null;
        else
            return new ArrayList<String>(tags);
    }

    /**
     * Assign tag to given object.
     * @param obj
     * @param id tag identifier
     */
    public void assignTag(GenericDataObject obj, String id) {
        Tag tag = (Tag) tagCache.get(id);
        if (tag == null) {
            log.warn("Cannot increment usage for tag '" + id + "' - not in cache!");
            return;
        }
        tag.setUsage(tag.getUsage() + 1);

        List<String> tags = loadAssignedTags(obj);
        if (tags == null)
            tags = new ArrayList<String>();
        tags.add(id);
        mappingCache.store(obj.makeLightClone(), tags);
    }

    /**
     * Unassign tag from given object.
     * @param obj
     * @param id tag identifier
     */
    public void unassignTag(GenericDataObject obj, String id) {
        Tag tag = (Tag) tagCache.get(id);
        if (tag == null) {
            log.warn("Cannot decrement usage for tag '" + id + "' - not in cache!");
            return;
        }
        tag.setUsage(tag.getUsage() - 1);

        List<String> tags = loadAssignedTags(obj);
        if (tags == null) {
            log.warn("Cannot unassign tag '" + id + "' for " + obj + " - not in cache!");
            return;
        }
        tags.remove(id);
        mappingCache.store(obj.makeLightClone(), tags);
    }

    /**
     * Removes information about tags assigned to given object.
     * @param obj some object
     */
    public void removeAssignment(GenericDataObject obj) {
        mappingCache.remove(obj);
    }

    /**
     * Returns singleton of this class.
     * @return singleton for this cache
     */
    public static TagCache getInstance() {
        return instance;
    }

    private TagCache() {
        ConfigurationManager.getConfigurator().configureMe(this);
        tagCache = new ConcurrentHashMap(cacheSize, 1.0f, concurrencyLevel);
        try {
            mappingCache = CacheManager.getInstance().getCache("tags");
        } catch (CacheException e) {
            log.fatal("Cannot start cache!", e);
        }
    }

    public void configure(Preferences prefs) throws ConfigurationException {
        cacheSize = prefs.getInt(PREF_SIZE, 1000);
        concurrencyLevel = prefs.getInt(PREF_CONCURRENCY_LEVEL, 1000);
    }

    /**
     * Compares two Tags by their usage property in specified order.
     */
    public static class UsageComparator implements Comparator {
        boolean ascending;

        public UsageComparator(boolean ascending) {
            this.ascending = ascending;
        }

        public int compare(Object o1, Object o2) {
            return ((ascending)? 1 : -1) * ((Tag) o1).getUsage() - ((Tag) o2).getUsage();
        }
    }

    /**
     * Compares two Tags by their title property in specified order.
     */
    public static class TitleComparator implements Comparator {
        boolean ascending;

        public TitleComparator(boolean ascending) {
            this.ascending = ascending;
        }

        public int compare(Object o1, Object o2) {
            return ((ascending) ? 1 : -1) * ((Tag) o1).getTitle().compareTo(((Tag) o2).getTitle());
        }
    }

    /**
     * Compares two Tags by their created property in specified order.
     */
    public static class CreationComparator implements Comparator {
        boolean ascending;

        public CreationComparator(boolean ascending) {
            this.ascending = ascending;
        }

        public int compare(Object o1, Object o2) {
            return ((ascending) ? 1 : -1) * ((Tag) o1).getCreated().compareTo(((Tag) o2).getCreated());
        }
    }
}
