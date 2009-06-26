package cz.abclinuxu.persistence.cache;

import com.whirlycott.cache.CacheManager;
import com.whirlycott.cache.CacheException;
import com.whirlycott.cache.Cache;
import org.apache.log4j.Logger;
import cz.abclinuxu.data.GenericDataObject;

import java.util.*;

/**
 * Cache for storing most often used monitors.
 * User: literakl
 * Date: 15.2.2009
 */
public class MonitorCache {
    static Logger log = Logger.getLogger(MonitorCache.class);
    private static MonitorCache instance;
    static {
        instance = new MonitorCache();
    }

    Cache cache;

    /**
     * Stores information about user monitoring given document.
     * @param doc monitored document
     * @param uid user
     */
    public void put(GenericDataObject doc, int uid) {
        put(doc, Collections.singleton(uid));
    }

    /**
     * Stores information about users monitoring given document.
     * @param doc monitored document
     * @param uids list of user
     */
    public void put(GenericDataObject doc, Set<Integer> uids) {
        Set<Integer> currentUids = (Set<Integer>) cache.retrieve(doc);
        if (currentUids == null) {
            if (uids.isEmpty()) {
                cache.store(doc.makeLightClone(), Collections.emptySet()); // save some memory
                return;
            }

            currentUids = new HashSet<Integer>(uids.size() + 2, 1.0f);
            cache.store(doc.makeLightClone(), currentUids);
        } else if (currentUids.isEmpty()) {
            currentUids = new HashSet<Integer>(uids.size() + 2, 1.0f);
            cache.store(doc.makeLightClone(), currentUids);
        }
        currentUids.addAll(uids);
    }

    /**
     * Removes information about user monitoring given document.
     * @param doc monitored document
     * @param uid user
     */
    public boolean remove(GenericDataObject doc, int uid) {
        Set<Integer> users = get(doc);
        if (users == null)
            return false;

        return users.remove(uid);
    }

    /**
     * Gets set of users (id) monitoring requested document. Returns null if this information is not cached.
     * @param doc monitored document
     * @return set of user id or null
     */
    public Set<Integer> get(GenericDataObject doc) {
        return (Set<Integer>) cache.retrieve(doc);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Returns singleton of this class.
     * @return singleton for this cache
     */
    public static MonitorCache getInstance() {
        return instance;
    }

    private MonitorCache() {
        try {
            cache = CacheManager.getInstance().getCache("monitors");
        } catch (CacheException e) {
            log.fatal("Cannot start cache!", e);
        }
    }
}
