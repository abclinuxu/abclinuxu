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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract template for multiple form values. Uses Map as backend.
 * @param <K> Keys used to store values
 * @param <V> Values stored, must implement Selectable interface
 * @see Selectable
 * @author kapy
 */
public abstract class MapMultipleChoice<K,V extends Selectable> extends MultipleChoice<V> {
    
    /** Map to store chosen values */
    protected Map<K,V> choices = new HashMap<K,V>();
   
    /**
     * Returns collection filled by keys of selected items
     * @return Collection of selected items
     */
    public Collection<K> selectedSet() {
        List<K> list = new ArrayList<K>();
        for (K key:choices.keySet()) {
            // check whether element is checked
            V element = choices.get(key);
            if (element != null && element.isSet())
                list.add(key);
        }
        return list;
    }
    
    /**
     * Returns collection af all values stored in collection,
     * including not selected ones
     * @return
     */
    public Collection<V> values() {
        return choices.values();
    }    

    /**
     * Iterator over all stored items.
     * @return Iterator throught stored values
     */
    @Override
    public Iterator iterator() {
        return choices.values().iterator();
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
