/*
 *  Copyright (C) 2008 Leos Literak
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

import cz.abclinuxu.data.view.JobsCzHolder;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;

import java.util.*;
import java.io.*;

import java.util.prefs.Preferences;
import org.apache.log4j.Logger;

/**
 * Fetches jobs.cz RSS.
 */
public class JobsCzFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(JobsCzFetcher.class);

	static final String PREF_URI = "uri";

    String uri;

    public void run() {
        // refresh list
        log.debug("Refreshing list for jobs.cz server");
        try {
            JobsCzHolder newHolder = new JobsCzHolder();
            newHolder.fetch(uri);
            VariableFetcher.getInstance().setJobsCzHolder(newHolder);
        } catch (IOException e) {
            log.error("IO problems for " + uri + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Selhalo nacitani pracovnich pozic serveru jobs.cz", e);
        }
    }

    public JobsCzFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
		uri = prefs.get(PREF_URI, null);
    }

    public static void main(String[] args) throws Exception {
        new JobsCzFetcher().run();
    }
}

