/*
 * User: literakl
 * Date: Feb 3, 2002
 * Time: 4:55:47 PM
 * (c)2001-2002 Tinnio
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.scheduler.jobs.UpdateLinks;

import java.util.*;

/**
 * Quick and dirty implementation of scheduler.
 */
public class Scheduler extends Thread {
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Scheduler.class);
    static Scheduler scheduler;
    static int MAX_SLEEP = 5*60*1000;
    static int MIN_SLEEP = 30*1000;

    List jobs;


    public void run() {
        log.info("Scheduler starts ...");
        long next, now;
        addLinksUpdate();

        while ( true ) {
            next = now = System.currentTimeMillis();

            for (int i=0; i<jobs.size();i++) {
                JobItem item = (JobItem) jobs.get(i);

                if ( item.next<now ) {
                    item.next = now+item.increment;
                    Job job = new Job(item.task);
                    job.start();
                    if ( next<item.next ) next = item.next;
                }
            }

            long sleep = next-now;
            if ( sleep<MIN_SLEEP ) {
                sleep = MIN_SLEEP;
            } else if ( sleep>MAX_SLEEP ) {
                sleep = MAX_SLEEP;
            }

            if ( log.isDebugEnabled() ) log.debug("Scheduler will sleep for "+(sleep/1000)+" seconds.");
            try {
                currentThread().sleep(sleep);
            } catch (InterruptedException e) {
                log.debug("Scheduler has been waken up.");
            }
        }
    }

    protected Scheduler() {
        jobs = new ArrayList();
        setName("Scheduler");
        setDaemon(true);
        start();
    }

    public static synchronized Scheduler getScheduler() {
        if ( scheduler==null ) {
            scheduler = new Scheduler();
        }
        return scheduler;
    }

    public void addTask(Task task, long increment, long firstRun) {
        JobItem job = new JobItem();
        job.next = firstRun;
        job.increment = increment;
        job.task = task;

        jobs.add(job);
        // wake up thread
    }

    protected void addLinksUpdate() {
        Date now = new Date();
        Calendar next = Calendar.getInstance();
        next.setTime(now);
        next.set(Calendar.HOUR,7);
        next.set(Calendar.MINUTE,30);

        addTask(new UpdateLinks(),6*60*60*1000,next.getTime().getTime()); // each 6 hours, starting at 7:30 AM
    }

    class JobItem {
        long next = System.currentTimeMillis();
        long increment = 0;
        Task task;
    }
}
