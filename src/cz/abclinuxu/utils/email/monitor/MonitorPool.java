/*
 * User: literakl
 * Date: 6.11.2003
 * Time: 12:08:11
 */
package cz.abclinuxu.utils.email.monitor;

import org.dom4j.Element;

import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Collections;

import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.utils.email.monitor.MonitorAction;

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
        pool = Collections.synchronizedList(new LinkedList());
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
    public boolean isEmpty() {
        return pool.size()==0;
    }

    /**
     * Extracts the first MonitorAction from the queue.
     * @return first MonitorAction
     * @throws java.lang.IndexOutOfBoundsException If there is no element in the pool.
     */
    public MonitorAction getFirst() {
        return (MonitorAction) pool.remove(0);
    }

    /**
     * Asynchronously schedules MonitorAction, so InstantSender can process
     * it later and the current thread can continue without delay.
     * @param action
     */
    public static void scheduleMonitorAction(MonitorAction action) {
        if ( action.object!=null ) {
            Element monitor = (Element) action.object.getData().selectSingleNode("//monitor");
            if ( monitor==null )
                return;
            action.monitor = monitor.createCopy();
        }

        action.performed = new Date();

        try {
            if (action.object!=null) {
                GenericDataObject clone = (GenericDataObject) action.object.getClass().newInstance();
                clone.setId(action.object.getId());
                action.object = clone;
            }
        } catch (Exception e) {
            log.error("Cannot create copy of "+action.object, e);
        }

        singleton.pool.add(action);
    }
}
