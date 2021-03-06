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

import cz.abclinuxu.data.Tag;

import java.util.Comparator;

/**
 * Compares two Tags by their usage property in specified order.
 */
public class TagUsageComparator implements Comparator {
    boolean ascending;

    public TagUsageComparator(boolean ascending) {
        this.ascending = ascending;
    }

    public int compare(Object o1, Object o2) {
        return ((ascending)? 1 : -1) * compare(((Tag) o1).getUsage(), ((Tag) o2).getUsage());
    }

    private int compare(int first, int second) {
        if (first == second)
            return 0;
        return (first > second) ? 1 : -1;
    }
}
