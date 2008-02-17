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
package cz.abclinuxu.data.view;

/**
 * Container for the content of some document. It contains all
 * text that is relevant for tokenization. No html tags are included,
 * just the text.
 * @author literakl
 * @since 15.2.2008
 */
public class ParsedDocument {
    String content;

    /**
     * Creates new instance with initialized content.
     * @param content the content of some document (all texts, no HTML tgas)
     */
    public ParsedDocument(String content) {
        this.content = content;
    }

    public ParsedDocument() {
    }

    /**
     * Gets the content of some document. The content is concatenation of all texts
     * stripped of all HTML tags.
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of some document. The content is concatenation of all texts
     * stripped of all HTML tags.
     * @param content the content
     */
    public void setContent(String content) {
        this.content = content;
    }
}
