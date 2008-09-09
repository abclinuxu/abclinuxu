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

package cz.abclinuxu.persistence.extra.tags;

import cz.abclinuxu.exceptions.InvalidInputException;
import cz.abclinuxu.utils.TagTool;

/**
 *
 * @author lubos
 */
public class TagValue extends TagExpression {
    String tag;
    boolean negated;
    
    public TagValue(String tag, boolean negated) throws InvalidInputException {
        this.tag = TagTool.getNormalizedId(tag);
        this.negated = negated;
    }
    
    @Override
    public String toString() {
        String value = ((negated) ? "0" : "1");
        return "bit_or(T.stitek = '"+tag+"') = " + value;
    }
}
