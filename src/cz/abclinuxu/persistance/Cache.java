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
 * Store all objects. Check size of queue and if it is larger
 * than 5 MB, start to shrink it. Don't store initialized Relation
 * as content of GenericObject. Just PK. If object is requested,
 * synchronize (search) relations first. Relations shall be cached
 * too. Ensure, that stored object contains correct updated field.<p>
 * Find good policy for synchronization of content of searched
 * objects. Synchronize children - OK, but synchronize also their
 * children? And if yes, synchronize only their data or lalso their
 * content?
 */
public class Cache {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Cache.class);

    Persistance persistance;
    CacheSynchronizationDaemon daemon;
    Map data;
    int modCount;

    public static final int SYNC_INTERVAL = 5*60*1000; // 5 minutes

    public Cache(Persistance persistance) {
        this.persistance = persistance;
        data = new HashMap();
    }

    /**
     * This method stores object into cache, so it can be retrieved
     * later without queueing database. If <code>obj</code> with same
     * PK is already stored in the cache, it is replaced by new version.
     */
    public void store(GenericObject obj) {
//        try {
//            if ( obj instanceof Category || obj instanceof User ) {
//                data.put(obj,new CachedObject(obj,System.currentTimeMillis()+SYNC_INTERVAL));
//                modCount++;
//                return;
//            }
//            if ( obj instanceof Relation ) {
//                GenericObject parent = ((Relation) obj).getParent();
//                CachedObject cached = loadCachedObject(parent);
//                if ( cached!=null ) {
//                    if ( ! cached.object.getContent().contains(obj) ) {
//                        cached.object.addContent((Relation)obj);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Problems in cache",e);
//        }
    }

    /**
     * This method searches cache for specified object. If it is found, returns
     * cached object, otherwise it returns null.
     * @return cached object or null, if it is not found.
     */
    public GenericObject load(GenericObject obj) {
//        try {
//            if ( obj instanceof Category || obj instanceof User ) {
//                CachedObject cached = (CachedObject) data.get(obj);
//                if ( cached!=null ) {
//                    return cached.object;
//                }
//            }
//        } catch (Exception e) {
//            log.error("Problems in cache",e);
//        }
        return null;
    }

    /**
     * If <code>obj</code> is not available anymore, it is good practice to
     * delete it from Persistant storage too. Otherwise inconsistency occurs.
     */
    public void remove(GenericObject obj) {
//        try {
//            if ( obj instanceof Category || obj instanceof User ) {
//                data.remove(obj);
//                modCount++;
//                return;
//            }
//            if ( obj instanceof Relation ) {
//                GenericObject parent = ((Relation) obj).getParent();
//                CachedObject cached = loadCachedObject(parent);
//                if ( cached!=null ) {
//                    cached.object.getContent().remove(obj);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Problems in cache",e);
//        }
    }

    /**
     * This method searches cache for specified object.
     * @return CachedObject or null, if it is not found.
     */
    private CachedObject loadCachedObject(GenericObject obj) {
        try {
            if ( obj instanceof Category || obj instanceof User ) {
                return (CachedObject) data.get(obj);
            }
        } catch (Exception e) {
            log.error("Problems in cache",e);
        }
        return null;
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
        int expectedModCount;
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
                        if ( cached.nextSync<System.currentTimeMillis() ) {
                            persistance.synchronizeCached(cached);
                        }
                        if ( cached.nextSync<nextSync ) nextSync = cached.nextSync;
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
