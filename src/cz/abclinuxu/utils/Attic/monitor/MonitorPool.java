/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:08:11
 */
package cz.abclinuxu.utils.monitor;

import org.dom4j.Element;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;

import cz.abclinuxu.data.GenericDataObject;

/**
 * Pool for triggered actions used by InstantSender.
 */
public class MonitorPool {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MonitorPool.class);

    static MonitorPool singleton;
    static {
        singleton = new MonitorPool();
    }

    List pool;

    /**
     * Default constructor.
     */
    private MonitorPool() {
        pool = new LinkedList();
    }

    /**
     * Gives access to singleton.
     */
    public static MonitorPool getInstance() {
        return singleton;
    }

    /**
     * Finds out, whether the pool is empty.
     * @return true, if the pool doesn't contain any unprocessed MonitorAction.
     */
    public synchronized boolean isEmpty() {
        return pool.size()==0;
    }

    /**
     * Extracts the first MonitorAction from the queue.
     * @return first MonitorAction
     * @throws IndexOutOfBoundsException If there is no element in the pool.
     */
    public synchronized MonitorAction getFirst() {
        return (MonitorAction) pool.remove(0);
    }

    /**
     * Asynchronously schedules MonitorAction, so InstantSender can process
     * it later and the current thread can continue without delay.
     * @param action
     */
    public static void scheduleMonitorAction(MonitorAction action) {
        Element monitor = (Element) action.object.getData().selectSingleNode("//monitor");
        if (monitor==null)
            return;

        action.monitor = monitor.createCopy();
        action.performed = new Date();

        try {
            GenericDataObject clone = (GenericDataObject) action.object.getClass().newInstance();
            clone.setId(action.object.getId());
            action.object = clone;
        } catch (Exception e) {
            log.error("Cannot create copy of "+action.object, e);
        }

        synchronized (singleton) {
            singleton.pool.add(action);
        }
    }
}
