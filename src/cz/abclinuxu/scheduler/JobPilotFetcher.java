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

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.prefs.Preferences;

/**
 * Fetches RSS for JobPilot.
 */
public class JobPilotFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(JobPilotFetcher.class);

    static final String PREF_FILE = "file";
    static final String PREF_URI = "uri";
    static final String PREF_MAX_ITEMS = "max";

    String fileName, uri;
    int max;

    public void run() {
        log.debug("Fetching JobPilot RSS starts ..");
        try {
            // pri pouziti SAXReader vyleti vyjimka, ze v prologu je pouzit znak 0xa0,
            // ale pouze pri cteni z URI, ze stejneho souboru je to uz v poradku
            URL url = new URL(uri);
            InputStream is = url.openStream();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = is.read()) != -1)
                sb.append((char)c);

            Document document = DocumentHelper.parseText(sb.toString());
            List nodes = document.getRootElement().elements("job");
            ArrayList result = new ArrayList();
            for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                Element element = (Element) iter.next();
                RssItem item = new RssItem();
                item.setUrl(element.elementText("url"));
                item.setTitle(element.elementText("title"));
                result.add(item);
            }

            if (result.size() == 0) {
                log.debug("data is missing - bye!");
                return;
            }

            Map env = new HashMap();
            env.put("ITEMS", result);
            String file = AbcConfig.calculateDeployedPath(fileName);
            FMUtils.executeTemplate("/include/misc/generate_jobpilot.ftl", env, new File(file));
            log.debug("JobPilot include file generated");
        } catch (IOException e) {
            log.error("IO problems for " + uri + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from " + uri, e);
        }
    }

    public JobPilotFetcher() {
        Configurator configurator = ConfigurationManager.getConfigurator();
        configurator.configureMe(this);
    }

    /**
     * Callback used to configure your class from preferences.
     */
    public void configure(Preferences prefs) throws ConfigurationException {
        uri = prefs.get(PREF_URI, null);
        fileName = prefs.get(PREF_FILE, null);
        max = prefs.getInt(PREF_MAX_ITEMS, 3);
    }

    public static void main(String[] args) throws Exception {
        new JobPilotFetcher().run();
    }

    public static class RssItem {
        String url, title, description;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
