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
package cz.abclinuxu.utils.comparator;

import cz.abclinuxu.utils.freemarker.Tools;
import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.GenericObject;

import java.util.Comparator;
import java.text.Collator;

/**
 * This comparator sorts Relations or GenericDataObjects by their name
 * in ascending order.
 */
public class NameComparator implements Comparator {
    Collator collator = Collator.getInstance();

    public int compare(Object o1, Object o2) {
        String s1, s2;
        if (o1 instanceof Relation)
            s1 = Tools.childName((Relation) o1).toLowerCase();
        else
            s1 = Tools.childName((GenericObject) o1).toLowerCase();
        if (o2 instanceof Relation)
            s2 = Tools.childName((Relation) o2).toLowerCase();
        else
            s2 = Tools.childName((GenericObject) o2).toLowerCase();
        return collator.compare(s1, s2);
    }
}
