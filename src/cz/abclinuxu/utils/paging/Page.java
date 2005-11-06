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
package cz.abclinuxu.utils.paging;

/**
 * This class encapsulates information about single page of listing.
 */
public final class Page {

    int row, size;

    /**
     * Can be instantiated only within this package.
     * @param row index of first element within whole set, starts at 0.
     * @param size number of elements within this page.
     */
    Page(int row, int size) {
        this.row = row;
        this.size = size;
    }

    /**
     * Gets initial row of this page.
     * @return  row, where page starts.
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets size of this page.
     * @return number of rows, which are contained by this page.
     */
    public int getSize() {
        return size;
    }
}
