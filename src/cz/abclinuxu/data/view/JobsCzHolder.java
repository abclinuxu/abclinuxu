/*
 *  Copyright (C) 2008 Karel Piwko
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Holds structure parsed from XML file for jobs.cz server
 * @author kapy
 */
public class JobsCzHolder {
    private static Logger log = Logger.getLogger(JobsCzHolder.class);

    /**
     * Empty JobsCzHolder
     */
    public static final JobsCzHolder EMPTY_HOLDER = new JobsCzHolder();
    
    /** List of jobs */
    private List<JobsCzItem> jobsList;

    /** Creates new empty Jobs.cz holder */
    public JobsCzHolder() {
        jobsList = Collections.emptyList();
    }

    /**
     * Parses XML at given URI to feed values of holder
     * @param uri Where to find document to be parsed
     * @throws java.lang.Exception
     */
    public void fetch(String uri) throws Exception {

        log.debug("Fetching Jobs.cz RSS starts ..");

        List<JobsCzItem> jobs = new ArrayList<JobsCzItem>();

        SAXReader reader = new SAXReader();
        Document document = reader.read(uri);
        List elements = document.selectNodes("/positionList/position");
        for (Element element : (List<Element>) elements) {
            String title = element.elementText("positionName");
            title = Tools.encodeSpecial(title);

            JobsCzItem jobItem = new JobsCzItem();
            jobItem.setUrl(element.elementText("url"));
            jobItem.setPositionName(title);

            jobs.add(jobItem);
        }
        log.debug("Fetching Jobs.cz finished");
        
        this.jobsList = jobs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JobsCzHolder)) {
            return false;
        }

        final JobsCzHolder j = (JobsCzHolder) o;
        return jobsList.equals(j.getJobsList());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.jobsList != null ? this.jobsList.hashCode() : 0);
        return hash;
    }

    /**
     * Returs all jobs 
     * @return All jobs stored in holder
     */
    public List<JobsCzItem> getJobsList() {
        return jobsList;
    }
}
