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
import org.dom4j.Element;

import java.util.Set;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;

/**
 * Checks style attribute (CSS).
 * @author literakl
 * @since 22.11.2008
 */
public class StyleAttributeChecker implements AttributeChecker {
    Map<String, Set<String>> properties = new HashMap<String, Set<String>>();

    public void check(String text, String tag, String attribute) throws AttributeValueNotAllowedException {
        StringTokenizer stk = new StringTokenizer(text, ";");
        while (stk.hasMoreTokens()) {
            String expression = stk.nextToken();
            int position = expression.indexOf(':');
            if (position == -1)
                throw new AttributeValueNotAllowedException("Nepodporovaný format CSS stylu u značky " + tag +
                        ", chybí dvojtečka ve výrazu '" + expression + "'!");

            String property = expression.substring(0, position).trim().toUpperCase();
            String value = expression.substring(position + 1).trim().toUpperCase();
            Set<String> propertyValues = properties.get(property);
            if (propertyValues == null)
                throw new AttributeValueNotAllowedException("CSS vlastnost " + property + " není povolena u značky " + tag);

            if (! propertyValues.contains(value))
                throw new AttributeValueNotAllowedException("Hodnota " + value + " CSS vlastnosti " + property + " není povolena u značky " + tag);
        }
    }

    public void configure(Element element) {
        for (Iterator iter = element.elements("property").iterator(); iter.hasNext();) {
            Element propertyElement = (Element) iter.next();
            String id = propertyElement.attributeValue("id").toUpperCase();
            Set<String> values = new HashSet<String>();
            for (Iterator iterValues = propertyElement.element("values").elements().iterator(); iterValues.hasNext();) {
                Element valueElement = (Element) iterValues.next();
                values.add(valueElement.getTextTrim().toUpperCase());
            }
            properties.put(id, values);
        }

    }
}
