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

import cz.abclinuxu.utils.parser.clean.exceptions.AttributeNotAllowedException;
import cz.abclinuxu.utils.parser.clean.exceptions.HtmlCheckException;
import cz.abclinuxu.utils.parser.clean.exceptions.HtmlCheckException;
import org.htmlparser.lexer.PageAttribute;
import org.htmlparser.nodes.TagNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Definition of one HTML tag and its allowed attributes.
 */
public class Tag {
    String id;
    boolean mustBeClosed;
    Map<String, Attribute> attributes;

    public Tag(String id, boolean mustBeClosed) {
        this.id = id;
        this.mustBeClosed = mustBeClosed;
    }

    /**
     * Checks content of the tag.
     * @param tagNode TagNode from htmlparser
     * @throws cz.abclinuxu.utils.parser.clean.exceptions.HtmlCheckException tag content is not allowed
     */
    public void check(TagNode tagNode) throws HtmlCheckException {
        Vector tagAttributes = tagNode.getAttributesEx();
        normalizeAttributes(tagAttributes);

        if (attributes == null && tagAttributes.isEmpty())
            throw new AttributeNotAllowedException("Značka " + id + " nesmí obsahovat žádné atributy!");

        for (Iterator iter = tagAttributes.iterator(); iter.hasNext();) {
            org.htmlparser.Attribute tagAttribute = (org.htmlparser.Attribute) iter.next();
            String name = tagAttribute.getName();
            if (name == null)
                continue;

            Attribute attribute = attributes.get(name.toUpperCase());
            if (attribute == null)
                throw new AttributeNotAllowedException("Značka " + id + " nesmí obsahovat atribut " + name + "!");
            else
                attribute.check(tagAttribute.getValue(), id);
        }
    }

    /**
     * getAttributesEx() returns name of tag as attribute. Remove it.
     */
    private static void normalizeAttributes(Vector attributes) {
        if (attributes == null)
            return;
        attributes.remove(0);
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            PageAttribute attribute = (PageAttribute) iter.next();
            if (attribute.isWhitespace()) {
                iter.remove();
                continue;
            }
            if ("/".equals(attribute.getName())) {
                iter.remove();
                continue;
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setAttributes(List<Attribute> attributes) {
        Map<String, Attribute> newAttributes = new HashMap<String, Attribute>(attributes.size(), 1.0f);
        for (Attribute attribute : attributes) {
            newAttributes.put(attribute.id, attribute);
        }
        this.attributes = newAttributes;
    }

    /**
     * If true, than this is pair attribute that must have closing counterpart.
     * @return whether this tag must have closing counterpart
     */
    public boolean mustBeClosed() {
        return mustBeClosed;
    }

    public String toString() {
        return id;
    }

    public void print() {
        System.out.println("Tag " + id + ", must be closed = " + mustBeClosed);
    }
}
