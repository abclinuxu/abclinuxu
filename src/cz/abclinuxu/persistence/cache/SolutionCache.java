package cz.abclinuxu.persistence.cache;

import com.whirlycott.cache.CacheManager;
import com.whirlycott.cache.CacheException;
import com.whirlycott.cache.Cache;
import org.apache.log4j.Logger;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.view.Solution;
import cz.abclinuxu.servlets.Constants;

import java.util.*;

/**
 * Cache for storing solution comments from recent questions.
 * User: literakl
 * Date: 30.8.2009
 */
public class SolutionCache {
    static Logger log = Logger.getLogger(SolutionCache.class);
    private static SolutionCache instance;
    static {
        instance = new SolutionCache();
    }

    Cache cache;

    /**
     * Stores information about solutions for selected discussion.
     * @param diz discussion item, it must be the question
     * @param solution list of solutions, it can be empty, but not null
     */
    public synchronized void put(Item diz, List<Solution> solutions) {
        if (! Constants.SUBTYPE_QUESTION.equals(diz.getSubType())) {
            log.warn("Discussion " + diz.getId() + " is not a question!");
            return;
        }

        if (solutions.isEmpty()) {
            cache.store(diz.makeLightClone(), Collections.emptyList()); // save some memory
        } else {
            cache.store(diz.makeLightClone(), solutions);
        }
    }

    /**
     * Removes all solutions for selected discussion.
     * @param diz discussion item, it must be the question
     */
    public synchronized boolean remove(Item diz) {
        return cache.remove(diz) != null;
    }

    /**
     * Gets list of solutions for given question. Returns null if this information is not cached.
     * @param diz discussion item, it must be the question
     * @return list of solutions or null
     */
    public List<Solution> get(Item diz) {
        return (List<Solution>) cache.retrieve(diz);
    }

    /**
     * Clears the cache.
     */
    public synchronized void clear() {
        cache.clear();
    }

    /**
     * Returns singleton of this class.
     * @return singleton for this cache
     */
    public static SolutionCache getInstance() {
        return instance;
    }

    private SolutionCache() {
        try {
            cache = CacheManager.getInstance().getCache("solutions");
        } catch (CacheException e) {
            log.fatal("Cannot start cache!", e);
        }
    }
}