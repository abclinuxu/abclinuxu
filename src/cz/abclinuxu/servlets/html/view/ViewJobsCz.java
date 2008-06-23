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

package cz.abclinuxu.servlets.html.view;

import cz.abclinuxu.data.view.JobsCzHolder;
import cz.abclinuxu.data.view.JobsCzItem;
import cz.abclinuxu.scheduler.VariableFetcher;
import cz.abclinuxu.servlets.AbcAction;
import cz.abclinuxu.servlets.Constants;
import cz.abclinuxu.servlets.utils.ServletUtils;
import cz.abclinuxu.servlets.utils.template.FMTemplateSelector;
import cz.abclinuxu.utils.Misc;
import cz.abclinuxu.utils.forms.JobsCzLocalitiesSet;
import cz.abclinuxu.utils.forms.JobsCzSkillsSet;
import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.utils.paging.Paging;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Prepares data from jobs.cz to be displayed in page
 * at abclinuxu.cz
 * @author kapy
 */
public class ViewJobsCz implements AbcAction {

    /** n-th oldest object, where display from */
    public static final String PARAM_FROM = "from";
    /** how many object to display */
    public static final String PARAM_COUNT = "count";
    
    /** what locality is selected  */
    public static final String PARAM_LOCALITIES = "locality";
    public static final String PARAM_SKILLS = "skill";

    /** Starting part of URL, until value of from parameter */
    public static final String VAR_URL_BEFORE_FROM = "URL_BEFORE_FROM";
    /** Final part of URL, after value of from parameter */
    public static final String VAR_URL_AFTER_FROM = "URL_AFTER_FROM";
    
    public static final String VAR_JOBS = "JOBS";
    public static final String VAR_LOCALITIES = "LOCS";
    public static final String VAR_SKILLS = "SKILLS";
    
    public String process(HttpServletRequest request, HttpServletResponse response, Map env) throws Exception {

        Map params = (Map) env.get(Constants.VAR_PARAMS);

        int from = Misc.parseInt((String) params.get(PARAM_FROM), 0);
        int count = Misc.getPageSize(10, 50, env, null);
        
        // get actual jobs.cz holder
        JobsCzHolder holder = VariableFetcher.getInstance().getJobsCzHolder();
        if (holder==JobsCzHolder.EMPTY_HOLDER) 
            ServletUtils.addMessage("Služba je momentálně nedostupná.", env, null);
        
        // get actual localities selected
        JobsCzLocalitiesSet localities = new JobsCzLocalitiesSet(params.get(PARAM_LOCALITIES), holder.getLocalities());
        env.put(VAR_LOCALITIES, localities);
        
        Collection<String> localitiesList;
        if (localities.isNothingSelected() || localities.isEverythingSelected())
            localitiesList = Collections.emptyList();
        else
            localitiesList = localities.selectedSet();
        
        // get actual skills selected
        JobsCzSkillsSet skills = new JobsCzSkillsSet(params.get(PARAM_SKILLS), holder.getSkills());
        env.put(VAR_SKILLS, skills);
        
        Collection<String> skillsList;
        if (skills.isNothingSelected() || skills.isEverythingSelected())
            skillsList = Collections.emptyList();
        else
            skillsList = skills.selectedSet();
        
        List<JobsCzItem> jobs = holder.getJobsList(localitiesList, skillsList);
        int total = jobs.size();
        List sublist = Tools.sublist(jobs, from, count);
        if (sublist==null) sublist = Collections.emptyList();
        
        Paging found = new Paging(sublist, from, count, total);
        env.put(VAR_JOBS, found);
        
        // paging links
        StringBuffer sb = new StringBuffer("&amp;count=").append(found.getPageSize());
        for (String skill: skillsList) {
            sb.append("&amp;").append(PARAM_SKILLS).append("=");
            sb.append(URLEncoder.encode(skill, "UTF-8"));
        }
        for (String locality: localitiesList) {
            sb.append("&amp;").append(PARAM_LOCALITIES).append("=");
            sb.append(URLEncoder.encode(locality, "UTF-8"));
        }
        
        env.put(VAR_URL_BEFORE_FROM, "/jobs" + "?from=");
        env.put(VAR_URL_AFTER_FROM, sb.toString());
        
        return FMTemplateSelector.select("ViewJobsCz", "show", env, request);
    }

    
}
