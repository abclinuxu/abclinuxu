/*
 *  Copyright (C) 2006 Leos Literak
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; see the file COPYING.  If not, write to
 *  the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package cz.abclinuxu.scheduler;

import cz.abclinuxu.persistence.SQLTool;

import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Saves statistics about page views in one batch.
 * @author literakl
 * @since 1.4.2006
 */
public final class UpdateStatistics extends TimerTask {
    static Logger log = Logger.getLogger(UpdateStatistics.class);

    static UpdateStatistics instance;
    static {
        instance = new UpdateStatistics();
    }

    Map entries;
    boolean batchMode;

    /**
     * Records new page view in statistics for selected type of page.
     */
    public void recordPageView(String page) {
        if (! batchMode) {
            SQLTool.getInstance().recordPageView(page, 1);
            return;
        }

        synchronized(this) {
            Integer count = (Integer) entries.get(page);
            if (count == null)
                count = 1;
            else
                count = count + 1;
            entries.put(page, count);
        }
    }

    public void run() {
        Map toBeSaved;
        synchronized(this) {
            toBeSaved = entries;
            entries = new HashMap(20, 0.95f);
        }

        try {
            SQLTool sqlTool = SQLTool.getInstance();
            for (Iterator iter = toBeSaved.keySet().iterator(); iter.hasNext();) {
                String page = (String) iter.next();
                Integer count = (Integer) toBeSaved.get(page);
                sqlTool.recordPageView(page, count);
            }
        } catch (Throwable e) {
            log.error("Batch update of statistics failed", e);
        }
    }

    /**
     * Sets whether batch mode is active.
     * @param batchMode if true, all pageviews are persisted in one update, otherwise it is recorded immediatelly
     */
    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

    private UpdateStatistics() {
        entries = new HashMap(20, 0.95f);
    }

    public static UpdateStatistics getInstance() {
        return instance;
    }
}
