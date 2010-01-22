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

import cz.abclinuxu.data.view.JobsCzItem;
import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;

import java.util.*;
import java.io.*;

import java.util.prefs.Preferences;
import org.apache.log4j.Logger;

/**
 * Fetches jobs.cz RSS.
 */
public class JobsCzFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(JobsCzFetcher.class);

    static final String PREF_FILE = "file";
    static final String PREF_URI_PAGE = "uriPage";
	static final String PREF_URI_HP = "uriHP";

    String fileName, uriPage, uriHP;

    public void run() {
        // refresh list
        log.debug("Refreshing list for jobs.cz server");
        VariableFetcher.getInstance().refreshJobsCz(uriPage, uriHP);
        
        // create include file
        List<JobsCzItem> result = VariableFetcher.getInstance().getFreshJobsCz();
        Map env = new HashMap();
        env.put("ITEMS", result);
        String file = AbcConfig.calculateDeployedPath(fileName);
        try {
            FMUtils.executeTemplate("/include/misc/generate_jobscz.ftl", env, new File(file));
            log.debug("Jobs.cz include file generated");
        } catch (IOException e) {
            log.error("IO problems for " + uriHP + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from " + uriHP, e);
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
        uriPage = prefs.get(PREF_URI_PAGE, null);
		uriHP = prefs.get(PREF_URI_HP, null);
        fileName = prefs.get(PREF_FILE, null);
    }

    public static void main(String[] args) throws Exception {
        new JobsCzFetcher().run();
    }
}

