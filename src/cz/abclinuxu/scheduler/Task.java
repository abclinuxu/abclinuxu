/*
 * User: literakl
 * Date: Jan 28, 2002
 * Time: 1:24:48 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

/**
 * Every object passed as argument to Scheduler, must implement this interface.
 * If it needs to be instantiated byScheduler, it must also implement public
 * default constructor.
 */
public interface Task {

    /**
     * When it is time to schedule this task, this method will be invoked.
     */
    public void runJob();

    /**
     * for logging
     */
    public String getJobName();
}
