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

/**
 * Cache of GenericObjects. Only selected classes are cached.
 * @todo Complete rewrite needed. Add Date lru to CacheObject.
 * Use it as LRU, delete objects not accessed within 30 minutes.
 */
public class Cache {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Cache.class);

    Persistance persistance;
    CacheSynchronizationDaemon daemon;
    Map data;
    long modCount;

    public static final int SYNC_INTERVAL = 5*60*1000; // 5 minutes
    /** cached objects, which were not accessed within this interval, will be deleted */
    public static final int MAX_LRU = 30*60000; // 30 minutes

    public Cache(Persistance persistance) {
        this.persistance = persistance;
        data = new HashMap(100);
        modCount = 0;
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
            touch(found);

            if ( obj instanceof Relation ) {
                Relation relation = (Relation) found.object;

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
     * If <code>obj</code> is deleted from from persistant storage,
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
     * Analogue of unix's command touch. It doesn't affect content of object, just changes
     * last modification timestamp.
     */
    protected void touch(CachedObject cachedObject) {
        cachedObject.lastAccessed = System.currentTimeMillis();
    }

    /**
     * Starts new thread, which periodically checks validity of objects and if needed,
     * synchronizes them.<p>
     * Don't start sync daemon, until you start to make modifications in database. Synchronization
     * implies performance hit! Or make it run time option (like JMX method).
     */
    public void startUp() {
        if ( daemon!=null ) daemon.stop = true;
        daemon = new CacheSynchronizationDaemon();
        daemon.start();
    }

    /**
     * Stops daemon.
     */
    public void shutDown() {
        if ( daemon!=null ) daemon.stop = true;
        daemon = null;
    }

    class CacheSynchronizationDaemon extends Thread {
        boolean stop = false;
        long expectedModCount;
        long nextSync = System.currentTimeMillis()+SYNC_INTERVAL;

        public void run() {
            log.info("CacheSynchronizationDaemon starts ...");
            setDaemon(true);

            while ( !stop ) {
                try {
                    expectedModCount = modCount;
                    Iterator iterator = data.values().iterator();

                    while ( expectedModCount==modCount && iterator.hasNext() ) {
                        CachedObject cached = (CachedObject) iterator.next();
                        if ( cached.lastSync<System.currentTimeMillis() ) {
                            persistance.synchronizeCached(cached);
                        }
                        if ( cached.lastSync<nextSync ) nextSync = cached.lastSync;
                        yield();
                    }
                    if ( expectedModCount!=modCount ) { // concurrent modification of data
                        nextSync = System.currentTimeMillis();
                        yield(); // let the second thread finish its work
                    }
                } catch (ConcurrentModificationException e) {
                    log.error("Bad timing in CacheSynchronizationDaemon!");
                }

                long dreaming = nextSync - System.currentTimeMillis();
                if ( dreaming>0 ) {
                    try { Thread.sleep(dreaming); } catch (InterruptedException e) {}
                }
            }

            log.info("CacheSynchronizationDaemon dies ...");
        }
    }
}
