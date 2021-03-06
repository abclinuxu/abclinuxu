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
package cz.abclinuxu.utils.parser.clean.impl;

import cz.abclinuxu.utils.parser.clean.exceptions.AttributeValueNotAllowedException;
import cz.abclinuxu.utils.parser.clean.exceptions.AttributeValueNotAllowedException;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Definition of one tag attribute and its allowed values.
 */
public class Attribute {
    String id;
    AttributeChecker checker;
    Set<String> values;

    public Attribute(String id) {
        this.id = id;
    }

    /**
     * Checks value of the attribute.
     * @param value attribute value
     * @param tag tag name
     * @throws AttributeValueNotAllowedException value is not allowed
     */
    public void check(String value, String tag) throws AttributeValueNotAllowedException {
        if (value == null || value.length() == 0)
            throw new AttributeValueNotAllowedException("Atribut " + id + " značky " + tag + " nesmí být prázdný!");

        if (values != null) {
            boolean found = false;
            for (String s : values) {
                if (Pattern.matches(s,value.toUpperCase()))
                    found = true;
            }
            if(!found)
                throw new AttributeValueNotAllowedException("Hodnota '" + value + "' atributu " + id + " značky " + tag + " není povolena!");
        }

        if (checker != null)
            checker.check(value, tag, id);
    }

    public String getId() {
        return id;
    }

    public void setChecker(AttributeChecker checker) {
        this.checker = checker;
    }

    public void setValues(List<String> values) {
        Set<String> newValues = new HashSet<String>(values.size(), 1.0f);
        for (String s : values) {
            newValues.add(s.toUpperCase());
        }
        this.values = newValues;
    }

    public String toString() {
        return id;
    }
}
