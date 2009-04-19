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

import cz.abclinuxu.utils.parser.clean.Rules;
import cz.abclinuxu.utils.parser.clean.exceptions.CrossedTagException;
import cz.abclinuxu.utils.parser.clean.exceptions.HtmlCheckException;
import cz.abclinuxu.utils.parser.clean.exceptions.TagNotAllowedException;
import cz.abclinuxu.utils.parser.clean.exceptions.TagNotClosedException;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.ParserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One set of rules for particular use case.
 */
public class Definition {
    Rules id;
    DefinitionChecker checker;
    Map<String, Tag> tags;

    public Definition(Rules id) {
        this.id = id;
    }

    /**
     * Checks content of the tag.
     * @param htmlText HTML snippet to check
     * @throws HtmlCheckException tag content is not allowed
     */
    public void check(String htmlText) throws HtmlCheckException {
        Lexer lexer = new Lexer(htmlText);
        Node node;
        TagNode tagNode;
        Tag tag, lastTag;
        String currentTagName;
        List<Tag> tagStack = new ArrayList<Tag>();

        try {
            while ((node = lexer.nextNode()) != null) {
                if (!(node instanceof TagNode))
                    continue;

                tagNode = (TagNode) node;
                currentTagName = tagNode.getTagName(); // upper cased and normalized by htmlparser
                tag = tags.get(currentTagName);
                if (tag == null)
                    throw new TagNotAllowedException("Značka " + currentTagName + " není povolena!");

                if (tagNode.isEndTag()) {
                    do {
                        if (tagStack.size() == 0)
                            throw new TagNotClosedException("Nenalezena otevírací značka " + currentTagName + "! Nejsou značky překříženy?");

                        lastTag = tagStack.remove(tagStack.size() - 1);
                    } while (! lastTag.mustBeClosed && ! lastTag.id.equals(currentTagName));

                    if (! lastTag.id.equals(currentTagName))
                        throw new CrossedTagException("Značky " + lastTag.id + " a " + currentTagName + " jsou překříženy!");
                } else if (! tagNode.isEmptyXmlTag())
                    tagStack.add(tag);

                tag.check(tagNode);
            }
        } catch (ParserException e) {
            throw new HtmlCheckException("Závažná chyba při parsování vstupního textu!");
        }

        for (Tag checkedTag2 : tagStack) {
            if (checkedTag2.mustBeClosed)
                throw new TagNotClosedException("Značka " + checkedTag2.id + " musí být uzavřena!");
        }

        if (checker != null)
            checker.check(htmlText);
    }

    public void setChecker(DefinitionChecker checker) {
        this.checker = checker;
    }

    public Map<String, Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        Map<String, Tag> newTags = new HashMap<String, Tag>();
        for (Tag tag : tags) {
            newTags.put(tag.id, tag);
        }
        this.tags = newTags;
    }

    public String toString() {
        return Rules.getId(id);
    }

    public void print() {
        System.out.println("Definition " + toString() + ", checker class = " + checker);
        for (Tag tag : tags.values()) {
            tag.print();
        }
    }
}
