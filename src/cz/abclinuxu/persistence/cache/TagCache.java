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
import cz.abclinuxu.utils.TagTool;
import cz.abclinuxu.utils.comparator.TagCreationComparator;
import cz.abclinuxu.utils.comparator.TagTitleComparator;
import cz.abclinuxu.utils.comparator.TagUsageComparator;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
    private ConcurrentHashMap<String, Tag> tagCache;
    Cache mappingCache;

    /**
     * Finds tag with specified identifier or return null, if there is no such tag.
     * The search is case insensitive.
     * @param id tag identifier
     * @return the tag
     */
    public Tag get(String id) {
        if (id == null)
            return null;
        return tagCache.get(id.toLowerCase());
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
        Tag tag = tagCache.remove(id.toLowerCase());
        // todo unset as parent where used
        // if (tag.getUsage() > 0) todo remove tag from mappingCache
    }

    /**
     * Returns number of tags
     * @return number of tags
     */
    public int size() {
        return tagCache.size();
    }

    /**
     * Clears all tags from the cache.
     */
    public void clear() {
        tagCache.clear();
        mappingCache.clear();
    }

    /**
     * List tags in specified order.
     * @param from offset
     * @param count number of returned tags or -1 for all
     * @param order specified sort field - title, usage, creation time
     * @param ascending true when ascending order is requested
     * @return list of tags according to criteria
     */
    public List<Tag> list(int from, int count, TagTool.ListOrder order, boolean ascending) {
        List<Tag> allTags = new ArrayList<Tag>(tagCache.values());
        Comparator<Tag> comparator;
        if (TagTool.ListOrder.BY_CREATION.equals(order))
            comparator = new TagCreationComparator(ascending);
        else if (TagTool.ListOrder.BY_USAGE.equals(order))
            comparator = new TagUsageComparator(ascending);
        else
            comparator = new TagTitleComparator(ascending);

        Collections.sort(allTags, comparator);
        int toIndex = (count == -1) ? allTags.size() : from + count, size = allTags.size();
        return allTags.subList(from, (toIndex > size) ? size : toIndex);
    }

    /**
     * Stores list of tags assigned to given object
     * @param obj some object
     * @param tags its tags, empty list of none tags are assigned
     */
    public void storeAssignedTags(GenericDataObject obj, List<String> tags) {
        List<String> ids = new ArrayList<String>(tags.size());
        for (String s : tags) {
            ids.add(s.toLowerCase());
        }
        mappingCache.store(obj.makeLightClone(), ids);
    }

    /**
     * Loads list of tags assigned to given object
     * @param obj some object
     * @return list of tags, null if this information has not been cached yet
     */
    public List<String> getAssignedTags(GenericDataObject obj) {
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
        id = id.toLowerCase();
        Tag tag = tagCache.get(id);
        if (tag == null) {
            log.warn("Cannot increment usage for tag '" + id + "' - not in cache!");
            return;
        }
        tag.setUsage(tag.getUsage() + 1);

        List<String> tags = getAssignedTags(obj);
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
        id = id.toLowerCase();
        Tag tag = tagCache.get(id);
        if (tag == null) {
            log.warn("Cannot decrement usage for tag '" + id + "' - not in cache!");
            return;
        }
        tag.setUsage(tag.getUsage() - 1);

        List<String> tags = getAssignedTags(obj);
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
        tagCache = new ConcurrentHashMap<String, Tag>(cacheSize, 1.0f, concurrencyLevel);
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
}
