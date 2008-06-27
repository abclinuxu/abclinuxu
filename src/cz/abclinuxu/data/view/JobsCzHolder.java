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

import cz.abclinuxu.utils.forms.Selectable;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
    /** Localities of jobs, <code>null</code> should be skipped */
    private Set<String> localities;
    /** Skills required to take job, <code>null</code> should be skipped */
    private Set<String> skills;

    /** Creates new empty Jobs.cz holder */
    public JobsCzHolder() {
        jobsList = Collections.emptyList();
        localities = Collections.emptySet();
        skills = Collections.emptySet();
    }

    /**
     * Parses XML at given URI to feed values of holder
     * @param uri Where to find document to be parsed
     * @throws java.lang.Exception
     */
    public void fetch(String uri) throws Exception {

        log.debug("Fetching Jobs.cz RSS starts ..");

        List<JobsCzItem> jobs = new ArrayList<JobsCzItem>();
        Set<String> locations = new TreeSet<String>();
        Set<String> requiredSkills = new TreeSet<String>();

        SAXReader reader = new SAXReader();
        Document document = reader.read(uri);
        List elements = document.selectNodes("/positionList/position");
        for (Element element : (List<Element>) elements) {
            String title = element.elementText("positionName");
            title = Tools.encodeSpecial(title);

            JobsCzItem jobItem = new JobsCzItem();
            jobItem.setUrl(element.elementText("url"));
            jobItem.setPositionName(title);
            jobItem.setCompanyName(element.elementText("companyName"));
            jobItem.setCreateDate(element.elementText("createDate"));
            
            // locations
            List subElements = element.selectNodes("localityList/locality");
            if (subElements != null && subElements.size() > 0) {
                String[] strings = new String[subElements.size()];
                for (int j = 0; j < subElements.size(); j++) {
                    Element elementIn = (Element) subElements.get(j);
                    String value = elementIn.getText();
                    strings[j] = value;
                    if (value!=null &&!"".equals(value)) locations.add(value);
                }
                jobItem.setLocalities(strings);

            }

            // skills
            subElements = element.selectNodes("skillList/skill");
            if (subElements != null && subElements.size() > 0) {
                String[] strings = new String[subElements.size()];
                for (int i = 0; i < subElements.size(); i++) {
                    Element elementIn = (Element) subElements.get(i);
                    String value = elementIn.getText();
                    strings[i] = value;
                    if (value !=null && !"".equals(value)) requiredSkills.add(value);
                }
                jobItem.setSkills(strings);
            }

            jobs.add(jobItem);
        }
        log.debug("Fetching Jobs.cz finished");
        
        this.jobsList = jobs;
        this.localities = locations;
        this.skills = requiredSkills;

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
        if (!jobsList.equals(j.getJobsList()) || !localities.equals(j.getLocalities())
                || !skills.equals(j.getSkills())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.jobsList != null ? this.jobsList.hashCode() : 0);
        hash = 79 * hash + (this.localities != null ? this.localities.hashCode() : 0);
        hash = 79 * hash + (this.skills != null ? this.skills.hashCode() : 0);
        return hash;
    }

    /**
     * Returs all jobs 
     * @return All jobs stored in holder
     */
    public List<JobsCzItem> getJobsList() {
        return jobsList;
    }
    
    /**
     * Returs jobs restricted by localities and skills
     * @param localities List of required localites, if <code>null</code> 
     * or empty, it is not used
     * @param skills List of required skills, if <code>null</code>
     * or empty, it is not used
     * @return New list of jobs which passed restriction conditions
     */
    public List<JobsCzItem> getJobsList(Collection<String> localities, Collection<String> skills) {
        
        List<JobsCzItem> result = new ArrayList<JobsCzItem>();
        
        // check localities
        if (localities == null || localities.isEmpty()) 
            result = new ArrayList<JobsCzItem>(jobsList);
        else 
            for (JobsCzItem item: jobsList) 
                for (String locality: item.getLocalities()) 
                    if (localities.contains(locality)) {
                        result.add(item);
                        break;
                    }
        
        // check skills
        if ( ! (skills == null || skills.isEmpty()))  {
            Iterator<JobsCzItem> iter = result.iterator();
            while (iter.hasNext()) {
                JobsCzItem item = iter.next();
                boolean contains = false;
                
                for (String skill: item.getSkills())
                    if (skills.contains(skill)) {
                        contains = true;
                        break;
                    }
                    
                if (!contains) iter.remove();
            }
        }
        return result;
    }

    /**
     * Gets localities of all jobs
     * @return Available localities
     */
    public Set<String> getLocalities() {
        return localities;
    }

    /**
     * Gets skills of all jobs
     * @return Available skills
     */
    public Set<String> getSkills() {
        return skills;
    }
    
}
