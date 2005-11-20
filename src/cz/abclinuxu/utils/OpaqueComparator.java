/*
 *  Copyright (C) 2005 Leos Literak
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
package cz.abclinuxu.utils;

import java.util.Comparator;

/**
 * This comparator reverses order of supplied comparator.
 */
public class OpaqueComparator implements Comparator {
    Comparator comparator;

    public OpaqueComparator(Comparator comparator) {
        this.comparator = comparator;
    }

    public int compare(Object o1, Object o2) {
        int result = comparator.compare(o2,o1);
        return result;
    }
}
