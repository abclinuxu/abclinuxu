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
package cz.abclinuxu.utils.parser.safehtml;

import org.htmlparser.util.ParserException;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.nodes.TagNode;
import org.htmlparser.lexer.nodes.Attribute;
import org.htmlparser.lexer.nodes.PageAttribute;
import org.htmlparser.Node;

import java.util.*;

/**
 * Class, that can check validity and conformance to policy of HTML text.
 */
public class TagValidator {

    /**
     * Performs check of html string.
     * @param s html to be checked.
     * @param allowedTags list of allowed tags and their policy.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotAllowedException If tag is not allowed or recognized.
     * @throws cz.abclinuxu.utils.parser.safehtml.TagNotClosedException If tag is not closed.
     * @throws cz.abclinuxu.utils.parser.safehtml.AttributeNotAllowedException If attribute is not allowed.
     */
    static void check(String s, Map allowedTags) throws HtmlCheckException, ParserException {
        Lexer lexer = new Lexer(s);
        Node node = null;
        TagNode tag = null;
        CheckedTag checkedTag = null, lastTag = null;
        String currentTagName = null;
        Vector attributes = null;
        List tagStack = new ArrayList();

        while ((node=lexer.nextNode())!=null) {
            if (!(node instanceof TagNode))
                continue;

            tag = (TagNode) node;
            currentTagName = tag.getTagName();
            checkedTag = (CheckedTag) allowedTags.get(currentTagName);
            if (checkedTag==null)
                throw new TagNotAllowedException("Značka "+currentTagName+" není povolena!");

            if (tag.isEndTag()) {
                do {
                    if (tagStack.size() == 0)
                        throw new TagNotClosedException("Nenalezena otevírací značka " + currentTagName + "! Nejsou značky překříženy?");
                    lastTag = (CheckedTag) tagStack.remove(tagStack.size() - 1);
                } while(!lastTag.mustBeClosed && !lastTag.name.equals(currentTagName));

                if (!lastTag.name.equals(currentTagName))
                    throw new CrossedTagException("Značky " + lastTag.name + " a " + currentTagName + " jsou překříženy!");
            } else
                tagStack.add(checkedTag);

            attributes = tag.getAttributesEx();
            removeTagAttribute(attributes);

            if ( checkedTag.attributes==null && attributes.size()>0 )
                throw new AttributeNotAllowedException("Značka "+checkedTag.name+" nesmí obsahovat žádné atributy!");

            for ( Iterator iter = attributes.iterator(); iter.hasNext(); ) {
                boolean found = false;
                Attribute attribute = (Attribute) iter.next();
                String name = attribute.getName();
                if ( name==null ) continue;
                name = name.toUpperCase();
                for ( int i = 0; i<checkedTag.attributes.length; i++ ) {
                    String allowedAttribute = checkedTag.attributes[i];
                    if ( name.equals(allowedAttribute) ) {
                        found = true;
                        break;
                    }
                }
                if ( !found )
                    throw new AttributeNotAllowedException("Značka "+checkedTag.name+" nesmí obsahovat atribut "+name+"!");
                if ("HREF".equals(name) || "SRC".equals(name)) {
                    String value = attribute.getValue();
                    if (value == null || value.length() == 0)
                        throw new AttributeValueNotAllowedException("Atribut " + name + "značky " + checkedTag.name + "nesmí být prázdný!");
                    if (value.indexOf("javascript:") != -1)
                        throw new AttributeValueNotAllowedException("Atribut " + name + "značky " + checkedTag.name + "nesmí obsahovat javascript!");
                    if (value.indexOf("data:") != -1)
                        throw new AttributeValueNotAllowedException("Atribut " + name + "značky " + checkedTag.name + "nesmí obsahovat protokol data!");
                }
            }
        }

        for (Iterator iter = tagStack.iterator(); iter.hasNext();) {
            checkedTag = (CheckedTag) iter.next();
            if (checkedTag.mustBeClosed)
                throw new TagNotClosedException("Značka " + checkedTag.name + " musí být uzavřena!");
        }
    }

    /**
     * getAttributesEx() returns name of tag as attribute. Remove it.
     */
    private static void removeTagAttribute(Vector attributes) {
        if (attributes==null)
            return;
        attributes.remove(0);
        for ( Iterator iter = attributes.iterator(); iter.hasNext(); ) {
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
}
