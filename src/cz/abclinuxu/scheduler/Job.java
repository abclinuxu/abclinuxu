/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 4:56:34 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

/**
 * Thread, in which task is run.
 */
public class Job extends Thread {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Job.class);

    Task task;

    public Job(Task task) {
        this.task = task;
    }

    public void run() {
        if ( log.isDebugEnabled() ) log.debug("Starting task "+task.getJobName());

        try {
            task.runJob();
        } catch (Exception e) {
            log.error("Task has thrown exception!",e);
        }

        if ( log.isDebugEnabled() ) log.debug("Finishing task "+task.getJobName());
    }
}
