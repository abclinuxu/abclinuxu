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

package cz.abclinuxu.utils.forms;

import cz.abclinuxu.utils.freemarker.Tools;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Values selected in form when choosing jobs skills
 * @author kapy
 * @since 03.06.2008
 */
public class JobsCzSkillsSet extends 
        ListMultipleChoice<JobsCzSkillsSet.JobSkill>{
    
    /** all values available */
    private List<String> allValues;
    
    /** 
     * Creates new skills set
     * @param param Object from session to be used to fill form
     * @param skills Collection of all posible skills
     */
    public JobsCzSkillsSet(Object param, Collection<String> skills) {
        this.noneIsAll = true;
        allValues = new ArrayList<String>(skills);
        List<String> checked = (List<String>) Tools.asList(param);

        for (String skill : allValues) {
            JobSkill js;
            // checkbox was checked
            if (checked.contains(skill)) {
                js = new JobSkill(skill, true);
                selected++;
            }
            // checkbox was not checked
            else {
                js = new JobSkill(skill, false);
            }
            choices.add(js);
        }
        
    }

    @Override
    public boolean isEverythingSelected() {
        return selected == allValues.size();
    }

    @Override
    public Collection<String> selectedSet() {
        
        List<String> list = new ArrayList<String>();
        for (JobSkill element:choices) {
            // check whether element is checked
            if (element != null && element.isSet())
                list.add(element.getSkill());
        }
        return list;
    }
       
    /**
     * Job skill allowed to be selected in form
     */
    public static class JobSkill implements Selectable {
        
        private boolean set;
        private String skill;

        public JobSkill(String skill, boolean set) {
            this.set = set;
            this.skill = skill;
        }

        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }

        public String getSkill() {
            return skill;
        }
        
    }

}

