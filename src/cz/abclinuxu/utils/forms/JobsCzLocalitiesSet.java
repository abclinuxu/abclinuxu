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
 * Job's localities selected from form
 * @since 03.06.2008
 * @author kapy
 */
public class JobsCzLocalitiesSet extends 
        ListMultipleChoice<JobsCzLocalitiesSet.JobLocality>{
    
    /** All available values */
    private List<String> allValues;
    
    /**
     * Creates new localities holder
     * @param param Object from session to be used to fetch selected values
     * @param localities All posible values
     */
    public JobsCzLocalitiesSet(Object param, Collection<String> localities) {
        this.noneIsAll = true;
        allValues = new ArrayList<String>(localities);
        List<String> checked = (List<String>) Tools.asList(param);

        for (String locality : allValues) {
            JobLocality jl;
            // checkbox was checked
            if (checked.contains(locality)) {
                jl = new JobLocality(locality, true);
                selected++;
            }
            // checkbox was not checked
            else {
                jl = new JobLocality(locality, false);
            }
            choices.add(jl);
        }
        
    }

    @Override
    public boolean isEverythingSelected() {
        return selected == allValues.size();
    }

    @Override
    public Collection<String> selectedSet() {
        
        List<String> list = new ArrayList<String>();
        for (JobLocality element:choices) {
            // check whether element is checked
            if (element != null && element.isSet())
                list.add(element.getLocality());
        }
        return list;
    }
    
    
    /**
     * Job's locality allowed to be selected in form
     */
    public static class JobLocality implements Selectable {
        
        private boolean set;
        private String locality;

        public JobLocality(String locality, boolean set) {
            this.set = set;
            this.locality = locality;
        }

        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }

        public String getLocality() {
            return locality;
        }
        
        
    }

}
