/*
 * User: literakl
 * Date: Dec 28, 2001
 * Time: 12:25:03 PM
 *
 * Copyright by Leos Literak (literakl@centrum.cz) 2001
 */
package cz.abclinuxu.persistance;

import java.util.*;

import cz.abclinuxu.data.*;
import cz.abclinuxu.servlets.init.AbcInit;

/**
 * Cache of GenericObjects. Only selected classes are cached.
 * @todo Complete rewrite needed. Add Date lru to CacheObject.
 * Use it as LRU, delete objects not accessed within 30 minutes.
 */
public class DefaultCache extends TimerTask implements Cache {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultCache.class);

    Map data;
    long modCount;

    /**
     * Every cached object will be deleted, if it was not synchronized with last 15 minutes.
     * This way we propagate external changes from database to model and
     * reduce consequences of possible errors of Cache.
     */
    public static final int SYNC_INTERVAL = 15*60*1000; // 15 minutes
    /** cached objects, which were not accessed within this interval, will be deleted */
    public static final int MAX_LRU = 5*60*1000; // 5 minutes

    public DefaultCache() {
        data = new HashMap(1000);
        modCount = 0;
        AbcInit.getScheduler().schedule(this,((int)1.5*MAX_LRU),MAX_LRU);
//        AbcInit.getScheduler().schedule(this,((int)0.5*MAX_LRU),3*60*1000);
    }

    /**
     * This method stores copy of object into cache, so it can be retrieved
     * later without queueing database. If <code>obj</code> with same
     * PK is already stored in the cache, it is replaced by new version.
     */
    public void store(GenericObject obj) {
        if ( log.isDebugEnabled() ) { log.debug("Storing "+obj);}
        try {
            if ( obj instanceof Relation ) {
                Relation relation = (Relation) obj;

                // if parent has been changed on already stored relation, we must dismiss this link
                CachedObject cached = (CachedObject)data.get(relation);
                if ( cached!=null ) {
                    Relation original = (Relation) cached.object;
                    if ( ! original.getParent().equals(relation.getParent()) ) {
                        remove(original);
                    }
                }

                data.put(relation,new CachedObject(relation));
                modCount++;

                // add this relation to affected object.
                cached = (CachedObject) data.get(relation.getParent());
                if ( cached!=null ) {
                    cached.object.addContent(relation);
                }
            } else {
                GenericObject key = (GenericObject) obj.getClass().newInstance();
                key.synchronizeWith(obj);
                key.clearContent();

                for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
                    Relation relation = (Relation)iter.next();
                    key.addContent(relation);
                    data.put(relation,new CachedObject(relation));
                    modCount++;
                }

                data.put(key,new CachedObject(key));
                modCount++;
            }
        } catch (Exception e) {
            log.error("Cannot put "+obj+" to cache!",e);
        }
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * clone of cached object, otherwise it returns null.
     * @return cached object
     */
    public GenericObject load(GenericObject obj) {
        if ( log.isDebugEnabled() ) { log.debug("Loading "+obj); }
        try {
            CachedObject found = (CachedObject) data.get(obj);
            if ( found==null ) return null;
            found.touch();

            if ( obj instanceof Relation ) {
                Relation relation = new Relation();
                relation.synchronizeWith(found.object);

                GenericObject tmp = load(relation.getChild());
                if ( tmp!=null ) relation.setChild(tmp);

                tmp = load(relation.getParent());
                if ( tmp!=null ) relation.setParent(tmp);

                return relation;
            } else {
                GenericObject stored = found.object;
                GenericObject clone = (GenericObject) stored.getClass().newInstance();
                clone.synchronizeWith(stored);
                clone.clearContent();

                for (Iterator iter = stored.getContent().iterator(); iter.hasNext();) {
                    Relation relation = ((Relation)iter.next()).cloneRelation();
                    clone.addContent(relation);
                }

                return clone;
            }
        } catch (Exception e) {
            log.error("Cannot get "+obj+" from cache!",e);
            return null;
        }
    }

    /**
     * If <code>obj</code> is deleted from persistant storage,
     * it is wise to delete it from cache too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
        try {
            if ( data.remove(obj)==null ) return;
            modCount++;

            if ( obj instanceof Relation ) {
                GenericObject parent = ((Relation) obj).getParent();
                CachedObject cached = (CachedObject) data.get(parent);
                if ( cached!=null ) {
                    cached.object.getContent().remove(obj);
                }
            }
        } catch (Exception e) {
            log.error("Cannot delete "+obj+" from cache!",e);
        }
    }

    /**
     * Flushes content of Cache, so after this call it will be empty.
     */
    public void clear() {
        data.clear();
        modCount++;
        log.info("Cache cleared upon request.");
    }

    /**
     * When it is time to cleanup cache, this method is invoked. It
     * will scan all CachedObjects, and LRU objects are removed from cache.
     */
    public void run() {
        Runtime runtime = Runtime.getRuntime();
        log.info("cache: "+data.size()+" objects, memory: "+runtime.freeMemory()+"/"+runtime.totalMemory());
        long expectedModCount = modCount;
        long now = System.currentTimeMillis(); // calculated only once to make it as fast as possible

        try {
            Iterator iter = data.values().iterator();
            while ( expectedModCount==modCount && iter.hasNext() ) {
                CachedObject cached = (CachedObject) iter.next();

                if ( cached.lastSync+SYNC_INTERVAL<now ) {
                    iter.remove();
                } else if ( cached.lastAccessed+MAX_LRU<now ) {
                    iter.remove();
                }
            }
            runtime.gc();
            log.info("cache purged: "+data.size()+" objects, memory: "+runtime.freeMemory()+"/"+runtime.totalMemory());
        } catch (Exception e) {
            log.warn("Cache synchronization threw an exception!",e);
        }
    }

    public String getJobName() {
        return "Cache";
    }
}
