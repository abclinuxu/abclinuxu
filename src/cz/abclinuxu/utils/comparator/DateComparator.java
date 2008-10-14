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

import cz.abclinuxu.data.Relation;
import cz.abclinuxu.data.Link;
import cz.abclinuxu.data.GenericDataObject;
import cz.abclinuxu.data.Item;
import cz.abclinuxu.data.Poll;
import cz.abclinuxu.data.view.DiscussionHeader;
import cz.abclinuxu.exceptions.InvalidInputException;

import java.util.Comparator;
import java.util.Date;

/**
 * This comparator sorts GenericObjects by their
 * modified property in ascending order. If GenericObject
 * is an relation, then its child is compared.
 */
public class DateComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        Date d1 = getValue(o1);
        Date d2 = getValue(o2);
        return d1.compareTo(d2);
    }
    /**
     * Extracts date value of GenericObject.
     */
    private Date getValue(Object obj) {
        if ( obj instanceof Relation )
            obj = ((Relation)obj).getChild();

        if ( obj instanceof Link )
            return ((Link)obj).getUpdated();
        if ( obj instanceof DiscussionHeader )
            return ((DiscussionHeader)obj).getUpdated();
        if ( obj instanceof GenericDataObject ) {
            GenericDataObject gdo = (GenericDataObject) obj;
            if ( gdo instanceof Item && gdo.getType()==Item.ARTICLE )
                return gdo.getCreated();
            else
                return gdo.getUpdated();
        }
        if ( obj instanceof Poll )
            return ((Poll)obj).getCreated();

        throw new InvalidInputException("Don't know how to handle " + obj);
    }
}
