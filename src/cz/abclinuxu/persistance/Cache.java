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
import cz.abclinuxu.scheduler.Task;
import cz.abclinuxu.scheduler.Scheduler;

/**
 * Cache of GenericObjects. Only selected classes are cached.
 * @todo Complete rewrite needed. Add Date lru to CacheObject.
 * Use it as LRU, delete objects not accessed within 30 minutes.
 */
public class Cache implements Task {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Cache.class);

    Map data;
    long modCount;

    /**
     * Every cached object will be deleted, if it was not synchronized with last hour.
     * This way we propagate external changes from database to model and
     * reduce consequences of possible errors of Cache.
     */
    public static final int SYNC_INTERVAL = 60*60*1000; // 1 hour
    /** cached objects, which were not accessed within this interval, will be deleted */
    public static final int MAX_LRU = 30*60000; // 30 minutes

    public Cache() {
        data = new HashMap(100);
        modCount = 0;
        Scheduler.getScheduler().addTask(this,3*60*1000,System.currentTimeMillis()+3*60*1000);
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
                Relation key = cloneRelation(relation);
                data.put(key,new CachedObject(key));
                modCount++;

                // add this relation to affected object.
                CachedObject cached = (CachedObject) data.get(relation.getParent());
                if ( cached!=null ) {
                    cached.object.addContent((Relation)key);
                }
            } else {
                GenericObject key = (GenericObject) obj.getClass().newInstance();
                key.synchronizeWith(obj);
                key.clearContent();

                for (Iterator iter = obj.getContent().iterator(); iter.hasNext();) {
                    Relation relation = cloneRelation((Relation)iter.next());
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
                Relation relation = (Relation) found.object; // maybe famous bug lies here. return clone here

                GenericObject tmp = load(relation.getChild());
                if ( tmp!=null ) relation.setChild(tmp);

                tmp = load(relation.getParent());
                if ( tmp!=null ) relation.setParent(tmp);

                return relation;
            } else {
                GenericObject stored = found.object;
                GenericObject clone = (GenericObject) stored.getClass().newInstance();
                clone.synchronizeWith(stored);
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
     * Creates lightweight clone of selected relation. Such clone is almost same as original,
     * but child and parent objects are not initialized (just PK is set).
     */
    protected Relation cloneRelation(Relation relation) {
        Relation clone = new Relation();
        clone.synchronizeWith(relation);

        try {
            GenericObject tmp = (GenericObject)relation.getParent().getClass().newInstance();
            tmp.setId(relation.getParent().getId());
            clone.setParent(tmp);

            tmp = (GenericObject)relation.getChild().getClass().newInstance();
            tmp.setId(relation.getChild().getId());
            clone.setChild(tmp);
        } catch (Exception e) {
            log.error("Exception while cloning relation!",e);
        }

        return clone;
    }

    /**
     * When it is time to cleanup cache, this method is invoked. It
     * will scan all CachedObjects, and LRU objects are removed from cache.
     */
    public void runJob() {
        long expectedModCount = modCount;

        try {
            Iterator iter = data.values().iterator();

            long now = System.currentTimeMillis(); // calculated only once to make it as fast as possible
            while ( expectedModCount==modCount && iter.hasNext() ) {
                CachedObject cached = (CachedObject) iter.next();
                if ( cached.lastSync+SYNC_INTERVAL<now ) {
                    iter.remove();
                }
                if ( cached.lastAccessed+MAX_LRU<now ) {
                    iter.remove();
                }
            }
            // if expectedModCount!=modCount, we will postpone work for next task
        } catch (ConcurrentModificationException e) {
            log.warn("Bad timing in CacheSynchronizationDaemon!");
        }
    }

    public String getJobName() {
        return "Cache";
    }
}
