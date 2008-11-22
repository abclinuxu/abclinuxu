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
import org.htmlparser.util.Translate;

/**
 * Verifies that this attrbute (HREF, SRC) does not contain XSS attack.
 * @author literakl
 * @since 22.11.2008
 */
public class LinkAttributeChecker implements AttributeChecker {

    public void check(String text, String tag, String id) throws AttributeValueNotAllowedException {
        String value = text.toLowerCase();
        value = Translate.decode(value);
        if (value.indexOf("javascript:") != -1 || value.indexOf("%3cscript") != -1)
            throw new AttributeValueNotAllowedException("Atribut " + id + " značky " + tag + " nesmí obsahovat javascript!");
        if (value.indexOf("data:") != -1)
            throw new AttributeValueNotAllowedException("Atribut " + id + " značky " + tag + " nesmí obsahovat protokol data!");
    }

    public void configure(Element element) {}
}
