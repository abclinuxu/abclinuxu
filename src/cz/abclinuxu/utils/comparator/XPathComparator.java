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
import cz.abclinuxu.data.CommonObject;

import java.util.Comparator;

import org.dom4j.Node;

/**
 * This comparator sorts CommonObject (or relations containing CommonObjects) by specified xpath.
 */
public class XPathComparator implements Comparator {
    String xpath;

    public XPathComparator(String xpath) {
        this.xpath = xpath;
    }

    public int compare(Object o1, Object o2) {
        CommonObject i1, i2;
        if (o1 instanceof Relation)
            i1 = (CommonObject) ((Relation) o1).getChild();
        else
            i1 = (CommonObject) o1;
        if (o2 instanceof Relation)
            i2 = (CommonObject) ((Relation) o2).getChild();
        else
            i2 = (CommonObject) o2;

        Node n1 = i1.getData().selectSingleNode(xpath);
        Node n2 = i2.getData().selectSingleNode(xpath);

        if (n1 == null) {
            if (n2 == null)
                return 0;
            else
                return -1;
        }
        if (n2 == null)
            return 1;

        String s1 = n1.getText();
        String s2 = n2.getText();
        return s1.compareTo(s2);
    }
}
