/*
 * User: literakl
 * Date: 4.11.2004
 * Time: 21:09:38
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.utils.DateTool;

import java.util.TimerTask;

/**
 * At midninght recalculates times used in DateTool.
 */
public class UpdateDateTool extends TimerTask {

    public void run() {
        DateTool.updateTodayTimes();
    }
}
