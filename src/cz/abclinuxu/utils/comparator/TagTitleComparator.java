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

import net.eithel.CzechComparator;

import java.util.Comparator;

import cz.abclinuxu.data.Tag;

/**
 * Compares two Tags by their title property in specified order.
 */
public class TagTitleComparator implements Comparator {
//        Collator collator = Collator.getInstance();
    CzechComparator collator = CzechComparator.getInstance();
    boolean ascending;

    public TagTitleComparator(boolean ascending) {
        this.ascending = ascending;
    }

    public int compare(Object o1, Object o2) {
        String s1 = ((Tag) o1).getTitle().toString();
        String s2 = ((Tag) o2).getTitle().toString();
        return ((ascending) ? 1 : -1) * collator.compare(s1, s2);
    }
}
