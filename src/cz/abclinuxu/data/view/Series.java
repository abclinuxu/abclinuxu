/*
 *  Copyright (C) 2007 Leos Literak
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
package cz.abclinuxu.data.view;

import cz.abclinuxu.data.Relation;

import java.io.Serializable;

/**
 * Data object containing digest from Series for some article.
 * Date: 7.1.2007
 */
public class Series implements Serializable {
    private Relation series, first, last, previous, next;
    int total;

    /**
     * Creates new Series view object. All relations must be initialized, including their children. The previous
     * and next relation may be null, in case that they would be identical to the current article.
     * @param series relation to the series item
     * @param first relation to the first article within this series
     * @param last relation to the last article within this series
     * @param previous relation to the previous article to some article within this series
     * @param next relation to the next article to some article within this series
     * @param total total number of articles in this series
     */
    public Series(Relation series, Relation first, Relation last, Relation previous, Relation next, int total) {
        this.series = series;
        this.first = first;
        this.last = last;
        this.previous = previous;
        this.next = next;
        this.total = total;
    }

    public Relation getSeries() {
        return series;
    }

    public Relation getFirst() {
        return first;
    }

    public Relation getLast() {
        return last;
    }

    public Relation getPrevious() {
        return previous;
    }

    public Relation getNext() {
        return next;
    }

    public int getTotal() {
        return total;
    }
}
