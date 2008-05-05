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

import cz.abclinuxu.utils.config.Configurable;
import cz.abclinuxu.utils.config.ConfigurationException;
import cz.abclinuxu.utils.config.Configurator;
import cz.abclinuxu.utils.config.ConfigurationManager;
import cz.abclinuxu.utils.config.impl.AbcConfig;
import cz.abclinuxu.utils.freemarker.FMUtils;
import cz.abclinuxu.utils.freemarker.Tools;

import java.util.prefs.Preferences;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Fetches Unixshop RSS.
 */
public class JobsCzFetcher extends TimerTask implements Configurable {
    static Logger log = Logger.getLogger(JobsCzFetcher.class);

    static final String PREF_FILE = "file";
    static final String PREF_URI = "uri";

    String fileName, uri;

    public void run() {
        log.debug("Fetching Jobs.cz RSS starts ..");
        try {
            ArrayList result = new ArrayList();
            SAXReader reader = new SAXReader();
            Document document = reader.read(uri);
            List elements = document.selectNodes("/positionList/position");
            if (elements != null) {
                for (Iterator iter = elements.iterator(); iter.hasNext();) {
                    Element element = (Element) iter.next();
                    String title = element.elementText("positionName");
                    title = Tools.encodeSpecial(title);

                    JobItem jobItem = new JobItem();
                    jobItem.setUrl(element.elementText("url"));
                    jobItem.setPositionName(title);
                    jobItem.setCompanyName(element.elementText("companyName"));

                    List subElements = element.selectNodes("workLocalityList/locality");
                    if (subElements != null && subElements.size() > 0) {
                        String[] strings = new String[subElements.size()];
                        for (int j = 0; j < subElements.size(); j++) {
                            Element elementIn = (Element) subElements.get(j);
                            strings[j] = elementIn.getText();
                        }
                        jobItem.setLocalities(strings);
                    }

                    subElements = element.selectNodes("skillList/skill");
                    if (subElements != null && subElements.size() > 0) {
                        String[] strings = new String[subElements.size()];
                        for (int i = 0; i < subElements.size(); i++) {
                            Element elementIn = (Element) subElements.get(i);
                            strings[i] = elementIn.getText();
                        }
                        jobItem.setSkills(strings);
                    }

                    result.add(jobItem);
                }
            }

            Map env = new HashMap();
            env.put("ITEMS", result);
            String file = AbcConfig.calculateDeployedPath(fileName);
            FMUtils.executeTemplate("/include/misc/generate_jobscz.ftl", env, new File(file));
            log.debug("Jobs.cz include file generated");
        } catch (IOException e) {
            log.error("IO problems for " + uri + ": " + e.getMessage());
        } catch (Exception e) {
            log.error("Cannot parse links from " + uri, e);
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
        fileName = prefs.get(PREF_FILE, null);
    }

    public static void main(String[] args) throws Exception {
        new JobsCzFetcher().run();
    }

    public static class JobItem {
        String url, positionName, companyName;
        String[] skills, localities;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getPositionName() {
            return positionName;
        }

        public void setPositionName(String positionName) {
            this.positionName = positionName;
        }

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String[] getSkills() {
            return skills;
        }

        public void setSkills(String[] skills) {
            this.skills = skills;
        }

        public String[] getLocalities() {
            return localities;
        }

        public void setLocalities(String[] localities) {
            this.localities = localities;
        }
    }
}
