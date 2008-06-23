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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract template for multiple values in form. Uses List as backend.
 * @param <V> Values stored, must implement Selectable interface
 * @see Selectable
 * @author kapy
 * @since 03.07.2008
 */
public abstract class ListMultipleChoice<V extends Selectable> extends MultipleChoice<V> {
    
    /** Map to store chosen values */
    protected List<V> choices = new ArrayList<V>();
   
    /**
     * Returns collection filled by keys of selected items
     * @return Collection of selected items
     */
    public Collection selectedSet() {
        List<V> list = new ArrayList<V>();
        for (V element:choices) {
            // check whether element is checked
            if (element != null && element.isSet())
                list.add(element);
        }
        return list;
    }
    
    /**
     * Returns collection af all values stored in collection,
     * including not selected ones
     * @return
     */
    public Collection<V> values() {
        return choices;
    }    

    /**
     * Iterator over all stored items.
     * @return Iterator throught stored values
     */
    @Override
    public Iterator iterator() {
        return choices.iterator();
    }

    /**
     * Size of collection of all items
     * @return Size of collection
     */
    @Override
    public int size() {
        return choices.size();
    }
    
    
}

