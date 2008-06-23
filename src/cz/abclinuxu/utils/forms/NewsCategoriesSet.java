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

import cz.abclinuxu.data.view.NewsCategories;
import cz.abclinuxu.data.view.NewsCategory;
import cz.abclinuxu.utils.freemarker.Tools;
import java.util.List;
import java.util.Map;

/**
 * Set of selected NewCategories from all possible categories
 * @author kapy
 */
public class NewsCategoriesSet extends 
        MapMultipleChoice<String, NewsCategoriesSet.SelectedNewsCategory> {
    
    /** All possible categories */
    protected Map<String, NewsCategory> allCategories;

    /**
     * Creates new set from values passed
     * @param param Object holding values stored in HTTP session/request
     */
    public NewsCategoriesSet(Object param) {
        
        this.noneIsAll = true;
        allCategories = NewsCategories.getAllCategoriesAsMap();
        List checked = (List<String>) Tools.asList(param);
       
        // no item selected means all selected
        if(checked.size()==0 && noneIsAll) {
            for (NewsCategory nc: allCategories.values()) {
                SelectedNewsCategory snc = new SelectedNewsCategory(nc, true);
                choices.put(nc.getKey(), snc);
            }
        }
        // check manually selection
        else {
            for (NewsCategory nc: allCategories.values()) {
                String key = nc.getKey();
                SelectedNewsCategory snc;
                // checkbox was checked
                if(checked.contains(key)) {
                    snc = new SelectedNewsCategory(nc, true);
                    selected++;
                }
                // checkbox was not checked
                else {
                    snc = new SelectedNewsCategory(nc, false);
                }
                choices.put(key, snc);
            }
        }
    }
    
    /**
     * 
     * @return <code>true</true> if all possible categories were/are selected,
     * <code>false</code> otherwise
     */
    @Override
    public boolean isEverythingSelected() {
        return selected == allCategories.size();
    }

    /**
     * Encapsulates NewCategory adding possibility to be selected
     */
    public static class SelectedNewsCategory extends NewsCategory implements Selectable {

        /** selection flag */
        private boolean set;
        
        /**
         * Creates NewsCategory object with allowed selection
         * @param category Category
         * @param set Selection flag
         */
        public SelectedNewsCategory(NewsCategory category, boolean set) {
            super(category.getKey(), category.getName(), category.getDesc());
            this.set = set;
        }
        
        public boolean isSet() {
            return set;
        }

        public void setSet(boolean set) {
            this.set = set;
        }
        
    }
    
}
